package ru.itmo.isitmolab.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import ru.itmo.isitmolab.dto.GridTableRequest;
import ru.itmo.isitmolab.model.Vehicle;
import ru.itmo.isitmolab.util.gridtable.GridTablePredicateBuilder;

import java.util.*;


@ApplicationScoped
public class VehicleDao {

    @PersistenceContext(unitName = "studsPU")
    EntityManager em;


    @Transactional
    public void save(Vehicle v) {
        if (v.getId() == null) {
            em.persist(v);
        } else {
            em.merge(v);
        }
    }

    public Optional<Vehicle> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(em.find(Vehicle.class, id));
    }

    public boolean existsById(Long id) {
        if (id == null) return false;
        Long cnt = em.createQuery("select count(v) from Vehicle v where v.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }

    @Transactional
    public void deleteById(Long id) {
        if (id == null) return;
        Vehicle ref = em.find(Vehicle.class, id);
        if (ref != null) em.remove(ref);
    }


    public List<Vehicle> findAll() {
        return em.createQuery(
                "select v from Vehicle v order by v.creationTime desc, v.id desc", Vehicle.class
        ).getResultList();
    }

    public List<Vehicle> findAll(int offset, int limit) {
        return em.createQuery(
                        "select v from Vehicle v order by v.creationTime desc, v.id desc", Vehicle.class
                )
                .setFirstResult(Math.max(0, offset))
                .setMaxResults(Math.max(1, limit))
                .getResultList();
    }


    public List<Vehicle> findPageByGrid(GridTableRequest req) {
        final int pageSize = Math.max(1, req.endRow - req.startRow);
        final int first = Math.max(0, req.startRow);

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // ---------- Шаг 1: получаем страницу ID ----------
        CriteriaQuery<Long> idCq = cb.createQuery(Long.class);
        Root<Vehicle> idRoot = idCq.from(Vehicle.class);

        List<Predicate> predicates = GridTablePredicateBuilder.build(cb, idRoot, req.filterModel);
        if (!predicates.isEmpty()) idCq.where(predicates.toArray(new Predicate[0]));

        if (req.sortModel != null && !req.sortModel.isEmpty()) {
            List<Order> orders = new ArrayList<>();
            req.sortModel.forEach(s -> {
                Path<?> p = GridTablePredicateBuilder.resolvePath(idRoot, s.getColId());
                orders.add("desc".equalsIgnoreCase(s.getSort()) ? cb.desc(p) : cb.asc(p));
            });
            idCq.orderBy(orders);
        } else {
            idCq.orderBy(cb.desc(idRoot.get("creationTime")), cb.desc(idRoot.get("id")));
        }

        idCq.select(idRoot.get("id"));

        List<Long> ids = em.createQuery(idCq)
                .setFirstResult(first)
                .setMaxResults(pageSize)
                .getResultList();

        if (ids.isEmpty()) return List.of();

        // ---------- Шаг 2: грузим сущности по этим id с графом ----------
        EntityGraph<Vehicle> graph = getWithOwnerAdminGraph();

        List<Vehicle> items = em.createQuery(
                        "select v from Vehicle v where v.id in :ids", Vehicle.class)
                .setParameter("ids", ids)
                .setHint("jakarta.persistence.loadgraph", graph)
                .getResultList();

        // ---------- Сохраняем порядок как в ids ----------
        Map<Long, Integer> rank = new HashMap<>(ids.size() * 2);
        for (int i = 0; i < ids.size(); i++) rank.put(ids.get(i), i);
        items.sort(Comparator.comparingInt(v -> rank.getOrDefault(v.getId(), Integer.MAX_VALUE)));

        return items;
    }

    public long countByGrid(GridTableRequest req) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cnt = cb.createQuery(Long.class);
        Root<Vehicle> root = cnt.from(Vehicle.class);

        List<Predicate> preds = GridTablePredicateBuilder.build(cb, root, req.filterModel);
        cnt.select(cb.count(root));
        if (!preds.isEmpty()) cnt.where(preds.toArray(new Predicate[0]));

        return em.createQuery(cnt).getSingleResult();
    }

    @SuppressWarnings("unchecked")
    private EntityGraph<Vehicle> getWithOwnerAdminGraph() {
        try {
            // если добавлен @NamedEntityGraph(name="Vehicle.withOwnerAdmin")
            return (EntityGraph<Vehicle>) em.getEntityGraph("Vehicle.withOwnerAdmin");
        } catch (IllegalArgumentException ex) {
            // fallback: динамический граф
            EntityGraph<Vehicle> g = em.createEntityGraph(Vehicle.class);
            g.addAttributeNodes("owner", "admin");
            return g;
        }
    }
}
