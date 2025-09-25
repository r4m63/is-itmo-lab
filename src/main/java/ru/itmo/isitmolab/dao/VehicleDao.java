package ru.itmo.isitmolab.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.itmo.isitmolab.model.Vehicle;

import java.util.Optional;

@Stateless
public class VehicleDao {

    @PersistenceContext(unitName = "studsPU")
    private EntityManager em;

    public Vehicle save(Vehicle v) {
        if (v.getId() == null) {
            em.persist(v);
            return v;
        }
        return em.merge(v);
    }

    public Optional<Vehicle> findById(Long id) {
        return Optional.ofNullable(em.find(Vehicle.class, id));
    }

    public boolean existsById(Long id) {
        return em.find(Vehicle.class, id) != null;
    }

    public void deleteById(Long id) {
        Vehicle ref = em.find(Vehicle.class, id);
        if (ref != null) {
            em.remove(ref);
        }
    }
}
