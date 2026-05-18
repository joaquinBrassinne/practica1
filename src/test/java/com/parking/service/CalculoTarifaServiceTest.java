package com.parking.service;

import com.parking.domain.TipoVehiculo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CalculoTarifaServiceTest {

    private final CalculoTarifaService svc = new CalculoTarifaService();

    @Test
    void testGracia8Min() {
        LocalDateTime e = LocalDateTime.of(2025,10,1,8,0);
        LocalDateTime s = e.plusMinutes(8);
        var r = svc.calcular(TipoVehiculo.AUTO, e, s);
        assertEquals(0, r.importeFinal.compareTo(BigDecimal.ZERO));
    }

    @Test
    void test43MinAuto() {
        LocalDateTime e = LocalDateTime.of(2025,10,1,8,0);
        LocalDateTime s = e.plusMinutes(43);
        var r = svc.calcular(TipoVehiculo.AUTO, e, s);
        assertEquals(new BigDecimal("1200.00"), r.importeFinal);
        assertEquals(2, r.bloques);
    }

    @Test
    void test70Min() {
        LocalDateTime e = LocalDateTime.of(2025,10,1,8,0);
        LocalDateTime s = e.plusMinutes(70);
        var r = svc.calcular(TipoVehiculo.AUTO, e, s);
        assertEquals(new BigDecimal("1200.00"), r.importeFinal);
        assertEquals(2, r.bloques);
    }

    @Test
    void testTopeDiario() {
        LocalDateTime e = LocalDateTime.of(2025,10,1,8,0);
        LocalDateTime s = e.plusHours(24);
        var r = svc.calcular(TipoVehiculo.CAMIONETA, e, s);
        // Debe topear a 8000
        assertEquals(0, r.importeFinal.compareTo(new BigDecimal("8000.00")));
    }
}