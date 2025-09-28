package ru.itmo.isitmolab.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import ru.itmo.isitmolab.model.Admin;

import java.util.Optional;

@ApplicationScoped
public class AdminDao {

    @PersistenceContext(unitName = "studsPU")
    private EntityManager em;

    public Optional<Admin> findByLoginAndPassHash(String login, String passHash) {
        try {
            Admin a = em.createQuery(
                            "select a from Admin a where a.login = :l and a.passHash = :p",
                            Admin.class
                    )
                    .setParameter("l", login)
                    .setParameter("p", passHash)
                    .setMaxResults(1)
                    .getSingleResult();
            return Optional.of(a);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<Admin> findById(Long id) {
        return Optional.ofNullable(em.find(Admin.class, id));
    }

}
