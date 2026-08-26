import { createContext, useContext, useState, type ReactNode } from "react";
import { clearToken, getToken, setToken as saveToken } from "../api/client";

interface AuthContextValue {
  token: string | null;
  email: string | null;
  isAuthenticated: boolean;
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

// O JWT guarda o e-mail no claim "sub" (ver AuthService.login no backend).
// Decodifica so a parte do payload (base64url), sem validar assinatura -
// isso e so pra mostrar "logado como X" na tela, a validacao de verdade
// e sempre feita pelo backend em cada requisicao.
function decodeEmailFromToken(token: string): string | null {
  try {
    const payload = token.split(".")[1];
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    const claims = JSON.parse(json);
    return typeof claims.sub === "string" ? claims.sub : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(() => getToken());

  function login(newToken: string) {
    saveToken(newToken);
    setTokenState(newToken);
  }

  function logout() {
    clearToken();
    setTokenState(null);
  }

  const value: AuthContextValue = {
    token,
    email: token ? decodeEmailFromToken(token) : null,
    isAuthenticated: token !== null,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth precisa ser usado dentro de um AuthProvider");
  }
  return context;
}
