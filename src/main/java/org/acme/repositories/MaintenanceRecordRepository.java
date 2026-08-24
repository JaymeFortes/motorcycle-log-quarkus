package org.acme.repositories;

import java.util.List;
import java.util.Optional;

import org.acme.models.MaintenanceRecord;
import org.acme.models.MaintenanceType;
import org.acme.models.Motorcycle;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MaintenanceRecordRepository implements PanacheRepository<MaintenanceRecord> {

    public List<MaintenanceRecord> findByMotorcycle(Motorcycle motorcycle) {
        return find("motorcycle", motorcycle).list();
    }

    // Usado no calculo de "proximas manutencoes" (UC10): pega o registro mais
    // recente de um tipo especifico, nessa moto especifica, pra servir de
    // base ("ultima manutencao + intervalo").
    public Optional<MaintenanceRecord> findLatestByMotorcycleAndType(Motorcycle motorcycle, MaintenanceType type) {
        return find("motorcycle = ?1 and maintenanceType = ?2 order by service_date desc", motorcycle, type)
                .firstResultOptional();
    }
}
