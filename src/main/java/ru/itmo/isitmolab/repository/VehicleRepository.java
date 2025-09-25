package ru.itmo.isitmolab.repository;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import ru.itmo.isitmolab.model.Vehicle;

@Repository
public interface VehicleRepository extends CrudRepository<Vehicle, Long> {
    boolean existsById(Long id);
}
