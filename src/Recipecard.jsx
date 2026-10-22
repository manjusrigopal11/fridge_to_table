import { useState } from "react";

export default function RecipeCard({ recipe }) {
  const [open, setOpen] = useState(false);

  return (
    <div style={{ border: "1px solid #ddd", borderRadius: 12, padding: 12, margin: 8 }}>
      <h3>{recipe.title}</h3>
      <button onClick={() => setOpen(true)}>View Recipe</button>

      {open && (
        <div
          onClick={() => setOpen(false)}
          style={{
            position: "fixed", inset: 0, background: "rgba(0,0,0,0.5)",
            display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1000,
          }}
        >
          <div
            onClick={(e) => e.stopPropagation()}
            style={{
              background: "#fff", color: "#000", padding: 20, borderRadius: 12,
              maxWidth: 420, maxHeight: "80vh", overflowY: "auto",
            }}
          >
            <h2>{recipe.title}</h2>
            <h4>Ingredients</h4>
            <ul>
              {(recipe.ingredients || []).map((item, i) => (
                <li key={i}>{typeof item === "string" ? item : item.name}</li>
              ))}
            </ul>
            <h4>Steps</h4>
            <ol>
              {(recipe.steps || recipe.instructions || []).map((s, i) => (
                <li key={i}>{s}</li>
              ))}
            </ol>
            <button onClick={() => setOpen(false)}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
}
