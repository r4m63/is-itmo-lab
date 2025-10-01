package ru.itmo.isitmolab.dto;

import jakarta.validation.constraints.DecimalMax;
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
    @NotNull
    @DecimalMax("613")
    private Double x;

    @NotNull
    @DecimalMax("962")
    private Float y;
}