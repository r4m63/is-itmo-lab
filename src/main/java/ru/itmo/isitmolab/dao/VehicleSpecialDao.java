package ru.itmo.isitmolab.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.itmo.isitmolab.model.Vehicle;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VehicleSpecialDao {

    @PersistenceContext(unitName = "studsPU")
    EntityManager em;

    public Optional<Vehicle> findAnyWithMinDistance() {
        List<Vehicle> list = em.createNativeQuery(
                        "select * from fn_vehicle_min_distance()", Vehicle.class)
                .getResultList();
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public long countFuelConsumptionGreaterThan(float v) {
        Number n = (Number) em.createNativeQuery(
                        "select fn_vehicle_count_fuel_gt(?1)")
                .setParameter(1, v)
                .getSingleResult();
        return n.longValue();
    }

    public List<Vehicle> listFuelConsumptionGreaterThan(float v) {
        return em.createNativeQuery(
                        "select * from fn_vehicle_list_fuel_gt(?1)", Vehicle.class)
                .setParameter(1, v)
                .getResultList();
    }

    public List<Vehicle> listByType(String type) {
        return em.createNativeQuery(
                        "select * from fn_vehicle_list_by_type(?1)", Vehicle.class)
                .setParameter(1, type)
                .getResultList();
    }

    public List<Vehicle> listByEnginePowerBetween(Integer min, Integer max) {
        return em.createNativeQuery(
                        "select * from fn_vehicle_list_engine_between(?1, ?2)", Vehicle.class)
                .setParameter(1, min)
                .setParameter(2, max)
                .getResultList();
    }
}
