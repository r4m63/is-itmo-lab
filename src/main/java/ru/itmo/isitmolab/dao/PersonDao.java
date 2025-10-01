package ru.itmo.isitmolab.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import ru.itmo.isitmolab.model.Person;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PersonDao {

    @PersistenceContext(unitName = "studsPU")
    EntityManager em;

    public Optional<Person> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(em.find(Person.class, id));
    }

    public boolean existsById(Long id) {
        if (id == null) return false;
        Long cnt = em.createQuery("select count(p) from Person p where p.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }

    public Person save(Person p) {
        if (p.getId() == null) {
            em.persist(p);
            return p;
        } else {
            return em.merge(p);
        }
    }

    public void deleteById(Long id) {
        if (id == null) return;
        Person ref = em.find(Person.class, id);
        if (ref != null) em.remove(ref);
    }

    public List<Person> findAll() {
        return em.createQuery(
                "select p from Person p order by p.creationTime desc, p.id desc",
                Person.class
        ).getResultList();
    }

    public List<Person> findAll(int offset, int limit) {
        return em.createQuery(
                        "select p from Person p order by p.creationTime desc, p.id desc",
                        Person.class
                )
                .setFirstResult(Math.max(0, offset))
                .setMaxResults(Math.max(1, limit))
                .getResultList();
    }

    public List<Person> findAllByAdminId(Long adminId) {
        if (adminId == null) return List.of();
        return em.createQuery(
                        "select p from Person p where p.admin.id = :aid order by p.creationTime desc, p.id desc",
                        Person.class
                )
                .setParameter("aid", adminId)
                .getResultList();
    }

    public List<Person> findByAdminId(Long adminId, int offset, int limit) {
        if (adminId == null) return List.of();
        return em.createQuery(
                        "select p from Person p where p.admin.id = :aid order by p.creationTime desc, p.id desc",
                        Person.class
                )
                .setParameter("aid", adminId)
                .setFirstResult(Math.max(0, offset))
                .setMaxResults(Math.max(1, limit))
                .getResultList();
    }

    public List<Person> searchByFullName(String query, Integer limit, Long adminId) {
        String q = (query == null) ? "" : query.trim().toLowerCase();
        int max = (limit == null || limit <= 0) ? 50 : limit;

        if (adminId == null) {
            return em.createQuery(
                            "select p from Person p " +
                                    "where lower(p.fullName) like :q " +
                                    "order by p.creationTime desc, p.id desc",
                            Person.class
                    )
                    .setParameter("q", "%" + q + "%")
                    .setMaxResults(max)
                    .getResultList();
        } else {
            return em.createQuery(
                            "select p from Person p " +
                                    "where p.admin.id = :aid and lower(p.fullName) like :q " +
                                    "order by p.creationTime desc, p.id desc",
                            Person.class
                    )
                    .setParameter("aid", adminId)
                    .setParameter("q", "%" + q + "%")
                    .setMaxResults(max)
                    .getResultList();
        }
    }

    public Optional<Person> findOwnedByAdmin(Long personId, Long adminId) {
        if (personId == null || adminId == null) return Optional.empty();
        try {
            Person p = em.createQuery(
                            "select p from Person p where p.id = :pid and p.admin.id = :aid",
                            Person.class
                    )
                    .setParameter("pid", personId)
                    .setParameter("aid", adminId)
                    .setMaxResults(1)
                    .getSingleResult();
            return Optional.of(p);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

}
