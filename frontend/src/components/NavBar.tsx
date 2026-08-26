import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export function NavBar() {
  const { isAuthenticated, email, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <nav className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-3">
      <Link to="/motorcycles" className="text-lg font-semibold text-slate-900">
        MotoLog
      </Link>

      {isAuthenticated && (
        <div className="flex items-center gap-4 text-sm text-slate-600">
          <span>{email}</span>
          <button
            onClick={handleLogout}
            className="rounded-md border border-slate-300 px-3 py-1.5 text-slate-700 hover:bg-slate-50"
          >
            Sair
          </button>
        </div>
      )}
    </nav>
  );
}
