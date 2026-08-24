package org.acme.services;

import java.util.List;

import org.acme.dtos.CreateMotorcycleRequest;
import org.acme.dtos.MotorcycleResponse;
import org.acme.models.Motorcycle;
import org.acme.models.User;
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
}
