package com.parking.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JPAUtil {
    private static final EntityManagerFactory emf = build();

    private static EntityManagerFactory build() {
        try {
            return Persistence.createEntityManagerFactory("parkingPU");
        } catch (Exception e) {
            throw new IllegalStateException("Error creando EntityManagerFactory: " + e.getMessage(), e);
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private JPAUtil() {}
}