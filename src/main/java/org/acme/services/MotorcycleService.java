package org.acme.services;

import java.util.List;

import org.acme.dtos.CreateMotorcycleRequest;
import org.acme.dtos.MotorcycleResponse;
import org.acme.dtos.UpdateMotorcycleRequest;
import org.acme.models.Motorcycle;
import org.acme.models.User;
import org.acme.repositories.MaintenanceRecordRepository;
import org.acme.repositories.MotorCycleRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class MotorcycleService {

    @Inject
    UserService userService;

    @Inject
    MotorCycleRepository motorCycleRepository;

    @Inject
    MaintenanceRecordRepository maintenanceRecordRepository;

    @Transactional
    public MotorcycleResponse createMotorcycle(CreateMotorcycleRequest request) {

        User owner = userService.getAuthenticatedUser();

        if (motorCycleRepository.findByPlate(request.plate()).isPresent()) {
            throw new WebApplicationException("Placa ja cadastrada", Response.Status.CONFLICT);
        }

        Motorcycle motorcycle = new Motorcycle(
                request.brand(),
                request.model(),
                request.modelYear(),
                request.plate(),
                request.currentKm(),
                request.currentEngineHours(),
                owner);

        motorCycleRepository.persist(motorcycle);

        return MotorcycleResponse.from(motorcycle);
    }

    // Nao existe um "listAll()" aqui de proposito: sempre filtrado pelo dono
    // autenticado, nunca a tabela inteira - e essa a "autorizacao por dono"
    // do CLAUDE.md aplicada em consulta, nao so na criacao.
    public List<MotorcycleResponse> listMyMotorcycles() {
        User owner = userService.getAuthenticatedUser();

        return motorCycleRepository.listByOwner(owner).stream()
                .map(MotorcycleResponse::from)
                .toList();
    }

    // PUT: substitui o recurso inteiro - todos os campos de request sao
    // aplicados, sem checagem de null (CreateMotorcycleRequest exige todos
    // preenchidos via @NotBlank/@NotNull, entao aqui sempre chega completo).
    @Transactional
    public MotorcycleResponse updateMotorcycle(Long id, CreateMotorcycleRequest request) {
        Motorcycle motorcycle = getOwnedMotorcycleOrThrow(id);

        ensurePlateIsFreeForUpdate(request.plate(), motorcycle.getId());

        motorcycle.setBrand(request.brand());
        motorcycle.setModel(request.model());
        motorcycle.setModel_year(request.modelYear());
        motorcycle.setPlate(request.plate());
        motorcycle.setCurrent_km(request.currentKm());
        motorcycle.setCurrent_engine_hours(request.currentEngineHours());

        return MotorcycleResponse.from(motorcycle);
    }

    // PATCH: so aplica os campos que vieram preenchidos em request - null
    // significa "deixa como esta". Por isso cada campo tem seu proprio if.
    @Transactional
    public MotorcycleResponse patchMotorcycle(Long id, UpdateMotorcycleRequest request) {
        Motorcycle motorcycle = getOwnedMotorcycleOrThrow(id);

        if (request.plate() != null) {
            ensurePlateIsFreeForUpdate(request.plate(), motorcycle.getId());
            motorcycle.setPlate(request.plate());
        }
        if (request.brand() != null) {
            motorcycle.setBrand(request.brand());
        }
        if (request.model() != null) {
            motorcycle.setModel(request.model());
        }
        if (request.modelYear() != null) {
            motorcycle.setModel_year(request.modelYear());
        }
        if (request.currentKm() != null) {
            motorcycle.setCurrent_km(request.currentKm());
        }
        if (request.currentEngineHours() != null) {
            motorcycle.setCurrent_engine_hours(request.currentEngineHours());
        }

        return MotorcycleResponse.from(motorcycle);
    }

    @Transactional
    public void deleteMotorcycle(Long id) {
        Motorcycle motorcycle = getOwnedMotorcycleOrThrow(id);

        // Sem isso, o Postgres rejeita o delete com um erro de FK constraint
        // sempre que a moto tiver alguma manutencao registrada (maintenance_records
        // ainda aponta pra ela) - apaga o historico junto, de proposito: excluir
        // a moto assumindo que o dono tambem quer descartar o historico dela.
        maintenanceRecordRepository.delete("motorcycle", motorcycle);

        motorCycleRepository.delete(motorcycle);
    }

    // Busca a moto e confirma que ela pertence ao usuario autenticado - essa
    // e a "autorizacao por dono" do CLAUDE.md aplicada em update/delete, nao
    // so em criacao/listagem. Moto de outro usuario da o MESMO 404 de "nao
    // existe": nao revelamos que o id pertence a outra pessoa.
    // Publico (nao mais private) porque MaintenanceService reaproveita essa
    // mesma checagem ao registrar uma manutencao (UC08 exige "moto e do usuario").
    public Motorcycle getOwnedMotorcycleOrThrow(Long id) {
        User owner = userService.getAuthenticatedUser();

        Motorcycle motorcycle = motorCycleRepository.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Moto nao encontrada", Response.Status.NOT_FOUND));

        if (!motorcycle.getOwner().getId().equals(owner.getId())) {
            throw new WebApplicationException("Moto nao encontrada", Response.Status.NOT_FOUND);
        }

        return motorcycle;
    }

    // Mesma checagem de placa duplicada do create, mas ignorando a propria
    // moto - senao, atualizar uma moto reenviando a placa que ela ja tem
    // dispararia 409 contra si mesma.
    private void ensurePlateIsFreeForUpdate(String plate, Long currentMotorcycleId) {
        motorCycleRepository.findByPlate(plate)
                .filter(existing -> !existing.getId().equals(currentMotorcycleId))
                .ifPresent(existing -> {
                    throw new WebApplicationException("Placa ja cadastrada", Response.Status.CONFLICT);
                });
    }
}
