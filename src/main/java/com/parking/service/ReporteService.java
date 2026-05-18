package com.parking.service;

import com.parking.domain.EstadoTicket;
import com.parking.domain.FormaPago;
import com.parking.domain.TicketEstadia;
import com.parking.domain.TipoVehiculo;
import com.parking.repository.TicketEstadiaRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ReporteService {

    private final TicketEstadiaRepository repo;

    public ReporteService(TicketEstadiaRepository repo) {
        this.repo = repo;
    }

    public long ocupacionActual() {
        return repo.countOpen();
    }

    public int capacidad() {
        return Config.getCapacidad();
    }

    public int disponibles() {
        return capacidad() - (int) ocupacionActual();
    }

    public Map<FormaPago, BigDecimal> totalesPorFormaPago(LocalDate fecha) {
        return repo.totalesPorFormaPago(fecha);
    }

    public Map<EstadoTicket, Long> cantidadPorEstado(LocalDate fecha) {
        return repo.cantidadPorEstado(fecha);
    }

    public BigDecimal recaudacionNeta(LocalDate fecha) {
        return repo.recaudacionNeta(fecha);
    }

    public List<TicketEstadia> listado(LocalDateTime desde, LocalDateTime hasta,
                                       String patenteLike, EstadoTicket estado,
                                       TipoVehiculo tipo, BigDecimal importeMin, BigDecimal importeMax) {
        return repo.search(desde, hasta, patenteLike, estado, tipo, importeMin, importeMax);
    }

    public File exportarCSV(List<TicketEstadia> tickets, File destino) throws IOException {
        try (FileWriter fw = new FileWriter(destino)) {
            fw.write("id,nro_ticket,patente,tipo_vehiculo,hora_entrada,hora_salida,minutos,tarifa_aplicada,importe,forma_pago,estado,motivo,creado_en,cerrado_en\n");
            for (TicketEstadia t : tickets) {
                fw.write(String.join(",",
                        n(t.getId()),
                        s(t.getNroTicket()),
                        s(t.getPatente()),
                        s(t.getTipoVehiculo() != null ? t.getTipoVehiculo().name() : ""),
                        s(t.getHoraEntrada()),
                        s(t.getHoraSalida()),
                        n(t.getMinutosEstadia()),
                        s(t.getTarifaAplicada()),
                        n(t.getImporteCalculado()),
                        s(t.getFormaPago() != null ? t.getFormaPago().name() : ""),
                        s(t.getEstado() != null ? t.getEstado().name() : ""),
                        s(t.getMotivoAnulacion()),
                        s(t.getCreadoEn()),
                        s(t.getCerradoEn())
                ));
                fw.write("\n");
            }
        }
        return destino;
    }

    private String s(Object o) {
        if (o == null) return "";
        String raw = String.valueOf(o);
        String escaped = raw.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    private String n(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}