const BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
const TOKEN_KEY = "motolog_token";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

// Mensagens genericas por status: hoje boa parte dos erros do backend
// (WebApplicationException(String, Status)) NAO manda a mensagem no corpo
// da resposta - so o status HTTP chega de verdade. Ver conversa sobre iso;
// se um dia o backend passar a mandar o corpo, esses defaults viram fallsback.
const DEFAULT_MESSAGES: Record<number, string> = {
  400: "Requisicao invalida.",
  401: "Credenciais invalidas ou sessao expirada.",
  403: "Voce nao tem permissao para isso.",
  404: "Nao encontrado.",
  409: "Ja existe um registro com esses dados.",
  422: "Dados invalidos.",
};

async function extractErrorMessage(response: Response): Promise<string> {
  try {
    const body = await response.clone().json();
    if (Array.isArray(body?.violations)) {
      return body.violations.join(", ");
    }
    if (typeof body?.message === "string") {
      return body.message;
    }
  } catch {
    // corpo vazio ou nao-JSON: cai no fallback abaixo.
  }
  return DEFAULT_MESSAGES[response.status] ?? `Erro inesperado (status ${response.status}).`;
}

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  auth?: boolean; // default true - a maioria das rotas exige token
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, auth = true } = options; // default method GET, default auth true

  const headers: Record<string, string> = {};
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (auth) {
    const token = getToken();
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!response.ok) {
    throw new ApiError(response.status, await extractErrorMessage(response));
  }

  // 204 No Content (ex.: DELETE) ou corpo vazio em geral.
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}
