package org.acme.models;

import java.time.Instant;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PastOrPresent(message = "service_date nao pode ser uma data futura")
    private Date service_date;

    private Integer odometer_km; // km no momento do serviço

    private Double engine_hours; // horas no momento do serviço

    private Double cost; // custo do serviço

    private String notes; // peças usadas e tals

    @NotNull
    private Instant created_at = Instant.now(); // data de criação do registro

    // Motorcycle 1:N MaintenanceRecord: varias manutencoes apontam pra uma
    // moto so - mesmo padrao de Motorcycle.owner (User) que ja usamos.
    @ManyToOne(optional = false)
    @JoinColumn(name = "motorcycle_id", nullable = false)
    private Motorcycle motorcycle;

    // MaintenanceType 1:N MaintenanceRecord: varios registros podem ser do
    // mesmo tipo (ex.: varias "trocas de oleo" ao longo do tempo).
    @ManyToOne(optional = false)
    @JoinColumn(name = "maintenance_type_id", nullable = false)
    private MaintenanceType maintenanceType;

    public MaintenanceRecord(Long id,
            @NotNull @PastOrPresent(message = "service_date nao pode ser uma data futura") Date service_date,
            Integer odometer_km, Double engine_hours, Double cost, String notes, @NotNull Instant created_at) {
        this.id = id;
        this.service_date = service_date;
        this.odometer_km = odometer_km;
        this.engine_hours = engine_hours;
        this.cost = cost;
        this.notes = notes;
        this.created_at = created_at;
    }

    public MaintenanceRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getService_date() {
        return service_date;
    }

    public void setService_date(Date service_date) {
        this.service_date = service_date;
    }

    public Integer getOdometer_km() {
        return odometer_km;
    }

    public void setOdometer_km(Integer odometer_km) {
        this.odometer_km = odometer_km;
    }

    public Double getEngine_hours() {
        return engine_hours;
    }

    public void setEngine_hours(Double engine_hours) {
        this.engine_hours = engine_hours;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public Motorcycle getMotorcycle() {
        return motorcycle;
    }

    public void setMotorcycle(Motorcycle motorcycle) {
        this.motorcycle = motorcycle;
    }

    public MaintenanceType getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(MaintenanceType maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

}
