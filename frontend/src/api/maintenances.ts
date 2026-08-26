import { apiRequest } from "./client";
import type {
  CreateMaintenanceRecordRequest,
  MaintenanceRecordResponse,
  MaintenanceTypeResponse,
  UpcomingMaintenanceResponse,
} from "../types/dtos";

export function listMaintenanceTypes(): Promise<MaintenanceTypeResponse[]> {
  return apiRequest<MaintenanceTypeResponse[]>("/maintenance-types");
}

export function listMaintenances(motorcycleId: number): Promise<MaintenanceRecordResponse[]> {
  return apiRequest<MaintenanceRecordResponse[]>(`/motorcycles/${motorcycleId}/maintenances`);
}

export function createMaintenance(
  motorcycleId: number,
  request: CreateMaintenanceRecordRequest,
): Promise<MaintenanceRecordResponse> {
  return apiRequest<MaintenanceRecordResponse>(`/motorcycles/${motorcycleId}/maintenances`, {
    method: "POST",
    body: request,
  });
}

export function listUpcomingMaintenances(motorcycleId: number): Promise<UpcomingMaintenanceResponse[]> {
  return apiRequest<UpcomingMaintenanceResponse[]>(`/motorcycles/${motorcycleId}/maintenances/upcoming`);
}
