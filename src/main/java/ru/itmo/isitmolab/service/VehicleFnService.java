package ru.itmo.isitmolab.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import ru.itmo.isitmolab.dao.VehicleFnDao;
import ru.itmo.isitmolab.dto.VehicleDto;
import ru.itmo.isitmolab.model.Vehicle;

import java.util.List;
import java.util.Optional;

@Stateless
public class VehicleFnService {

    @Inject
    VehicleFnDao fnDao;

    public Optional<VehicleDto> findAnyWithMinDistance() {
        return fnDao.findAnyWithMinDistance().map(VehicleDto::fromEntity);
    }

    public long countFuelConsumptionGreaterThan(float value) {
        return fnDao.countFuelConsumptionGreaterThan(value);
    }

    public List<VehicleDto> listFuelConsumptionGreaterThan(float value) {
        return fnDao.listFuelConsumptionGreaterThan(value).stream()
                .map(VehicleDto::fromEntity)
                .toList();
    }

    public List<VehicleDto> listByType(String type) {
        return fnDao.listByType(type).stream()
                .map(VehicleDto::fromEntity)
                .toList();
    }

    public List<VehicleDto> listByEnginePowerBetween(int min, int max) {
        return fnDao.listByEnginePowerBetween(min, max).stream()
                .map(VehicleDto::fromEntity)
                .toList();
    }
}