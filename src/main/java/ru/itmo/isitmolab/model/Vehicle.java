package ru.itmo.isitmolab.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@NamedEntityGraph(
        name = "Vehicle.withOwnerAdmin",
        attributeNodes = {
                @NamedAttributeNode("owner"),
                @NamedAttributeNode("admin")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicle")
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

    @Column(name = "creation_time", nullable = false, updatable = false, columnDefinition = "timestamp default now()")
    private LocalDateTime creationTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Positive
    @Column(name = "engine_power")
    private Integer enginePower;

    @Positive
    @Column(name = "number_of_wheels", nullable = false)
    private int numberOfWheels;

    @Positive
    private Integer capacity;

    @Positive
    @Column(name = "distance_travelled")
    private Integer distanceTravelled;

    @Positive
    @Column(name = "fuel_consumption", nullable = false)
    private float fuelConsumption;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private FuelType fuelType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false, foreignKey = @ForeignKey(name = "vehicle_admin_id_fkey"))
    private Admin admin;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "vehicle_owner_id_fkey"))
    private Person owner;

    @PrePersist
    void onCreate() {
        if (creationTime == null) {
            creationTime = LocalDateTime.now();
        }
    }

}