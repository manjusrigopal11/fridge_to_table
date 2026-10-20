const addItem = () => {
  const newItems = input
    .split(',')
    .map((i) => i.trim().toLowerCase())
    .filter((i) => i && !fridge.includes(i));
  if (newItems.length) setFridge([...fridge, ...newItems]);
  setInput('');
};
 
