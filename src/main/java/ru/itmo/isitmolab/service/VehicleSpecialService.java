package ru.itmo.isitmolab.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import ru.itmo.isitmolab.dao.VehicleSpecialDao;
import ru.itmo.isitmolab.dto.VehicleDto;

import java.util.List;
import java.util.Optional;

@Stateless
public class VehicleSpecialService {

    @Inject
    VehicleSpecialDao fnDao;

    public Optional<VehicleDto> findAnyWithMinDistance() {
        return fnDao.findAnyWithMinDistance().map(VehicleDto::toDto);
    }

    public long countFuelConsumptionGreaterThan(float value) {
        return fnDao.countFuelConsumptionGreaterThan(value);
    }

    public List<VehicleDto> listFuelConsumptionGreaterThan(float value) {
        return fnDao.listFuelConsumptionGreaterThan(value).stream()
                .map(VehicleDto::toDto)
                .toList();
    }

    public List<VehicleDto> listByType(String type) {
        return fnDao.listByType(type).stream()
                .map(VehicleDto::toDto)
                .toList();
    }

    public List<VehicleDto> listByEnginePowerBetween(int min, int max) {
        return fnDao.listByEnginePowerBetween(min, max).stream()
                .map(VehicleDto::toDto)
                .toList();
    }
}