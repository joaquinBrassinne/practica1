package com.parking.service;

import com.parking.domain.TipoVehiculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;

public class CalculoTarifaService {

    public static class ResultadoCalculo {
        public final int minutosTotales;
        public final int minutosCobrables;
        public final int bloques;
        public final BigDecimal importeBase;
        public final BigDecimal importeAjustado;
        public final BigDecimal importeFinal;
        public final String tarifaAplicada;

        public ResultadoCalculo(int minutosTotales, int minutosCobrables, int bloques,
                                BigDecimal importeBase, BigDecimal importeAjustado, BigDecimal importeFinal,
                                String tarifaAplicada) {
            this.minutosTotales = minutosTotales;
            this.minutosCobrables = minutosCobrables;
            this.bloques = bloques;
            this.importeBase = importeBase;
            this.importeAjustado = importeAjustado;
            this.importeFinal = importeFinal;
            this.tarifaAplicada = tarifaAplicada;
        }
    }

    public ResultadoCalculo calcular(TipoVehiculo tipo, LocalDateTime entrada, LocalDateTime salida) {
        if (salida.isBefore(entrada)) throw new IllegalArgumentException("La hora de salida no puede ser anterior a la de entrada");
        int gracia = Config.getMinutosGracia();
        int bloque = Config.getTamanoBloqueMinutos();
        BigDecimal tarifaHora = Config.getTarifaPorHora(tipo);
        BigDecimal tope = Config.getTopeDiario();

        int minutosTotales = (int) Duration.between(entrada, salida).toMinutes();
        if (minutosTotales <= gracia) {
            return new ResultadoCalculo(minutosTotales, 0, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    buildTarifaAplicada(tipo));
        }
        int minutosCobrables = minutosTotales - gracia;
        int bloques = (int) Math.ceil(minutosCobrables / (double) bloque);
        BigDecimal precioBloque = tarifaHora.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal importeBase = precioBloque.multiply(BigDecimal.valueOf(bloques));

        BigDecimal importeAjustado = importeBase;
        if (Config.isNocturnoHabilitado()) {
            int nocturnoInicio = Config.getNocturnoInicioHora();
            int nocturnoFin = Config.getNocturnoFinHora();
            BigDecimal factor = Config.getNocturnoFactor(); // 0.8

            // Prorrateo por minutos
            int minutosNocturnos = minutosEnFranjaNocturna(entrada.plusMinutes(gracia), salida, nocturnoInicio, nocturnoFin);
            int minutosConsiderados = Math.max(minutosCobrables, 1);
            BigDecimal proporcionNocturna = BigDecimal.valueOf(minutosNocturnos)
                    .divide(BigDecimal.valueOf(minutosConsiderados), 6, RoundingMode.HALF_UP);

            BigDecimal descuentoProporcional = BigDecimal.ONE.subtract(factor).multiply(proporcionNocturna);
            // importe ajustado = base * (1 - descProp)
            importeAjustado = importeBase.multiply(BigDecimal.ONE.subtract(descuentoProporcional))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal importeFinal = importeAjustado.min(tope).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        return new ResultadoCalculo(minutosTotales, minutosCobrables, bloques,
                importeBase.setScale(2, RoundingMode.HALF_UP),
                importeAjustado, importeFinal,
                buildTarifaAplicada(tipo));
    }

    private String buildTarifaAplicada(TipoVehiculo tipo) {
        YearMonth ym = YearMonth.now();
        return tipo.name() + "_STD_" + ym.getYear() + "Q" + ((ym.getMonthValue()-1)/3 + 1);
    }

    // Cuenta minutos dentro de la franja nocturna [inicio: 22, fin: 6] soportando cruces de medianoche
    static int minutosEnFranjaNocturna(LocalDateTime desde, LocalDateTime hasta, int nocturnoInicio, int nocturnoFin) {
        if (!hasta.isAfter(desde)) return 0;

        int total = 0;
        LocalDateTime cursor = desde;
        while (cursor.isBefore(hasta)) {
            LocalDateTime siguiente = cursor.plusMinutes(1);
            int hour = cursor.getHour();
            boolean esNocturno;
            if (nocturnoInicio > nocturnoFin) {
                // Ej: 22 -> 6 (cruza medianoche): nocturno si hour >= 22 || hour < 6
                esNocturno = hour >= nocturnoInicio || hour < nocturnoFin;
            } else {
                esNocturno = hour >= nocturnoInicio && hour < nocturnoFin;
            }
            if (esNocturno) total++;
            cursor = siguiente;
        }
        return total;
    }
}