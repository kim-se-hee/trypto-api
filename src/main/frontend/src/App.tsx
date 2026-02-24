import { Routes, Route, Navigate } from "react-router-dom";
import { MarketPage } from "@/pages/MarketPage";

function App() {
  return (
    <Routes>
      <Route path="/market" element={<MarketPage />} />
      <Route path="*" element={<Navigate to="/market" replace />} />
    </Routes>
  );
}

export default App;
