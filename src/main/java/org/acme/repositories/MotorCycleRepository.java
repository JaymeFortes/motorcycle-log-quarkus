package org.acme.repositories;

import java.util.Optional;

import org.acme.models.Motorcycle;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MotorCycleRepository implements PanacheRepository<Motorcycle> {

    public Optional<Motorcycle> findByPlate(String plate) {
        return find("plate", plate).firstResultOptional();
    }
}
