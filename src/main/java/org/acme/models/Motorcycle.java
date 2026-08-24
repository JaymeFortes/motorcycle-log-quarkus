package org.acme.models;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "motorcycles")
public class Motorcycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    private Integer model_year;

    @Column(nullable = false, unique = true)
    private String plate;
    private Integer current_km;
    private Double current_engine_hours;

    @Column(nullable = false)
    private Instant created_at = Instant.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public Motorcycle() {
    }

    public Motorcycle(String brand, String model, Integer model_year, String plate, Integer current_km,
            Double current_engine_hours, User owner) {
        this.brand = brand;
        this.model = model;
        this.model_year = model_year;
        this.plate = plate;
        this.current_km = current_km;
        this.current_engine_hours = current_engine_hours;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getModel_year() {
        return model_year;
    }

    public void setModel_year(Integer model_year) {
        this.model_year = model_year;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public Integer getCurrent_km() {
        return current_km;
    }

    public void setCurrent_km(Integer current_km) {
        this.current_km = current_km;
    }

    public Double getCurrent_engine_hours() {
        return current_engine_hours;
    }

    public void setCurrent_engine_hours(Double current_engine_hours) {
        this.current_engine_hours = current_engine_hours;
    }

    public Instant getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Instant created_at) {
        this.created_at = created_at;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    } 
}
