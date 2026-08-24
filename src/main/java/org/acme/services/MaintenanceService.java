package org.acme.services;

import java.util.List;

import org.acme.dtos.CreateMaintenanceRecordRequest;
import org.acme.dtos.MaintenanceRecordResponse;
import org.acme.models.MaintenanceRecord;
import org.acme.models.MaintenanceType;
import org.acme.models.Motorcycle;
import org.acme.repositories.MaintenanceRecordRepository;
import org.acme.repositories.MaintenanceTypeRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class MaintenanceService {

    @Inject
    MaintenanceRecordRepository maintenanceRecordRepository;

    @Inject
    MaintenanceTypeRepository maintenanceTypeRepository;

    // Reaproveita a checagem de dono que MotorcycleService ja tem, em vez de
    // duplicar "essa moto existe e e do usuario autenticado?" aqui de novo.
    @Inject
    MotorcycleService motorcycleService;

    /**
     * UC08 - Registrar manutencao. Fluxo: escolhe moto (motorcycleId, vem da
     * URL /motorcycles/{id}/maintenances) e tipo de servico, informa data,
     * km/horas e custo -> valida (data nao futura, moto e do usuario) ->
     * persiste -> 201.
     */
    @Transactional
    public MaintenanceRecordResponse registerMaintenance(Long motorcycleId, CreateMaintenanceRecordRequest request) {
        // getOwnedMotorcycleOrThrow ja cobre as duas validacoes da UC08
        // ligadas a moto: ela existe, E pertence a quem esta autenticado
        // (404 pros dois casos, sem distinguir - mesmo padrao usado em
        // update/delete de moto).
        Motorcycle motorcycle = motorcycleService.getOwnedMotorcycleOrThrow(motorcycleId);

        MaintenanceType maintenanceType = maintenanceTypeRepository.findByIdOptional(request.maintenanceTypeId())
                .orElseThrow(() -> new WebApplicationException(
                        "Tipo de manutencao nao encontrado", Response.Status.NOT_FOUND));

        // A validacao de "data nao futura" ja acontece no @PastOrPresent do
        // CreateMaintenanceRecordRequest (via @Valid no resource) - chegando
        // aqui, o service_date ja passou por essa checagem.
        MaintenanceRecord record = new MaintenanceRecord();
        record.setMotorcycle(motorcycle);
        record.setMaintenanceType(maintenanceType);
        record.setService_date(request.serviceDate());
        record.setOdometer_km(request.odometerKm());
        record.setEngine_hours(request.engineHours());
        record.setCost(request.cost());
        record.setNotes(request.notes());

        maintenanceRecordRepository.persist(record);

        return MaintenanceRecordResponse.from(record);
    }

    public List<MaintenanceRecordResponse> listMaintenances(Long motorcycleId) {
        Motorcycle motorcycle = motorcycleService.getOwnedMotorcycleOrThrow(motorcycleId);

        return maintenanceRecordRepository.findByMotorcycle(motorcycle).stream()
                .map(MaintenanceRecordResponse::from)
                .toList();
    }
}
