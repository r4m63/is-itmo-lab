package ru.itmo.isitmolab.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.itmo.isitmolab.dao.AdminDao;
import ru.itmo.isitmolab.dao.PersonDao;
import ru.itmo.isitmolab.dao.VehicleDao;
import ru.itmo.isitmolab.dto.GridTableRequest;
import ru.itmo.isitmolab.dto.GridTableResponse;
import ru.itmo.isitmolab.dto.VehicleDto;
import ru.itmo.isitmolab.model.Admin;
import ru.itmo.isitmolab.model.Vehicle;
import ru.itmo.isitmolab.ws.VehicleWsHub;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
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
    @Inject
    private PersonDao personDao;

    public Long createNewVehicle(VehicleDto dto, HttpServletRequest req) {
        Long adminId = sessionService.getCurrentUserId(req);
        Admin admin = adminDao.findById(adminId)
                .orElseThrow(() -> new WebApplicationException(
                        "Admin not found: " + adminId, Response.Status.UNAUTHORIZED));

        if (dto.getOwnerId() == null) {
            throw new WebApplicationException("ownerId is required", Response.Status.BAD_REQUEST);
        }


        var owner = personDao.findById(dto.getOwnerId())
                .orElseThrow(() -> new WebApplicationException(
                        Response.status(Response.Status.BAD_REQUEST)
                                .type(MediaType.APPLICATION_JSON_TYPE)
                                .entity(Map.of("message", "Не найден owner с id " + dto.getOwnerId()))
                                .build()
                ));

        Vehicle v = VehicleDto.toEntity(dto, null);
        v.setAdmin(admin);
        v.setOwner(owner);

        dao.save(v);
        wsHub.broadcastText("refresh");
        return v.getId();
    }

    public void updateVehicle(Long id, VehicleDto dto) {
        Vehicle current = dao.findById(id)
                .orElseThrow(() -> new WebApplicationException(
                        "Vehicle not found: " + id, Response.Status.NOT_FOUND));

        if (dto.getOwnerId() != null &&
                (current.getOwner() == null || !dto.getOwnerId().equals(current.getOwner().getId()))) {
            var newOwner = personDao.findById(dto.getOwnerId())
                    .orElseThrow(() -> new WebApplicationException(
                            "Person (owner) not found: " + dto.getOwnerId(), Response.Status.BAD_REQUEST));
            current.setOwner(newOwner);
        }

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
        List<Vehicle> rows = vehicleDao.findPageByGrid(req);
        long total = vehicleDao.countByGrid(req);
        List<VehicleDto> dtos = rows.stream()
                .map(VehicleDto::toDto)
                .toList();

        return new GridTableResponse<>(dtos, (int) total);
    }

    public List<Vehicle> findByOwner(Long ownerId) {
        return vehicleDao.findByOwner(ownerId);
    }

    @Transactional
    public int reassignOwnerBulk(Long fromOwnerId, Long toOwnerId) {
        if (Objects.equals(fromOwnerId, toOwnerId)) {
            throw new WebApplicationException("Нельзя переназначать на того же владельца", Response.Status.BAD_REQUEST);
        }
        if (!personDao.existsById(toOwnerId)) {
            throw new WebApplicationException("Целевой владелец не найден: " + toOwnerId, Response.Status.BAD_REQUEST);
        }
        return vehicleDao.reassignOwner(fromOwnerId, toOwnerId);
    }

}