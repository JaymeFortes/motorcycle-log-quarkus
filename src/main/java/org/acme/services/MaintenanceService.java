package org.acme.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.acme.dtos.BotMaintenanceRequest;
import org.acme.dtos.CreateMaintenanceRecordRequest;
import org.acme.dtos.MaintenanceRecordResponse;
import org.acme.dtos.MaintenanceTypeResponse;
import org.acme.dtos.UpcomingMaintenanceResponse;
import org.acme.dtos.UpcomingMaintenanceResponse.Status;
import org.acme.models.MaintenanceRecord;
import org.acme.models.MaintenanceType;
import org.acme.models.Motorcycle;
import org.acme.models.User;
import org.acme.repositories.MaintenanceRecordRepository;
import org.acme.repositories.MaintenanceTypeRepository;
import org.acme.repositories.MotorCycleRepository;
import org.acme.repositories.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class MaintenanceService {

    // "Proximo" = falta 10% ou menos do intervalo (ex.: intervalo de 3000km,
    // "proximo" quando faltar <= 300km). Nao especificado na UC10, escolhido
    // como um valor razoavel - ajuste aqui se quiser outro criterio.
    private static final double NEAR_THRESHOLD_RATIO = 0.10;

    @Inject
    MaintenanceRecordRepository maintenanceRecordRepository;

    @Inject
    MaintenanceTypeRepository maintenanceTypeRepository;

    // Reaproveita a checagem de dono que MotorcycleService ja tem, em vez de
    // duplicar "essa moto existe e e do usuario autenticado?" aqui de novo.
    @Inject
    MotorcycleService motorcycleService;

    @Inject
    UserRepository userRepository;

    @Inject
    MotorCycleRepository motorcycleRepository;

    @Transactional
    public void registerFromBot(BotMaintenanceRequest req) {
        // Descobre o usuário pelo Telegram vinculado
        User user = userRepository.findByTelegramChatId(req.chatId())
                .orElseThrow(() -> new WebApplicationException(
                        "Telegram não vinculado a nenhum usuário", Response.Status.FORBIDDEN));

        // Pega a moto do usuário (assumindo uma por usuário no teste)
        Motorcycle motorcycle = motorcycleRepository.findFirstByUser(user)
                .orElseThrow(() -> new WebApplicationException(
                        "Usuário não tem moto cadastrada", Response.Status.NOT_FOUND));

        // Busca o tipo de serviço (reaproveita o repositório que você já tem)
        MaintenanceType maintenanceType = maintenanceTypeRepository
                .findByIdOptional(req.maintenanceTypeId())
                .orElseThrow(() -> new WebApplicationException(
                        "Tipo de manutenção não encontrado", Response.Status.NOT_FOUND));

        MaintenanceRecord record = new MaintenanceRecord();
        record.setMotorcycle(motorcycle);
        record.setMaintenanceType(maintenanceType);
        // MaintenanceRecord.service_date e java.util.Date (mesmo tipo usado
        // em CreateMaintenanceRecordRequest); o DTO do bot usa LocalDate
        // (mais moderno), entao precisa converter - java.sql.Date.valueOf
        // aceita LocalDate direto e ja e um java.util.Date por heranca.
        record.setService_date(java.sql.Date.valueOf(req.serviceDate()));
        record.setOdometer_km(req.odometerKm());
        record.setEngine_hours(req.engineHours());
        // Mesma historia: cost aqui e BigDecimal, mas o campo na entidade e
        // Double - converte explicitamente.
        record.setCost(req.cost() != null ? req.cost().doubleValue() : null);
        record.setNotes(req.notes());

        maintenanceRecordRepository.persist(record);
    }

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

    // GET /maintenance-types: catalogo fixo (seed via import.sql), so leitura.
    public List<MaintenanceTypeResponse> listMaintenanceTypes() {
        return maintenanceTypeRepository.listAll().stream()
                .map(MaintenanceTypeResponse::from)
                .toList();
    }

    public List<MaintenanceRecordResponse> listMaintenances(Long motorcycleId) {
        Motorcycle motorcycle = motorcycleService.getOwnedMotorcycleOrThrow(motorcycleId);

        return maintenanceRecordRepository.findByMotorcycle(motorcycle).stream()
                .map(MaintenanceRecordResponse::from)
                .toList();
    }

    /**
     * UC10 - Proximas manutencoes previstas. Para cada MaintenanceType com
     * intervalo definido, calcula (ultima manutencao desse tipo + intervalo)
     * e compara com o km/horas atuais da moto, devolvendo so os que estao
     * proximos ou vencidos (os que ainda tem folga nao aparecem na lista).
     */
    public List<UpcomingMaintenanceResponse> listUpcomingMaintenances(Long motorcycleId) {
        Motorcycle motorcycle = motorcycleService.getOwnedMotorcycleOrThrow(motorcycleId);

        List<UpcomingMaintenanceResponse> upcoming = new ArrayList<>();

        for (MaintenanceType type : maintenanceTypeRepository.listAll()) {
            calculateUpcoming(motorcycle, type).ifPresent(upcoming::add);
        }

        return upcoming;
    }

    // Optional vazio significa "esse tipo nao entra na lista" - por 3 motivos
    // possiveis: sem intervalo definido, sem manutencao anterior desse tipo
    // nessa moto (nao ha "ultima manutencao" pra somar o intervalo), ou tem
    // folga suficiente (nem proximo, nem vencido).
    private Optional<UpcomingMaintenanceResponse> calculateUpcoming(Motorcycle motorcycle, MaintenanceType type) {
        boolean hasKmInterval = type.getInterval_km() != null;
        boolean hasHoursInterval = type.getInterval_engine_hours() != null;
        if (!hasKmInterval && !hasHoursInterval) {
            return Optional.empty();
        }

        Optional<MaintenanceRecord> lastRecord = maintenanceRecordRepository
                .findLatestByMotorcycleAndType(motorcycle, type);
        if (lastRecord.isEmpty()) {
            return Optional.empty();
        }

        // dueAt = km/horas em que o PROXIMO servico desse tipo devia acontecer.
        // remaining = quanto falta pra chegar la (negativo = ja passou = vencido).
        Integer dueAtKm = null;
        Integer remainingKm = null;
        if (hasKmInterval && lastRecord.get().getOdometer_km() != null && motorcycle.getCurrent_km() != null) {
            dueAtKm = lastRecord.get().getOdometer_km() + type.getInterval_km();
            remainingKm = dueAtKm - motorcycle.getCurrent_km();
        }

        Double dueAtEngineHours = null;
        Double remainingEngineHours = null;
        if (hasHoursInterval && lastRecord.get().getEngine_hours() != null
                && motorcycle.getCurrent_engine_hours() != null) {
            dueAtEngineHours = lastRecord.get().getEngine_hours() + type.getInterval_engine_hours();
            remainingEngineHours = dueAtEngineHours - motorcycle.getCurrent_engine_hours();
        }

        if (remainingKm == null && remainingEngineHours == null) {
            // Tinha intervalo e historico, mas faltou o dado necessario pra
            // calcular (ex.: moto sem current_km preenchido) - nao da pra dizer nada.
            return Optional.empty();
        }

        boolean overdueByKm = remainingKm != null && remainingKm <= 0;
        boolean overdueByHours = remainingEngineHours != null && remainingEngineHours <= 0;
        boolean nearByKm = remainingKm != null
                && remainingKm <= type.getInterval_km() * NEAR_THRESHOLD_RATIO;
        boolean nearByHours = remainingEngineHours != null
                && remainingEngineHours <= type.getInterval_engine_hours() * NEAR_THRESHOLD_RATIO;

        Status status;
        if (overdueByKm || overdueByHours) {
            status = Status.OVERDUE;
        } else if (nearByKm || nearByHours) {
            status = Status.NEAR;
        } else {
            // Tem folga em ambos os criterios (ou no unico que se aplica) - nao entra na
            // lista.
            return Optional.empty();
        }

        return Optional.of(new UpcomingMaintenanceResponse(
                type.getId(),
                type.getName(),
                dueAtKm,
                remainingKm,
                dueAtEngineHours,
                remainingEngineHours,
                status));
    }
}
