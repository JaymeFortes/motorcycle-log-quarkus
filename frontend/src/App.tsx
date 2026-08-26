import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { ProtectedRoute } from "./components/ProtectedRoute";
import { NavBar } from "./components/NavBar";
import { RegisterPage } from "./pages/RegisterPage";
import { LoginPage } from "./pages/LoginPage";
import { ForgotPasswordPage } from "./pages/ForgotPasswordPage";
import { ResetPasswordPage } from "./pages/ResetPasswordPage";
import { MotorcyclesPage } from "./pages/MotorcyclesPage";
import { MotorcycleMaintenancesPage } from "./pages/MotorcycleMaintenancesPage";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="min-h-screen bg-slate-100">
          <NavBar />
          <Routes>
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route
              path="/motorcycles"
              element={
                <ProtectedRoute>
                  <MotorcyclesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/motorcycles/:id/maintenances"
              element={
                <ProtectedRoute>
                  <MotorcycleMaintenancesPage />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/motorcycles" replace />} />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}
