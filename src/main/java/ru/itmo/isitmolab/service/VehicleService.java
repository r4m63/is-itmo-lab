package ru.itmo.isitmolab.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import ru.itmo.isitmolab.dao.VehicleDao;
import ru.itmo.isitmolab.dto.VehicleDto;
import ru.itmo.isitmolab.model.Vehicle;


@Stateless
public class VehicleService {

    @Inject
    VehicleDao dao;

    public void createNewVehicle(VehicleDto dto) {
        Vehicle entity = VehicleDto.toEntity(dto, null);
        Vehicle saved = dao.save(entity);
        VehicleDto.fromEntity(saved);
    }

    public void updateVehicle(Long id, VehicleDto dto) {
        Vehicle current = dao.findById(id)
                .orElseThrow(() -> new WebApplicationException(
                        "Vehicle not found: " + id, Response.Status.NOT_FOUND));

        VehicleDto.toEntity(dto, current);
        Vehicle saved = dao.save(current);
        VehicleDto.fromEntity(saved);
    }

    public VehicleDto getVehicleById(Long id) {
        Vehicle v = dao.findById(id)
                .orElseThrow(() -> new WebApplicationException(
                        "Vehicle not found: " + id, Response.Status.NOT_FOUND));
        return VehicleDto.fromEntity(v);
    }

    public void deleteVehicleById(Long id) {
        if (!dao.existsById(id)) {
            throw new WebApplicationException(
                    "Vehicle not found: " + id, Response.Status.NOT_FOUND);
        }
        dao.deleteById(id);
    }
}

//return userRepository.findById(userId).orElseThrow(() ->
//        new WebApplicationException(
//        Response.status(Response.Status.UNAUTHORIZED)
//                    .entity(Map.of("message", "User not found with id: " + userId))
//        .build()
//        )
//                );