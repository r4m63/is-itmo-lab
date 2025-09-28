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

import java.time.LocalDateTime;
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

    /** ISO-строка времени создания, отдаем на фронт (с миллисекундами) */
    private String creationDate;

    /** ISO с миллисекундами, без таймзоны: 2025-09-27T13:05:07.123 */
    private static final DateTimeFormatter ISO_MILLIS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /** Entity -> DTO  */
    public static VehicleDto toDto(Vehicle v) {
        if (v == null) return null;

        String createdIso = null;
        if (v.getCreationDateTime() != null) {
            createdIso = v.getCreationDateTime()
                    .truncatedTo(ChronoUnit.MILLIS)  // обрезаем наносекунды до миллисекунд
                    .format(ISO_MILLIS);             // форматируем как ISO с миллисекундами
        }

        return VehicleDto.builder()
                .id(v.getId())
                .name(v.getName())
                .coordinates(new CoordinatesDto(
                        v.getCoordinates() != null ? v.getCoordinates().getX() : null,
                        v.getCoordinates() != null ? v.getCoordinates().getY() : null
                ))
                .type(v.getType())
                .enginePower(v.getEnginePower())
                .numberOfWheels(v.getNumberOfWheels())
                .capacity(v.getCapacity())
                .distanceTravelled(v.getDistanceTravelled())
                .fuelConsumption(v.getFuelConsumption())
                .fuelType(v.getFuelType())
                .creationDate(createdIso)
                .build();
    }

    /** DTO -> Entity (в target либо создаём новый, либо обновляем переданный) */
    public static Vehicle toEntity(VehicleDto d, Vehicle target) {
        if (d == null) return null;
        if (target == null) target = new Vehicle();

        // Простейшая нормализация входных данных
        target.setName(d.getName() != null ? d.getName().trim() : null);

        // Координаты (создаём, если в target их ещё нет)
        Coordinates coords = target.getCoordinates();
        if (coords == null) coords = new Coordinates();
        CoordinatesDto cd = d.getCoordinates();
        coords.setX(cd != null ? cd.getX() : null);
        coords.setY(cd != null ? cd.getY() : null);
        target.setCoordinates(coords);

        // Прочие поля 1-в-1
        target.setType(d.getType());
        target.setEnginePower(d.getEnginePower());
        target.setNumberOfWheels(d.getNumberOfWheels());
        target.setCapacity(d.getCapacity());
        target.setDistanceTravelled(d.getDistanceTravelled());
        target.setFuelConsumption(d.getFuelConsumption());
        target.setFuelType(d.getFuelType());

        // Гарантируем, что дата создания задана при создании новой сущности
        if (target.getCreationDateTime() == null) {
            target.setCreationDateTime(LocalDateTime.now());
        }

        return target;
    }
}