package ru.itmo.isitmolab.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle")
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
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "coordinates_x", nullable = false)),
            @AttributeOverride(name = "y", column = @Column(name = "coordinates_y", nullable = false))
    })
    private Coordinates coordinates;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vehicle_admin"))
    private Admin createdBy;

    @PrePersist
    void onCreate() {
        if (creationDateTime == null) {
            creationDateTime = LocalDateTime.now();
        }
    }

}