package ru.itmo.isitmolab.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;

@Embeddable
public class Coordinates {
    @DecimalMax(value = "613", inclusive = true, message = "x ≤ 613")
    private double x;

    @NotNull(message = "y is required")
    @DecimalMax(value = "962", inclusive = true, message = "y ≤ 962")
    private Float y;

    public Coordinates() {
    }

    public Coordinates(double x, Float y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }
}
