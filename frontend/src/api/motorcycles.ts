import { apiRequest } from "./client";
import type { CreateMotorcycleRequest, MotorcycleResponse } from "../types/dtos";

export function listMotorcycles(): Promise<MotorcycleResponse[]> {
  return apiRequest<MotorcycleResponse[]>("/motorcycles");
}

export function createMotorcycle(request: CreateMotorcycleRequest): Promise<MotorcycleResponse> {
  return apiRequest<MotorcycleResponse>("/motorcycles", { method: "POST", body: request });
}

// PUT espera o objeto inteiro (mesmo DTO do create) - substituicao total.
export function updateMotorcycle(id: number, request: CreateMotorcycleRequest): Promise<MotorcycleResponse> {
  return apiRequest<MotorcycleResponse>(`/motorcycles/${id}`, { method: "PUT", body: request });
}

export function deleteMotorcycle(id: number): Promise<void> {
  return apiRequest<void>(`/motorcycles/${id}`, { method: "DELETE" });
}
