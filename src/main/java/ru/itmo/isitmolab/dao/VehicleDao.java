package ru.itmo.isitmolab.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import ru.itmo.isitmolab.model.Vehicle;

import java.util.Optional;

@ApplicationScoped
public class VehicleDao {

    @PersistenceContext(unitName = "studsPU")
    EntityManager em;

    @Transactional
    public Vehicle save(Vehicle v) {
        if (v.getId() == null) {
            em.persist(v);
            em.flush();
            return v;
        } else {
            return em.merge(v);
        }
    }

    public Optional<Vehicle> findById(Long id) {
        return Optional.ofNullable(em.find(Vehicle.class, id));
    }

    public boolean existsById(Long id) {
        if (id == null) return false;
        return em.createQuery("select count(v) from Vehicle v where v.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    @Transactional
    public void deleteById(Long id) {
        if (id == null) return;
        Vehicle ref = em.find(Vehicle.class, id);
        if (ref != null) {
            em.remove(ref);
        }
    }
}
