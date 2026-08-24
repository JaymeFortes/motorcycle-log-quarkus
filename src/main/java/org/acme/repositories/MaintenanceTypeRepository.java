package org.acme.repositories;

import org.acme.models.MaintenanceType;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MaintenanceTypeRepository implements PanacheRepository<MaintenanceType> {
}
