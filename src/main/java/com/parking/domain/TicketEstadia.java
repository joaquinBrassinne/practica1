package com.parking.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets_estadia",
       indexes = {
           @Index(name="idx_patente_estado", columnList = "patente, estado"),
           @Index(name="idx_estado_entrada", columnList = "estado, hora_entrada"),
           @Index(name="idx_entrada", columnList = "hora_entrada")
       })
public class TicketEstadia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nro_ticket", length=20, unique=true, nullable=false)
    private String nroTicket;

    @Column(name="patente", length=10, nullable=false)
    private String patente;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo_vehiculo", length=20, nullable=false, columnDefinition = "ENUM('AUTO','MOTO','CAMIONETA')")
    private TipoVehiculo tipoVehiculo;

    @Column(name="hora_entrada", nullable=false)
    private LocalDateTime horaEntrada;

    @Column(name="hora_salida")
    private LocalDateTime horaSalida;

    @Column(name="minutos_estadia")
    private Integer minutosEstadia;

    @Column(name="tarifa_aplicada", length=50)
    private String tarifaAplicada;

    @Column(name="importe_calculado", precision = 10, scale = 2)
    private BigDecimal importeCalculado;

    @Enumerated(EnumType.STRING)
    @Column(name="forma_pago", length=20, columnDefinition = "ENUM('EFECTIVO','TARJETA','TRANSFERENCIA')")
    private FormaPago formaPago;

    @Enumerated(EnumType.STRING)
    @Column(name="estado", length=20, nullable=false, columnDefinition = "ENUM('ABIERTO','CERRADO','ANULADO')")
    private EstadoTicket estado;

    @Column(name="motivo_anulacion", length=255)
    private String motivoAnulacion;

    @Column(name="observaciones", length=255)
    private String observaciones;

    @Column(name="creado_en", nullable=false)
    private LocalDateTime creadoEn;

    @Column(name="cerrado_en")
    private LocalDateTime cerradoEn;

    @PrePersist
    public void prePersist() {
        if (creadoEn == null) creadoEn = LocalDateTime.now();
        if (estado == null) estado = EstadoTicket.ABIERTO;
        if (horaEntrada == null) horaEntrada = LocalDateTime.now();
        if (patente != null) patente = patente.trim().toUpperCase();
    }

    @PreUpdate
    public void preUpdate() {
        if (patente != null) patente = patente.trim().toUpperCase();
    }

    // Getters y setters
    public Long getId() { return id; }
    public String getNroTicket() { return nroTicket; }
    public void setNroTicket(String nroTicket) { this.nroTicket = nroTicket; }
    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }
    public TipoVehiculo getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(TipoVehiculo tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }
    public LocalDateTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalDateTime horaEntrada) { this.horaEntrada = horaEntrada; }
    public LocalDateTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalDateTime horaSalida) { this.horaSalida = horaSalida; }
    public Integer getMinutosEstadia() { return minutosEstadia; }
    public void setMinutosEstadia(Integer minutosEstadia) { this.minutosEstadia = minutosEstadia; }
    public String getTarifaAplicada() { return tarifaAplicada; }
    public void setTarifaAplicada(String tarifaAplicada) { this.tarifaAplicada = tarifaAplicada; }
    public BigDecimal getImporteCalculado() { return importeCalculado; }
    public void setImporteCalculado(BigDecimal importeCalculado) { this.importeCalculado = importeCalculado; }
    public FormaPago getFormaPago() { return formaPago; }
    public void setFormaPago(FormaPago formaPago) { this.formaPago = formaPago; }
    public EstadoTicket getEstado() { return estado; }
    public void setEstado(EstadoTicket estado) { this.estado = estado; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
    public void setMotivoAnulacion(String motivoAnulacion) { this.motivoAnulacion = motivoAnulacion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getCerradoEn() { return cerradoEn; }
    public void setCerradoEn(LocalDateTime cerradoEn) { this.cerradoEn = cerradoEn; }
}