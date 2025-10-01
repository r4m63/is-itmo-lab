package ru.itmo.isitmolab.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.itmo.isitmolab.model.Coordinates;
import ru.itmo.isitmolab.model.FuelType;
import ru.itmo.isitmolab.model.Vehicle;
import ru.itmo.isitmolab.model.VehicleType;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDto {

    private Long id;

    @NotBlank(message = "Заполните name.")
    private String name;

    @NotNull(message = "Координаты обязательны.")
    @Valid
    private CoordinatesDto coordinates;

    @NotNull(message = "Выберите type.")
    private VehicleType type;

    @Positive(message = "enginePower должно быть > 0.")
    private Integer enginePower;

    @NotNull(message = "numberOfWheels обязательно.")
    @Positive(message = "numberOfWheels должно быть > 0.")
    private Integer numberOfWheels;

    @Positive(message = "capacity должно быть > 0.")
    private Integer capacity;

    @Positive(message = "distanceTravelled должно быть > 0.")
    private Integer distanceTravelled;

    @NotNull(message = "fuelConsumption обязательно.")
    @Positive(message = "fuelConsumption должно быть > 0.")
    private Float fuelConsumption;

    @NotNull(message = "Выберите fuelType.")
    private FuelType fuelType;

    @NotNull(message = "ownerId обязателен.")
    private Long ownerId;

    private String ownerName;
    private Long adminId;

    private String creationDate;

    private static final DateTimeFormatter ISO_MILLIS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static VehicleDto toDto(Vehicle v) {
        if (v == null) return null;
        String createdIso = null;
        if (v.getCreationTime() != null) {
            createdIso = v.getCreationTime().truncatedTo(ChronoUnit.MILLIS).format(ISO_MILLIS);
        }
        CoordinatesDto coords = null;
        if (v.getCoordinates() != null) {
            coords = CoordinatesDto.builder()
                    .x(v.getCoordinates().getX())
                    .y(v.getCoordinates().getY())
                    .build();
        }
        Long ownerId = (v.getOwner() != null) ? v.getOwner().getId() : null;
        String ownerName = (v.getOwner() != null) ? v.getOwner().getFullName() : null;
        Long adminId = (v.getAdmin() != null) ? v.getAdmin().getId() : null;
        return VehicleDto.builder()
                .id(v.getId())
                .name(v.getName())
                .coordinates(coords)
                .type(v.getType())
                .enginePower(v.getEnginePower())
                .numberOfWheels(v.getNumberOfWheels())
                .capacity(v.getCapacity())
                .distanceTravelled(v.getDistanceTravelled())
                .fuelConsumption(v.getFuelConsumption())
                .fuelType(v.getFuelType())
                .ownerId(ownerId)
                .ownerName(ownerName)
                .adminId(adminId)
                .creationDate(createdIso)
                .build();
    }

    public static Vehicle toEntity(VehicleDto d, Vehicle target) {
        if (d == null) return null;
        if (target == null) target = new Vehicle();
        target.setName(d.getName() != null ? d.getName().trim() : null);
        Coordinates coords = (target.getCoordinates() != null) ? target.getCoordinates() : new Coordinates();
        CoordinatesDto cd = d.getCoordinates();
        if (cd != null) {
            coords.setX(cd.getX());
            coords.setY(cd.getY());
        } else {
            coords.setX(null);
            coords.setY(null);
        }
        target.setCoordinates(coords);
        target.setType(d.getType());
        target.setEnginePower(d.getEnginePower());
        if (d.getNumberOfWheels() != null) {
            target.setNumberOfWheels(d.getNumberOfWheels());
        }
        target.setCapacity(d.getCapacity());
        target.setDistanceTravelled(d.getDistanceTravelled());
        if (d.getFuelConsumption() != null) {
            target.setFuelConsumption(d.getFuelConsumption());
        }
        target.setFuelType(d.getFuelType());
        return target;
    }
}
