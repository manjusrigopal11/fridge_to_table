import React from 'react';
import { createRoot } from 'react-dom/client';

function App() {
return (
<div style={{
fontFamily: 'Arial, sans-serif',
textAlign: 'center',
padding: '50px 20px',
backgroundColor: '#f0fff4',
minHeight: '100vh',
boxSizing: 'border-box'
}}>
<h1 style={{ color: '#218838' }}>🥗 Fridge to Table</h1>
<h2>Welcome!</h2>
<p>Make delicious meals with the ingredients in your fridge.</p>
</div>
);
}

createRoot(document.getElementById('root')).render(<App />);
