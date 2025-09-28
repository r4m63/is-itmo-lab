package ru.itmo.isitmolab.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import ru.itmo.isitmolab.dao.AdminDao;
import ru.itmo.isitmolab.dao.VehicleDao;
import ru.itmo.isitmolab.dto.GridTableRequest;
import ru.itmo.isitmolab.dto.GridTableResponse;
import ru.itmo.isitmolab.dto.VehicleDto;
import ru.itmo.isitmolab.model.Admin;
import ru.itmo.isitmolab.model.Vehicle;
import ru.itmo.isitmolab.ws.VehicleWsHub;

import java.util.List;


@Stateless
public class VehicleService {

    @Inject
    private VehicleDao dao;
    @Inject
    private AdminDao adminDao;
    @Inject
    private SessionService sessionService;
    @Inject
    private VehicleWsHub wsHub;
    @Inject
    private VehicleDao vehicleDao;

    public Long createNewVehicle(VehicleDto dto, HttpServletRequest req) {
        Long adminId = sessionService.getCurrentUserId(req);
        Admin admin = adminDao.findById(adminId)
                .orElseThrow(() -> new WebApplicationException(
                        "Admin not found: " + adminId, Response.Status.UNAUTHORIZED));

        Vehicle v = VehicleDto.toEntity(dto, null);
        v.setCreatedBy(admin);
        dao.save(v);
        wsHub.broadcastText("refresh");
        return v.getId();
    }

    public void updateVehicle(Long id, VehicleDto dto) {
        Vehicle current = dao.findById(id)
                .orElseThrow(() -> new WebApplicationException(
                        "Vehicle not found: " + id, Response.Status.NOT_FOUND));
        VehicleDto.toEntity(dto, current);
        dao.save(current);
        wsHub.broadcastText("refresh");
    }

    public VehicleDto getVehicleById(Long id) {
        Vehicle v = dao.findById(id)
                .orElseThrow(() -> new WebApplicationException(
                        "Vehicle not found: " + id, Response.Status.NOT_FOUND));
        return VehicleDto.toDto(v);
    }

    public void deleteVehicleById(Long id) {
        if (!dao.existsById(id)) {
            throw new WebApplicationException(
                    "Vehicle not found: " + id, Response.Status.NOT_FOUND);
        }
        dao.deleteById(id);
        wsHub.broadcastText("refresh");
    }

    public List<VehicleDto> getAllVehicles() {
        return dao.findAll().stream().map(VehicleDto::toDto).toList();
    }

    public GridTableResponse<VehicleDto> queryTableGridFilters(GridTableRequest req) {
        List<Vehicle> rows = vehicleDao.findPageByGrid(req); // страница сущностей
        long total = vehicleDao.countByGrid(req); // общее количество строк под те же фильтры
        List<VehicleDto> dtos = rows.stream()
                .map(VehicleDto::toDto)
                .toList();

        return new GridTableResponse<>(dtos, (int) total);
    }



}