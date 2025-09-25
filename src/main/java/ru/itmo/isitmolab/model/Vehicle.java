package ru.itmo.isitmolab.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import ru.itmo.isitmolab.dto.CoordinatesDto;
import ru.itmo.isitmolab.dto.VehicleDto;

import java.util.Date;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Embedded
    private Coordinates coordinates;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Date creationDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Positive
    @Column(name = "engine_power")
    private Integer enginePower; // nullable, >0

    @Positive
    @Column(name = "number_of_wheels", nullable = false)
    private int numberOfWheels; // >0

    @Positive
    private Integer capacity; // nullable, >0

    @Positive
    @Column(name = "distance_travelled")
    private Integer distanceTravelled; // nullable, >0

    @Positive
    @Column(name = "fuel_consumption", nullable = false)
    private float fuelConsumption; // >0

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private FuelType fuelType;

    @PrePersist
    void onCreate() {
        if (creationDate == null) {
            creationDate = new Date();
        }
    }

}