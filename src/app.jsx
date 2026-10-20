import React, { useState } from 'react';

const RECIPES = [
  { name: 'Tomato Omelette', items: ['egg', 'tomato', 'onion'] },
  { name: 'Veg Fried Rice', items: ['rice', 'carrot', 'onion', 'beans'] },
  { name: 'Potato Curry', items: ['potato', 'onion', 'tomato'] },
  { name: 'Paneer Bhurji', items: ['paneer', 'onion', 'tomato'] },
  { name: 'Curd Rice', items: ['rice', 'curd'] },
];

export default function App() {
  const [input, setInput] = useState('');
  const [fridge, setFridge] = useState([]);

  const addItem = () => {
    const item = input.trim().toLowerCase();
    if (item && !fridge.includes(item)) setFridge([...fridge, item]);
    setInput('');
  };

  const removeItem = (item) => setFridge(fridge.filter((i) => i !== item));

  const matches = RECIPES.map((r) => {
    const have = r.items.filter((i) => fridge.includes(i));
    return { ...r, have, missing: r.items.filter((i) => !fridge.includes(i)) };
  })
    .filter((r) => r.have.length > 0)
    .sort((a, b) => b.have.length - a.have.length);

  return (
    <div style={{ maxWidth: 600, margin: '40px auto', padding: 16, fontFamily: 'sans-serif' }}>
      <h1>🥗 Fridge to Table</h1>
      <p>Fridge-la irukkura items add pannunga, recipe suggest pannum.</p>

      <div style={{ display: 'flex', gap: 8 }}>
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && addItem()}
          placeholder="e.g. tomato"
          style={{ flex: 1, padding: 8 }}
        />
        <button onClick={addItem} style={{ padding: '8px 16px' }}>Add</button>
      </div>

      <div style={{ margin: '16px 0' }}>
        {fridge.map((item) => (
          <span
            key={item}
            onClick={() => removeItem(item)}
            style={{ display: 'inline-block', background: '#e6f4ea', padding: '4px 10px', margin: 4, borderRadius: 12, cursor: 'pointer' }}
          >
            {item} ✕
          </span>
        ))}
      </div>

      <h2>Recipes</h2>
      {fridge.length === 0 && <p>Munnadi items add pannunga.</p>}
      {matches.map((r) => (
        <div key={r.name} style={{ border: '1px solid #ddd', borderRadius: 8, padding: 12, marginBottom: 8 }}>
          <strong>{r.name}</strong>
          <div>✅ Irukku: {r.have.join(', ')}</div>
          {r.missing.length > 0 && <div>❌ Thevai: {r.missing.join(', ')}</div>}
        </div>
      ))}
    </div>
  );
}
