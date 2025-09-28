package ru.itmo.isitmolab.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import ru.itmo.isitmolab.dto.GridTableRequest;
import ru.itmo.isitmolab.model.Vehicle;
import ru.itmo.isitmolab.util.gridtable.GridTablePredicateBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class VehicleDao {

    @PersistenceContext(unitName = "studsPU")
    EntityManager em;

    @Transactional
    public void save(Vehicle v) {
        if (v.getId() == null) {
            em.persist(v);
            em.flush();
            em.merge(v);
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

    public List<Vehicle> findAll() {
        return em.createQuery(
                "select v from Vehicle v order by v.creationDateTime desc", Vehicle.class
        ).getResultList();
    }

    public List<Vehicle> findAll(int offset, int limit) {
        return em.createQuery(
                        "select v from Vehicle v order by v.creationDateTime desc", Vehicle.class
                )
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Vehicle> findPageByGrid(GridTableRequest req) {
        final int pageSize = Math.max(1, req.endRow - req.startRow);
        final int first = Math.max(0, req.startRow);

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Vehicle> cq = cb.createQuery(Vehicle.class);
        Root<Vehicle> root = cq.from(Vehicle.class);

        List<Predicate> predicates = GridTablePredicateBuilder.build(cb, root, req.filterModel);
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        if (req.sortModel != null && !req.sortModel.isEmpty()) {
            List<Order> orders = new ArrayList<>();
            req.sortModel.forEach(s -> {
                Path<?> p = GridTablePredicateBuilder.resolvePath(root, s.getColId());
                orders.add("desc".equalsIgnoreCase(s.getSort()) ? cb.desc(p) : cb.asc(p));
            });
            cq.orderBy(orders);
        } else {
            cq.orderBy(cb.desc(GridTablePredicateBuilder.resolvePath(root, "creationDateTime")));
        }

        return em.createQuery(cq)
                .setFirstResult(first)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public long countByGrid(GridTableRequest req) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Long> cnt = cb.createQuery(Long.class);
        Root<Vehicle> cntRoot = cnt.from(Vehicle.class);

        List<Predicate> preds = GridTablePredicateBuilder.build(cb, cntRoot, req.filterModel);

        cnt.select(cb.count(cntRoot));
        if (!preds.isEmpty()) {
            cnt.where(preds.toArray(new Predicate[0]));
        }

        return em.createQuery(cnt).getSingleResult();
    }
}
