package ru.itmo.isitmolab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coordinates {

    @DecimalMax(value = "613", inclusive = true)
    @Column(name = "coordinates_x")
    private Double x; // max 613

    @NotNull
    @DecimalMax(value = "962", inclusive = true)
    @Column(name = "coordinates_y", nullable = false)
    private Float y; // max 962, not null
}