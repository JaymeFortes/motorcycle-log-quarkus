package org.acme.repositories;

import java.util.List;

import org.acme.models.MaintenanceRecord;
import org.acme.models.Motorcycle;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MaintenanceRecordRepository implements PanacheRepository<MaintenanceRecord> {

    public List<MaintenanceRecord> findByMotorcycle(Motorcycle motorcycle) {
        return find("motorcycle", motorcycle).list();
    }
    
}
