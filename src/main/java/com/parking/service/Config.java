package com.parking.service;

import com.parking.domain.TipoVehiculo;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class Config {

    private static final Properties props = new Properties();
    private static final Map<TipoVehiculo, BigDecimal> tarifaPorHora = new EnumMap<>(TipoVehiculo.class);

    static {
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("appconfig.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            // ignore, use defaults
        }
        tarifaPorHora.put(TipoVehiculo.AUTO, getDecimal("tarifa.auto.por_hora", "1200"));
        tarifaPorHora.put(TipoVehiculo.MOTO, getDecimal("tarifa.moto.por_hora", "700"));
        tarifaPorHora.put(TipoVehiculo.CAMIONETA, getDecimal("tarifa.camioneta.por_hora", "1500"));
    }

    public static int getCapacidad() {
        return Integer.parseInt(props.getProperty("capacidad", "100"));
    }

    public static int getMinutosGracia() {
        return Integer.parseInt(props.getProperty("tarifa.gracia.minutos", "10"));
    }

    public static int getTamanoBloqueMinutos() {
        return Integer.parseInt(props.getProperty("tarifa.bloque.minutos", "30"));
    }

    public static BigDecimal getTopeDiario() {
        return getDecimal("tarifa.tope.diario", "8000");
    }

    public static boolean isNocturnoHabilitado() {
        return Boolean.parseBoolean(props.getProperty("tarifa.nocturno.habilitado", "false"));
    }

    public static int getNocturnoInicioHora() {
        return Integer.parseInt(props.getProperty("tarifa.nocturno.inicio.hora", "22"));
    }

    public static int getNocturnoFinHora() {
        return Integer.parseInt(props.getProperty("tarifa.nocturno.fin.hora", "6"));
    }

    public static BigDecimal getNocturnoFactor() {
        return getDecimal("tarifa.nocturno.factor", "0.8"); // 80%
    }

    public static BigDecimal getTarifaPorHora(TipoVehiculo tipo) {
        return tarifaPorHora.get(tipo);
    }

    public static String getDbUrl() {
        return props.getProperty("db.url", "jdbc:mysql://localhost:3306/parking?serverTimezone=UTC");
    }

    public static String getDbUser() {
        return props.getProperty("db.user", "root");
    }

    public static String getDbPassword() {
        return props.getProperty("db.password", "root");
    }

    private static BigDecimal getDecimal(String key, String def) {
        return new BigDecimal(props.getProperty(key, def));
    }

    private Config() {}
}