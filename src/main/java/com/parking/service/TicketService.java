package com.parking.service;

import com.parking.domain.*;

import com.parking.repository.TicketEstadiaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TicketService {

    private final TicketEstadiaRepository repo;
    private final CalculoTarifaService calculo;

    public TicketService(TicketEstadiaRepository repo, CalculoTarifaService calculo) {
        this.repo = repo;
        this.calculo = calculo;
    }

    public TicketEstadia registrarEntrada(String patente, TipoVehiculo tipo) {
        if (patente == null || patente.isBlank()) throw new DomainException("Patente obligatoria");
        if (tipo == null) throw new DomainException("Tipo de vehículo obligatorio");

        // Capacidad
        long ocupados = repo.countOpen();
        if (ocupados >= Config.getCapacidad()) {
            throw new DomainException("Capacidad completa. No se puede registrar la entrada.");
        }

        // Un ticket abierto por patente
        Optional<TicketEstadia> abierto = repo.findOpenByPatente(patente);
        if (abierto.isPresent()) {
            throw new DomainException("Ya existe un ticket ABIERTO para la patente " + patente.toUpperCase());
        }

        TicketEstadia t = new TicketEstadia();
        t.setPatente(patente);
        t.setTipoVehiculo(tipo);
        t.setHoraEntrada(LocalDateTime.now());
        t.setEstado(EstadoTicket.ABIERTO);
        t.setNroTicket("PENDIENTE");

        // Persistir para obtener ID y luego generar nro_ticket
        t = repo.save(t);
        String nro = generarNroTicket(t.getId());
        t.setNroTicket(nro);
        t = repo.update(t);
        return t;
    }

    private String generarNroTicket(Long id) {
        LocalDate hoy = LocalDate.now();
        return "T-" + hoy.getYear() + "-" + String.format("%06d", id);
    }

    public List<TicketEstadia> listarAbiertos() {
        return repo.findOpenTickets();
    }

    public TicketEstadia registrarSalida(Long idTicket, FormaPago formaPago) {
        TicketEstadia t = repo.findOpenById(idTicket)
                .orElseThrow(() -> new DomainException("Ticket no encontrado o no está ABIERTO"));

        LocalDateTime salida = LocalDateTime.now();
        if (salida.isBefore(t.getHoraEntrada())) {
            throw new DomainException("La hora de salida no puede ser anterior a la entrada");
        }
        if (formaPago == null) {
            throw new DomainException("La forma de pago es obligatoria");
        }

        CalculoTarifaService.ResultadoCalculo r = calculo.calcular(t.getTipoVehiculo(), t.getHoraEntrada(), salida);

        if (r.importeFinal.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("El importe calculado no puede ser negativo");
        }

        t.setHoraSalida(salida);
        t.setMinutosEstadia(r.minutosTotales);
        t.setTarifaAplicada(r.tarifaAplicada);
        t.setImporteCalculado(r.importeFinal);
        t.setFormaPago(formaPago);
        t.setEstado(EstadoTicket.CERRADO);
        t.setCerradoEn(LocalDateTime.now());

        return repo.update(t);
    }

    public TicketEstadia anular(Long idTicket, String motivo) {
        if (motivo == null || motivo.isBlank()) throw new DomainException("El motivo de anulación es obligatorio");

        TicketEstadia t = repo.findById(idTicket)
                .orElseThrow(() -> new DomainException("Ticket no encontrado"));

        // Solo ABIERTO o CERRADO del día
        if (!(t.getEstado() == EstadoTicket.ABIERTO ||
              (t.getEstado() == EstadoTicket.CERRADO &&
                (t.getCerradoEn() != null && t.getCerradoEn().toLocalDate().equals(LocalDate.now()))))) {
            throw new DomainException("Solo se puede anular tickets ABIERTO o CERRADO del día");
        }

        t.setEstado(EstadoTicket.ANULADO);
        t.setMotivoAnulacion(motivo);
        // Anulado no computa recaudación (dejamos importe_calculado sin modificar, pero reportes excluyen ANULADO)
        return repo.update(t);
    }
}