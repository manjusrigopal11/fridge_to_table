import React, { useEffect, useState } from 'react';

const RECIPES = [
  {
    name: 'Tomato Omelette',
    items: ['egg', 'tomato', 'onion'],
    time: '15 minutes',
    type: 'Breakfast',
    instructions: 'Beat the eggs, chop the vegetables, mix and cook in a pan.'
  },
  {
    name: 'Vegetable Fried Rice',
    items: ['rice', 'carrot', 'onion', 'beans'],
    time: '20 minutes',
    type: 'Lunch',
    instructions: 'Saute the vegetables, add cooked rice and season to taste.'
  },
  {
    name: 'Potato Curry',
    items: ['potato', 'onion', 'tomato'],
    time: '25 minutes',
    type: 'Lunch',
    instructions: 'Cook onion and tomato with spices, add potato and simmer until soft.'
  },
  {
    name: 'Paneer Bhurji',
    items: ['paneer', 'onion', 'tomato'],
    time: '20 minutes',
    type: 'Dinner',
    instructions: 'Saute onion and tomato, add crumbled paneer and cook with spices.'
  },
  {
    name: 'Curd Rice',
    items: ['rice', 'curd'],
    time: '10 minutes',
    type: 'Lunch',
    instructions: 'Mix cooked, cooled rice with curd. Add seasoning if desired.'
  },
  {
    name: 'Potato Fry',
    items: ['potato', 'onion'],
    time: '20 minutes',
    type: 'Side Dish',
    instructions: 'Slice potatoes and onion. Cook in a pan with oil and spices.'
  },
  {
    name: 'Tomato Rice',
    items: ['rice', 'tomato', 'onion'],
    time: '20 minutes',
    type: 'Lunch',
    instructions: 'Cook tomato and onion with spices, then mix with cooked rice.'
  },
  {
    name: 'Carrot Beans Poriyal',
    items: ['carrot', 'beans', 'onion'],
    time: '15 minutes',
    type: 'Side Dish',
    instructions: 'Chop the vegetables and cook until tender with your preferred seasoning.'
  },
  {
    name: 'Paneer Rice',
    items: ['rice', 'paneer', 'onion'],
    time: '20 minutes',
    type: 'Dinner',
    instructions: 'Saute onion and paneer, then mix with cooked rice and seasoning.'
  },
  {
    name: 'Simple Egg Rice',
    items: ['rice', 'egg', 'onion'],
    time: '15 minutes',
    type: 'Dinner',
    instructions: 'Scramble the egg with onion and combine with cooked rice.'
  },
  {
    name: 'Tomato Soup',
    items: ['tomato', 'onion'],
    time: '20 minutes',
    type: 'Starter',
    instructions: 'Cook tomato and onion with water, blend until smooth and season.'
  },
  {
    name: 'Vegetable Masala',
    items: ['potato', 'carrot', 'onion', 'tomato'],
    time: '25 minutes',
    type: 'Lunch',
    instructions: 'Cook the vegetables with onion, tomato and spices until tender.'
  }
];

