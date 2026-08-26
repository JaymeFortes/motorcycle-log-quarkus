package org.acme.repositories;

import java.util.List;
import java.util.Optional;

import org.acme.models.Motorcycle;
import org.acme.models.User;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MotorCycleRepository implements PanacheRepository<Motorcycle> {

    public Optional<Motorcycle> findByPlate(String plate) {
        return find("plate", plate).firstResultOptional();
    }

    public List<Motorcycle> listByOwner(User owner) {
        return find("owner", owner).list();
    }

    public Optional<Motorcycle> findFirstByUser(User user) {
        return find("owner", user).firstResultOptional();
    }
}
