package ru.itmo.isitmolab.grid;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import ru.itmo.isitmolab.model.Vehicle;

@ApplicationScoped
public class VehicleRepository {

    @Inject
    EntityManager em;

    @Transactional
    public Vehicle save(Vehicle v){
        if (v.getId() == null) { em.persist(v); return v; }
        else return em.merge(v);
    }

    public Vehicle find(Long id){ return em.find(Vehicle.class, id); }

    @Transactional
    public void delete(Long id){
        Vehicle v = em.find(Vehicle.class, id);
        if (v != null) em.remove(v);
    }

    public AgGridCriteriaBuilder.Result<Vehicle> query(GridQueryRequest req, int startRow, int endRow){
        int limit = Math.max(1, endRow - startRow);
        var builder = new AgGridCriteriaBuilder<Vehicle>(em, Vehicle.class);
        return builder.buildAndExecute(req.filterModel, req.sortModel, startRow, limit);
    }

    public long countAll(){
        return em.createQuery("select count(v) from Vehicle v", Long.class).getSingleResult();
    }
}