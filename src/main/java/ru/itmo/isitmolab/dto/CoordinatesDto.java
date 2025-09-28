package ru.itmo.isitmolab.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordinatesDto {
    @NotNull(message = "Координата X должна быть числом.")
    private Double x;

    @NotNull(message = "Координата Y должна быть числом.")
    private Float y;
}