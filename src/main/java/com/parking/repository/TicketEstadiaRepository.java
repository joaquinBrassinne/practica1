package com.parking.repository;

import com.parking.domain.EstadoTicket;
import com.parking.domain.FormaPago;
import com.parking.domain.TicketEstadia;
import com.parking.domain.TipoVehiculo;
import com.parking.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class TicketEstadiaRepository {

    public TicketEstadia save(TicketEstadia t) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(t);
            em.getTransaction().commit();
            return t;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public TicketEstadia update(TicketEstadia t) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            TicketEstadia merged = em.merge(t);
            em.getTransaction().commit();
            return merged;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<TicketEstadia> findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(TicketEstadia.class, id));
        } finally {
            em.close();
        }
    }

    public Optional<TicketEstadia> findOpenByPatente(String patente) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<TicketEstadia> q = em.createQuery(
                    "SELECT t FROM TicketEstadia t WHERE UPPER(t.patente) = :pat AND t.estado = :estado",
                    TicketEstadia.class);
            q.setParameter("pat", patente.trim().toUpperCase());
            q.setParameter("estado", EstadoTicket.ABIERTO);
            List<TicketEstadia> list = q.getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    public long countOpen() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery(
                    "SELECT COUNT(t) FROM TicketEstadia t WHERE t.estado = :estado", Long.class);
            q.setParameter("estado", EstadoTicket.ABIERTO);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<TicketEstadia> findOpenTickets() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<TicketEstadia> q = em.createQuery(
                    "SELECT t FROM TicketEstadia t WHERE t.estado = :estado ORDER BY t.horaEntrada ASC",
                    TicketEstadia.class);
            q.setParameter("estado", EstadoTicket.ABIERTO);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<TicketEstadia> findOpenById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<TicketEstadia> q = em.createQuery(
                    "SELECT t FROM TicketEstadia t WHERE t.id = :id AND t.estado = :estado",
                    TicketEstadia.class);
            q.setParameter("id", id);
            q.setParameter("estado", EstadoTicket.ABIERTO);
            List<TicketEstadia> list = q.getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    public List<TicketEstadia> search(LocalDateTime desde, LocalDateTime hasta,
                                      String patenteLike, EstadoTicket estado,
                                      TipoVehiculo tipoVehiculo,
                                      BigDecimal importeMin, BigDecimal importeMax) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT t FROM TicketEstadia t WHERE 1=1 ");
            Map<String, Object> params = new HashMap<>();
            if (desde != null) {
                jpql.append(" AND t.horaEntrada >= :desde ");
                params.put("desde", desde);
            }
            if (hasta != null) {
                jpql.append(" AND t.horaEntrada <= :hasta ");
                params.put("hasta", hasta);
            }
            if (patenteLike != null && !patenteLike.isBlank()) {
                jpql.append(" AND UPPER(t.patente) LIKE :pat ");
                params.put("pat", "%" + patenteLike.trim().toUpperCase() + "%");
            }
            if (estado != null) {
                jpql.append(" AND t.estado = :estado ");
                params.put("estado", estado);
            }
            if (tipoVehiculo != null) {
                jpql.append(" AND t.tipoVehiculo = :tipo ");
                params.put("tipo", tipoVehiculo);
            }
            if (importeMin != null) {
                jpql.append(" AND t.importeCalculado >= :imin ");
                params.put("imin", importeMin);
            }
            if (importeMax != null) {
                jpql.append(" AND t.importeCalculado <= :imax ");
                params.put("imax", importeMax);
            }
            jpql.append(" ORDER BY t.horaEntrada DESC ");

            TypedQuery<TicketEstadia> q = em.createQuery(jpql.toString(), TicketEstadia.class);
            params.forEach(q::setParameter);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Map<FormaPago, BigDecimal> totalesPorFormaPago(LocalDate fecha) {
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.atTime(23, 59, 59);
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Object[]> q = em.createQuery(
                    "SELECT t.formaPago, COALESCE(SUM(t.importeCalculado), 0) " +
                    "FROM TicketEstadia t " +
                    "WHERE t.estado = :cerrado AND t.horaEntrada BETWEEN :d AND :h " +
                    "GROUP BY t.formaPago",
                    Object[].class);
            q.setParameter("cerrado", com.parking.domain.EstadoTicket.CERRADO);
            q.setParameter("d", desde);
            q.setParameter("h", hasta);
            Map<FormaPago, BigDecimal> res = new EnumMap<>(FormaPago.class);
            for (Object[] row : q.getResultList()) {
                FormaPago fp = (FormaPago) row[0];
                BigDecimal total = (BigDecimal) row[1];
                if (fp != null) res.put(fp, total);
            }
            return res;
        } finally {
            em.close();
        }
    }

    public Map<EstadoTicket, Long> cantidadPorEstado(LocalDate fecha) {
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.atTime(23, 59, 59);
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Object[]> q = em.createQuery(
                    "SELECT t.estado, COUNT(t) FROM TicketEstadia t " +
                    "WHERE t.horaEntrada BETWEEN :d AND :h " +
                    "GROUP BY t.estado", Object[].class);
            q.setParameter("d", desde);
            q.setParameter("h", hasta);
            Map<EstadoTicket, Long> res = new EnumMap<>(EstadoTicket.class);
            for (Object[] row : q.getResultList()) {
                res.put((EstadoTicket) row[0], (Long) row[1]);
            }
            return res;
        } finally {
            em.close();
        }
    }

    public BigDecimal recaudacionNeta(LocalDate fecha) {
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.atTime(23, 59, 59);
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<BigDecimal> q = em.createQuery(
                    "SELECT COALESCE(SUM(t.importeCalculado), 0) FROM TicketEstadia t " +
                    "WHERE t.estado = :cerrado AND t.horaEntrada BETWEEN :d AND :h",
                    BigDecimal.class);
            q.setParameter("cerrado", EstadoTicket.CERRADO);
            q.setParameter("d", desde);
            q.setParameter("h", hasta);
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }
}