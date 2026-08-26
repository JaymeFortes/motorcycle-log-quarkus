import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { forgotPassword } from "../api/auth";

export function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    try {
      await forgotPassword({ email });
    } finally {
      // Sempre mostra a mesma mensagem, exista ou nao o e-mail - e o mesmo
      // comportamento do backend (nao revelar quais e-mails estao cadastrados).
      setLoading(false);
      setSubmitted(true);
    }
  }

  return (
    <div className="mx-auto mt-16 max-w-sm px-4">
      <h1 className="mb-6 text-2xl font-semibold text-slate-900">Esqueci minha senha</h1>

      {submitted ? (
        <p className="rounded-md bg-slate-50 px-3 py-2 text-sm text-slate-700">
          Se esse e-mail estiver cadastrado, um token de redefinicao foi gerado.
          Em desenvolvimento, o token aparece no log do backend (o envio de
          e-mail e simulado).
        </p>
      ) : (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">E-mail</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full rounded-md border border-slate-300 px-3 py-2 focus:border-slate-500 focus:outline-none"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="rounded-md bg-slate-900 px-4 py-2 text-white hover:bg-slate-800 disabled:opacity-50"
          >
            {loading ? "Enviando..." : "Enviar"}
          </button>
        </form>
      )}

      <p className="mt-4 text-sm text-slate-600">
        <Link to="/reset-password" className="underline">
          Ja tenho um token
        </Link>
        {" · "}
        <Link to="/login" className="underline">
          Voltar ao login
        </Link>
      </p>
    </div>
  );
}
