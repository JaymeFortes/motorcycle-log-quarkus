package org.acme.models;

import io.smallrye.common.constraint.NotNull;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "maintenance_types")
public class MaintenanceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    private Integer interval_km;

    private Double interval_engine_hours;

    public MaintenanceType() {
    }

    public MaintenanceType(Long id, @NotNull String name, Integer interval_km, Double interval_engine_hours) {
        this.id = id;
        this.name = name;
        this.interval_km = interval_km;
        this.interval_engine_hours = interval_engine_hours;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getInterval_km() {
        return interval_km;
    }

    public void setInterval_km(Integer interval_km) {
        this.interval_km = interval_km;
    }

    public Double getInterval_engine_hours() {
        return interval_engine_hours;
    }

    public void setInterval_engine_hours(Double interval_engine_hours) {
        this.interval_engine_hours = interval_engine_hours;
    }
}
