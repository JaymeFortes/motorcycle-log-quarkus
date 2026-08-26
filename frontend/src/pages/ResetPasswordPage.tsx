import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { resetPassword } from "../api/auth";
import { ApiError } from "../api/client";

export function ResetPasswordPage() {
  const navigate = useNavigate();
  const [token, setToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await resetPassword({ token, newPassword });
      navigate("/login", { state: { justRegistered: true } });
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "Nao foi possivel redefinir a senha.",
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto mt-16 max-w-sm px-4">
      <h1 className="mb-6 text-2xl font-semibold text-slate-900">Redefinir senha</h1>

      <p className="mb-4 text-sm text-slate-600">
        Cole abaixo o token gerado por "Esqueci minha senha" - em dev, ele
        aparece no log do backend (o e-mail e simulado, nao chega de
        verdade).
      </p>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Token</label>
          <input
            type="text"
            required
            value={token}
            onChange={(e) => setToken(e.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 font-mono text-sm focus:border-slate-500 focus:outline-none"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Nova senha (minimo 8 caracteres)</label>
          <input
            type="password"
            required
            minLength={8}
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            className="w-full rounded-md border border-slate-300 px-3 py-2 focus:border-slate-500 focus:outline-none"
          />
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="rounded-md bg-slate-900 px-4 py-2 text-white hover:bg-slate-800 disabled:opacity-50"
        >
          {loading ? "Redefinindo..." : "Redefinir senha"}
        </button>
      </form>

      <p className="mt-4 text-sm text-slate-600">
        <Link to="/login" className="underline">
          Voltar ao login
        </Link>
      </p>
    </div>
  );
}
