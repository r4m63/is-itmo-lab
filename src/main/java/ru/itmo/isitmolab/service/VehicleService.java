package ru.itmo.isitmolab.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import ru.itmo.isitmolab.dao.AdminDao;
import ru.itmo.isitmolab.dao.VehicleDao;
import ru.itmo.isitmolab.dto.VehicleDto;
import ru.itmo.isitmolab.model.Admin;
import ru.itmo.isitmolab.model.Vehicle;

@Stateless
public class VehicleService {

    @Inject
    VehicleDao dao;
    @Inject
    AdminDao adminDao;
    @Inject
    SessionService sessionService;

    public void createNewVehicle(VehicleDto dto, HttpServletRequest req) {
        Long adminId = sessionService.getCurrentUserId(req);
        Admin admin = adminDao.findById(adminId)
                .orElseThrow(() -> new WebApplicationException("Admin not found: " + adminId, Response.Status.UNAUTHORIZED));

        Vehicle v = VehicleDto.toEntity(dto, null);
        v.setCreatedBy(admin);
        dao.save(v);
    }

    public void updateVehicle(Long id, VehicleDto dto) {
        Vehicle current = dao.findById(id)
                .orElseThrow(() -> new WebApplicationException(
                        "Vehicle not found: " + id, Response.Status.NOT_FOUND));
        VehicleDto.toEntity(dto, current);
        dao.save(current);
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