const todayString = () => {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const daysUntil = (date) => {
  if (!date) return null;

  const [y, m, d] = date.split('-').map(Number);
  const expiry = Date.UTC(y, m - 1, d);

  const today = todayString().split('-').map(Number);
  const todayUTC = Date.UTC(today[0], today[1] - 1, today[2]);

  return Math.round((expiry - todayUTC) / 86400000);
};

const getStatus = (date) => {
  if (!date) {
    return { label: 'No expiry date', color: '#64748b' };
  }

  const days = daysUntil(date);

  if (days < 0) {
    return { label: 'Past expiry date', color: '#dc2626' };
  }

  if (days === 0) {
    return { label: 'Expiry date is today', color: '#dc2626' };
  }

  if (days <= 3) {
    return {
      label: `Expires in ${days} day${days === 1 ? '' : 's'}`,
      color: '#d97706'
    };
  }

  return { label: 'Freshness date upcoming', color: '#15803d' };
};

const initialInventory = () => {
  try {
    const saved = localStorage.getItem('fridge-to-table-inventory');
    return saved ? JSON.parse(saved) : [];
  } catch {
    return [];
  }
};

export default function App() {
  const [inventory, setInventory] = useState(initialInventory);
  const [input, setInput] = useState('');
  const [expiryDate, setExpiryDate] = useState('');
  const [quantity, setQuantity] = useState('1');
  const [selectedRecipe, setSelectedRecipe] = useState(null);
  const [message, setMessage] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    try {
      localStorage.setItem(
        'fridge-to-table-inventory',
        JSON.stringify(inventory)
      );
    } catch {
      setMessage('Could not save inventory in this browser.');
    }
  }, [inventory]);

  const addItems = (event) => {
    event.preventDefault();

    const names = [
      ...new Set(
        input
          .split(',')
          .map((name) => name.trim().toLowerCase())
          .filter(Boolean)
      )
    ];

    if (!names.length) {
      setMessage('Please enter at least one ingredient.');
      return;
    }

    const newItems = names.map((name) => {
      const existing = inventory.find((item) => item.name === name);

      return {
        id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
        name,
        quantity: Math.max(1, Number(quantity) || 1),
        expiryDate,
        used: false,
        ...(existing ? {} : {})
      };
    });

    setInventory((previous) => [
      ...previous,
      ...newItems.filter(
        (item) => !previous.some(
          (existing) => existing.name === item.name && !existing.used
        )
      )
    ]);

    setMessage(
      newItems.every((item) =>
        inventory.some(
          (existing) => existing.name === item.name && !existing.used
        )
      )
        ? 'These ingredients are already in your fridge.'
        : 'Ingredients added successfully!'
    );

    setInput('');
    setExpiryDate('');
    setQuantity('1');
  };

  const removeItem = (id) => {
    setInventory((previous) => previous.filter((item) => item.id !== id));
  };

  const markUsed = (id) => {
    setInventory((previous) =>
      previous.map((item) =>
        item.id === id ? { ...item, used: true } : item
      )
    );
  };

  const activeItems = inventory.filter((item) => !item.used);
  const availableNames = activeItems.map((item) => item.name);

  const alerts = activeItems.filter((item) => {
    const days = daysUntil(item.expiryDate);
    return days !== null && days <= 3;
  });

  const matches = RECIPES
    .map((recipe) => {
      const have = recipe.items.filter((name) =>
        availableNames.includes(name)
      );

      const missing = recipe.items.filter(
        (name) => !availableNames.includes(name)
      );

      const urgentCount = activeItems.filter((item) =>
        have.includes(item.name) &&
        item.expiryDate &&
        daysUntil(item.expiryDate) >= 0 &&
        daysUntil(item.expiryDate) <= 3
      ).length;

      return { ...recipe, have, missing, urgentCount };
    })
    .filter((recipe) => recipe.have.length > 0)
    .sort(
      (a, b) =>
        b.urgentCount - a.urgentCount ||
        b.have.length - a.have.length ||
        a.missing.length - b.missing.length
    );

  const filteredRecipes = matches.filter((recipe) =>
    recipe.name.toLowerCase().includes(search.toLowerCase())
  );

  const useRecipe = (recipe) => {
    setInventory((previous) =>
      previous.map((item) =>
        recipe.have.includes(item.name)
          ? { ...item, used: true }
          : item
      )
    );

    setSelectedRecipe(null);
    setMessage(
      'Ingredients marked as used. Please verify quantities before cooking.'
    );
  };

  const styles = {
    page: {
      maxWidth: 900,
      margin: '0 auto',
      padding: '20px 16px',
      fontFamily: 'Arial, sans-serif',
      color: '#20312a',
      background: '#f7faf7',
      minHeight: '100vh'
    },
    card: {
      background: '#fff',
      border: '1px solid #e0e9e1',
      borderRadius: 14,
      padding: 18,
      marginBottom: 16
    },
    input: {
      width: '100%',
      minWidth: 0,
      boxSizing: 'border-box',
      padding: 12,
      border: '1px solid #cbd5ce',
      borderRadius: 8,
      fontSize: 16
    },
    button: {
      padding: '11px 15px',
      background: '#18794e',
      color: '#fff',
      border: 0,
      borderRadius: 8,
      fontSize: 15,
      cursor: 'pointer'
    },
    grid: {
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 230px), 1fr))',
      gap: 12
    }
  };

  return (
    <main style={styles.page}>
      <header style={{ ...styles.card, background: '#eaf6ec' }}>
        <div style={{ fontSize: 42 }}>🥗</div>
        <h1 style={{ margin: '8px 0', color: '#166534' }}>
          Fridge to Table
        </h1>
        <p>
          Turn your fridge ingredients into delicious meals and reduce food
          waste.
        </p>

        <div style={styles.grid}>
          <div>
            <strong style={{ fontSize: 25 }}>{activeItems.length}</strong>
            <div>Ingredients available</div>
          </div>
          <div>
            <strong style={{ fontSize: 25 }}>{matches.length}</strong>
            <div>Recipe ideas</div>
          </div>
          <div>
            <strong style={{ fontSize: 25, color: '#b45309' }}>
              {alerts.length}
            </strong>
            <div>Expiry alerts</div>
          </div>
        </div>
      </header>

      {message && (
        <div
          role="status"
          style={{
            ...styles.card,
            padding: 12,
            background: '#eff6ff'
          }}
        >
          {message}
          <button
            type="button"
            onClick={() => setMessage('')}
            style={{
              float: 'right',
              border: 0,
              background: 'transparent',
              cursor: 'pointer'
            }}
            aria-label="Dismiss message"
          >
            ✕
          </button>
        </div>
      )}

      <section style={styles.card}>
        <h2>➕ Add Ingredients</h2>
        <p>Enter one or more ingredients separated by commas.</p>

        <form onSubmit={addItems}>
          <label htmlFor="ingredients">Ingredient names</label>
          <input
            id="ingredients"
            style={{ ...styles.input, marginTop: 6, marginBottom: 12 }}
            value={input}
            onChange={(event) => setInput(event.target.value)}
            placeholder="tomato, onion, potato, egg"
          />

          <div style={styles.grid}>
            <div>
              <label htmlFor="quantity">Quantity per ingredient</label>
              <input
                id="quantity"
                type="number"
                min="1"
                max="999"
                style={{ ...styles.input, marginTop: 6 }}
                value={quantity}
                onChange={(event) => setQuantity(event.target.value)}
                required
              />
            </div>

            <div>
              <label htmlFor="expiry">Expiry / use-by date</label>
              <input
                id="expiry"
                type="date"
                style={{ ...styles.input, marginTop: 6 }}
                value={expiryDate}
                onChange={(event) => setExpiryDate(event.target.value)}
              />
            </div>
          </div>

          <p style={{ color: '#64748b', fontSize: 13 }}>
            You can leave the date empty if it is unknown. Only add dates you
            have verified from the food label or another reliable source.
          </p>

          <button type="submit" style={styles.button}>
            Add to My Fridge
          </button>
        </form>
      </section>

      <section style={styles.card}>
        <h2>⏰ Expiry Reminder</h2>
        <p>
          Items with a saved date that is today, within 3 days, or in the past
          appear here.
        </p>

        {alerts.length === 0 ? (
          <p>🎉 No upcoming expiry alerts for your saved items.</p>
        ) : (
          alerts.map((item) => {
            const status = getStatus(item.expiryDate);

            return (
              <div
                key={item.id}
                style={{
                  padding: 12,
                  marginBottom: 8,
                  borderRadius: 8,
                  background: '#fff7ed',
                  border: '1px solid #fed7aa'
                }}
              >
                <strong style={{ textTransform: 'capitalize' }}>
                  {item.name}
                </strong>
                <div style={{ color: status.color, marginTop: 4 }}>
                  ⚠️ {status.label}
                </div>
                <small>Saved date: {item.expiryDate}</small>
              </div>
            );
          })
        )}
      </section>

      <section style={styles.card}>
        <h2>🧊 My Fridge</h2>

        {activeItems.length === 0 ? (
          <p>Your fridge is empty. Add ingredients to get started!</p>
        ) : (
          activeItems.map((item) => {
            const status = getStatus(item.expiryDate);

            return (
              <div
                key={item.id}
                style={{
                  padding: 12,
                  borderBottom: '1px solid #e5e7eb',
                  display: 'flex',
                  flexWrap: 'wrap',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  gap: 10
                }}
              >
                <div>
                  <strong style={{ textTransform: 'capitalize' }}>
                    {item.name}
                  </strong>
                  <div>Quantity: {item.quantity}</div>
                  <div style={{ color: status.color, fontSize: 13 }}>
                    {status.label}
                  </div>
                  {item.expiryDate && (
                    <small>Date: {item.expiryDate}</small>
                  )}
                </div>

                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                  <button
                    type="button"
                    onClick={() => markUsed(item.id)}
                    style={{ ...styles.button, background: '#2563eb' }}
                  >
                    Mark Used
                  </button>
                  <button
                    type="button"
                    onClick={() => removeItem(item.id)}
                    style={{ ...styles.button, background: '#b91c1c' }}
                  >
                    Remove
                  </button>
                </div>
              </div>
            );
          })
        )}
      </section>

      <section style={styles.card}>
        <h2>🍲 Smart Recipe Suggestions</h2>
        <p>
          Recipes using more of your available ingredients appear first.
          Ingredients nearing their saved dates receive additional priority.
        </p>

        <label htmlFor="recipeSearch">Search recipes</label>
        <input
          id="recipeSearch"
          style={{ ...styles.input, marginTop: 6, marginBottom: 14 }}
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Search by recipe name..."
        />

        {filteredRecipes.length === 0 ? (
          <p>
            No matching recipes found. Try adding more ingredients or a
            different search term.
          </p>
        ) : (
          filteredRecipes.map((recipe) => (
            <article
              key={recipe.name}
              style={{
                border: '1px solid #dce8dd',
                borderRadius: 10,
                padding: 14,
                marginBottom: 12,
                background: '#fff'
              }}
            >
              <h3 style={{ marginTop: 0 }}>{recipe.name}</h3>
              <p style={{ color: '#64748b', fontSize: 13 }}>
                ⏱️ {recipe.time} · {recipe.type}
              </p>

              {recipe.urgentCount > 0 && (
                <p style={{ color: '#b45309' }}>
                  ♻️ Uses {recipe.urgentCount} ingredient(s) nearing their
                  saved date.
                </p>
              )}

              <p>
                <strong>Available:</strong>{' '}
                {recipe.have.join(', ')}
              </p>

              {recipe.missing.length > 0 && (
                <p>
                  <strong>Still needed:</strong>{' '}
                  {recipe.missing.join(', ')}
                </p>
              )}

              <button
                type="button"
                onClick={() => setSelectedRecipe(recipe)}
                style={styles.button}
              >
                View Recipe
              </button>
            </article>
          ))
        )}
      </section>

      {selectedRecipe && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="recipeTitle"
          style={{
            ...styles.card,
            border: '2px solid #18794e'
          }}
        >
          <h2 id="recipeTitle">{selectedRecipe.name}</h2>
          <p>
            <strong>Cooking time:</strong> {selectedRecipe.time}
          </p>
          <p>
            <strong>Ingredients:</strong>{' '}
            {selectedRecipe.items.join(', ')}
          </p>
          <p>
            <strong>Instructions:</strong> {selectedRecipe.instructions}
          </p>

          <p style={{ color: '#64748b', fontSize: 13 }}>
            Check that ingredients are still safe to eat before cooking.
            Saved dates alone cannot determine food safety.
          </p>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            <button
              type="button"
              onClick={() => useRecipe(selectedRecipe)}
              style={styles.button}
            >
              Mark Used Ingredients
            </button>

            <button
              type="button"
              onClick={() => setSelectedRecipe(null)}
              style={{ ...styles.button, background: '#64748b' }}
            >
              Close
            </button>
          </div>
        </div>
      )}

      <footer style={{ textAlign: 'center', padding: 16, color: '#64748b' }}>
        <p>🥗 Fridge to Table — Cook Smart, Waste Less.</p>
        <small>
          Expiry alerts are displayed inside the app while it is open.
          Background phone notifications are not enabled in this version.
        </small>
      </footer>
    </main>
  );
}
