package ru.itmo.isitmolab.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import ru.itmo.isitmolab.dto.GridTableRequest;
import ru.itmo.isitmolab.model.Person;
import ru.itmo.isitmolab.util.persontable.PersonGridPredicateBuilder;

import java.util.*;

@ApplicationScoped
public class PersonDao {

    @PersistenceContext(unitName = "studsPU")
    EntityManager em;

    @Transactional
    public Person save(Person p) {
        if (p.getId() == null) {
            em.persist(p);
            return p;
        } else {
            return em.merge(p);
        }
    }

    public Optional<Person> findById(Long id) {
        return Optional.ofNullable(em.find(Person.class, id));
    }

    @Transactional
    public void deleteById(Long id) {
        if (id == null) return;
        Person ref = em.find(Person.class, id);
        if (ref != null) em.remove(ref);
    }

    private EntityGraph<Person> graph() {
        @SuppressWarnings("unchecked")
        EntityGraph<Person> g = (EntityGraph<Person>) em.createEntityGraph("Person.withAdmin");
        return g;
    }

    public Map<Long, Integer> vehiclesCountByOwnerIds(List<Long> ownerIds) {
        Map<Long, Integer> out = new HashMap<>();
        if (ownerIds == null || ownerIds.isEmpty()) return out;

        List<Object[]> rows = em.createQuery(
                        "select v.owner.id, count(v) " +
                                "from Vehicle v " +
                                "where v.owner.id in :ids " +
                                "group by v.owner.id", Object[].class)
                .setParameter("ids", ownerIds)
                .getResultList();

        for (Object[] r : rows) {
            Long ownerId = (Long) r[0];
            Long cnt = (Long) r[1];
            out.put(ownerId, cnt != null ? cnt.intValue() : 0);
        }
        // для тех, у кого нет записей в vehicle — вернём 0
        ownerIds.forEach(id -> out.putIfAbsent(id, 0));
        return out;
    }

    public List<Person> findPageByGrid(GridTableRequest req) {
        final int pageSize = Math.max(1, req.endRow - req.startRow);
        final int first = Math.max(0, req.startRow);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Person> cq = cb.createQuery(Person.class);
        Root<Person> root = cq.from(Person.class);

        var predicates = PersonGridPredicateBuilder.build(cb, root, req.filterModel);
        if (!predicates.isEmpty()) cq.where(predicates.toArray(new Predicate[0]));

        if (req.sortModel != null && !req.sortModel.isEmpty()) {
            List<Order> orders = new ArrayList<>();
            req.sortModel.forEach(s -> {
                Path<?> p = PersonGridPredicateBuilder.resolvePath(root, s.getColId());
                orders.add("desc".equalsIgnoreCase(s.getSort()) ? cb.desc(p) : cb.asc(p));
            });
            cq.orderBy(orders);
        } else {
            cq.orderBy(cb.asc(root.get("fullName")), cb.asc(root.get("id")));
        }

        cq.select(root);

        return em.createQuery(cq)
                .setFirstResult(first)
                .setMaxResults(pageSize)
                .setHint("jakarta.persistence.loadgraph", graph())
                .getResultList();
    }

    public long countByGrid(GridTableRequest req) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cnt = cb.createQuery(Long.class);
        Root<Person> root = cnt.from(Person.class);

        var preds = PersonGridPredicateBuilder.build(cb, root, req.filterModel);
        cnt.select(cb.count(root));
        if (!preds.isEmpty()) cnt.where(preds.toArray(new Predicate[0]));

        return em.createQuery(cnt).getSingleResult();
    }

    /** Для выпадашки — вернуть всех (или обрезать лимитом) */
    public List<Person> findAllShort(int limit) {
        return em.createQuery("select p from Person p order by p.fullName asc, p.id asc", Person.class)
                .setMaxResults(Math.max(1, limit))
                .setHint("jakarta.persistence.loadgraph", graph())
                .getResultList();
    }

    public Map<Long, Integer> countVehiclesForPersonIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        List<Object[]> rows = em.createQuery(
                        "select p.id, count(v.id) " +
                                "from Person p left join Vehicle v on v.owner.id = p.id " +
                                "where p.id in :ids group by p.id", Object[].class)
                .setParameter("ids", ids)
                .getResultList();

        Map<Long, Integer> out = new HashMap<>();
        for (Object[] r : rows) out.put((Long) r[0], ((Long) r[1]).intValue());
        return out;
    }

    public int countVehiclesForPersonId(Long id) {
        Long c = em.createQuery(
                        "select count(v.id) from Vehicle v where v.owner.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return c.intValue();
    }

    public List<Person> findAllOrdered() {
        return em.createQuery(
                "select p from Person p order by p.fullName asc, p.id asc",
                Person.class
        ).getResultList();
    }

    public List<Person> searchByName(String q, int limit) {
        String patt = "%" + (q == null ? "" : q.trim().toLowerCase()) + "%";
        return em.createQuery(
                        "select p from Person p " +
                                "where lower(p.fullName) like :patt " +
                                "order by p.fullName asc, p.id asc", Person.class)
                .setParameter("patt", patt)
                .setMaxResults(Math.max(1, Math.min(limit, 100)))
                .getResultList();
    }

    public List<Person> findTop(int limit) {
        return em.createQuery(
                        "select p from Person p order by p.fullName asc, p.id asc",
                        Person.class)
                .setMaxResults(Math.max(1, Math.min(limit, 100)))
                .getResultList();
    }

    public boolean existsById(Long id) {
        if (id == null) return false;
        Long cnt = em.createQuery(
                        "select count(p) from Person p where p.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }



}
