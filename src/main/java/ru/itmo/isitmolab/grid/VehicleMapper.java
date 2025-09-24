package ru.itmo.isitmolab.grid;

import ru.itmo.isitmolab.dto.VehicleDto;
import ru.itmo.isitmolab.model.Vehicle;

public final class VehicleMapper {
    public static VehicleDto toDto(Vehicle v) {
        VehicleDto d = new VehicleDto();
        d.id = v.getId();
        d.name = v.getName();
        d.type = v.getType();
        d.enginePower = v.getEnginePower();
        d.distanceTravelled = v.getDistanceTravelled();
        d.fuelConsumption = v.getFuelConsumption();
        return d;
    }
}
