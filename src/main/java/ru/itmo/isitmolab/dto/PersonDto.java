package ru.itmo.isitmolab.dto;

import lombok.*;
import ru.itmo.isitmolab.model.Person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonDto {
    private Long id;
    @NotBlank(message = "Заполните Full Name.")
    private String fullName;
    private String creationDate;
    private Long adminId;
    private Integer vehiclesCount; // заполняем отдельно

    private static final DateTimeFormatter ISO_MILLIS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static PersonDto toDto(Person p, Integer vehiclesCount) {
        if (p == null) return null;

        Long adminId = (p.getAdmin() != null) ? p.getAdmin().getId() : null;
        LocalDateTime ct = p.getCreationTime();

        return PersonDto.builder()
                .id(p.getId())
                .fullName(p.getFullName())
                .adminId(adminId)
                .creationDate(ct != null ? ct.toString() : null)
                .vehiclesCount(vehiclesCount)
                .build();
    }

    public static void apply(PersonDto d, Person target) {
        // изменяем только разрешённые поля
        target.setFullName(d.getFullName() != null ? d.getFullName().trim() : null);
        // admin/creationTime/vehicles — не трогаем, управляется сервисом/БД
    }

    public static PersonDto withVehiclesCount(PersonDto base, Integer cnt) {
        base.setVehiclesCount(cnt);
        return base;
    }

    public static PersonDto toShort(Person p) {
        if (p == null) return null;
        return PersonDto.builder()
                .id(p.getId())
                .fullName(p.getFullName())
                .build(); // без adminId/creationDate/vehiclesCount
    }
}
