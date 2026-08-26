// Espelha os records em src/main/java/org/acme/dtos/ do backend. Mesmos
// nomes de campo (camelCase) porque e assim que o Jackson serializa.

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface UserResponse {
  id: number;
  name: string;
  email: string;
  role: string;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface CreateMotorcycleRequest {
  brand: string;
  model: string;
  modelYear: number;
  plate: string;
  currentKm: number;
  currentEngineHours: number;
}

export interface MotorcycleResponse {
  id: number;
  brand: string;
  model: string;
  modelYear: number;
  plate: string;
  currentKm: number;
  currentEngineHours: number;
  createdAt: string;
  ownerId: number;
}

export interface MaintenanceTypeResponse {
  id: number;
  name: string;
  intervalKm: number | null;
  intervalEngineHours: number | null;
}

export interface CreateMaintenanceRecordRequest {
  maintenanceTypeId: number;
  serviceDate: string; // ISO datetime, ex.: "2026-08-20T10:00:00.000Z"
  odometerKm: number | null;
  engineHours: number | null;
  cost: number | null;
  notes: string | null;
}

export interface MaintenanceRecordResponse {
  id: number;
  motorcycleId: number;
  maintenanceTypeId: number;
  maintenanceTypeName: string;
  serviceDate: string;
  odometerKm: number | null;
  engineHours: number | null;
  cost: number | null;
  notes: string | null;
  createdAt: string;
}

export type UpcomingMaintenanceStatus = "NEAR" | "OVERDUE";

export interface UpcomingMaintenanceResponse {
  maintenanceTypeId: number;
  maintenanceTypeName: string;
  dueAtKm: number | null;
  remainingKm: number | null;
  dueAtEngineHours: number | null;
  remainingEngineHours: number | null;
  status: UpcomingMaintenanceStatus;
}
