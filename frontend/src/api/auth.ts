import { apiRequest } from "./client";
import type {
  ForgotPasswordRequest,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ResetPasswordRequest,
  UserResponse,
} from "../types/dtos";

// auth: false nessas quatro - sao as unicas rotas que nao exigem JWT.
export function register(request: RegisterRequest): Promise<UserResponse> {
  return apiRequest<UserResponse>("/auth/register", { method: "POST", body: request, auth: false });
}

export function login(request: LoginRequest): Promise<LoginResponse> {
  return apiRequest<LoginResponse>("/auth/login", { method: "POST", body: request, auth: false });
}

export function forgotPassword(request: ForgotPasswordRequest): Promise<void> {
  return apiRequest<void>("/auth/forgot-password", { method: "POST", body: request, auth: false });
}

export function resetPassword(request: ResetPasswordRequest): Promise<void> {
  return apiRequest<void>("/auth/reset-password", { method: "POST", body: request, auth: false });
}
