package ru.itmo.isitmolab.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Slf4j
@ApplicationScoped
public class HibernateUtil {

    private SessionFactory sessionFactory;

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing SessionFactory...");
            sessionFactory = new Configuration().configure().buildSessionFactory();
            log.info("SessionFactory created successfully");
        } catch (Exception e) {
            log.error("Error during SessionFactory initialization", e);
            throw new RuntimeException("Error creating SessionFactory", e);
        }
    }

    @Produces
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Produces
    @Dependent
    public Session getSession() {
        return sessionFactory.openSession();
    }

    @PreDestroy
    public void shutdown() {
        if (sessionFactory != null) {
            log.info("SessionFactory was closed successfully");
            sessionFactory.close();
        }
    }
}