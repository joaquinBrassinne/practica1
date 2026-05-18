package com.parking.ui.components;

import com.parking.domain.EstadoTicket;
import com.parking.domain.TicketEstadia;
import com.parking.domain.TipoVehiculo;
import com.parking.service.ReporteService;
import com.parking.ui.models.TicketTableModel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;

public class ReportesPanel extends JPanel {

    private final ReporteService reporteService;
    private TicketTableModel model;
    private JSpinner spDesde;
    private JSpinner spHasta;
    private JTextField txtPatente;
    private JComboBox<EstadoTicket> cmbEstado;
    private JComboBox<TipoVehiculo> cmbTipo;
    private JTextField txtImpMin;
    private JTextField txtImpMax;
    private JLabel lblKPIs;

    public ReportesPanel(ReporteService reporteService) {
        this.reporteService = reporteService;
        buildUI();
        buscar();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10,10));
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));

        spDesde = new JSpinner(new SpinnerDateModel(java.util.Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()), null, null, java.util.Calendar.MINUTE));
        spHasta = new JSpinner(new SpinnerDateModel(java.util.Date.from(LocalDate.now().atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant()), null, null, java.util.Calendar.MINUTE));
        spDesde.setEditor(new JSpinner.DateEditor(spDesde, "yyyy-MM-dd HH:mm"));
        spHasta.setEditor(new JSpinner.DateEditor(spHasta, "yyyy-MM-dd HH:mm"));

        txtPatente = new JTextField(8);
        cmbEstado = new JComboBox<>(EstadoTicket.values());
        cmbEstado.insertItemAt(null, 0);
        cmbEstado.setSelectedIndex(0);
        cmbTipo = new JComboBox<>(TipoVehiculo.values());
        cmbTipo.insertItemAt(null, 0);
        cmbTipo.setSelectedIndex(0);
        txtImpMin = new JTextField(5);
        txtImpMax = new JTextField(5);

        filtros.add(new JLabel("Desde:"));
        filtros.add(spDesde);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(spHasta);
        filtros.add(new JLabel("Patente:"));
        filtros.add(txtPatente);
        filtros.add(new JLabel("Estado:"));
        filtros.add(cmbEstado);
        filtros.add(new JLabel("Tipo:"));
        filtros.add(cmbTipo);
        filtros.add(new JLabel("Imp.Min:"));
        filtros.add(txtImpMin);
        filtros.add(new JLabel("Imp.Max:"));
        filtros.add(txtImpMax);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnCSV = new JButton("Exportar CSV");
        btnBuscar.addActionListener(e -> buscar());
        btnCSV.addActionListener(e -> exportarCSV());
        filtros.add(btnBuscar);
        filtros.add(btnCSV);

        add(filtros, BorderLayout.NORTH);

        model = new TicketTableModel();
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        lblKPIs = new JLabel();
        add(lblKPIs, BorderLayout.SOUTH);
    }

    private void buscar() {
        LocalDateTime desde = LocalDateTime.ofInstant(((java.util.Date)spDesde.getValue()).toInstant(), ZoneId.systemDefault());
        LocalDateTime hasta = LocalDateTime.ofInstant(((java.util.Date)spHasta.getValue()).toInstant(), ZoneId.systemDefault());
        String patente = txtPatente.getText();
        EstadoTicket estado = (EstadoTicket) cmbEstado.getSelectedItem();
        TipoVehiculo tipo = (TipoVehiculo) cmbTipo.getSelectedItem();
        BigDecimal impMin = parseBig(txtImpMin.getText());
        BigDecimal impMax = parseBig(txtImpMax.getText());

        List<TicketEstadia> list = reporteService.listado(desde, hasta, patente, estado, tipo, impMin, impMax);
        model.setData(list);
        actualizarKPIs();
    }

    private void actualizarKPIs() {
        LocalDate hoy = LocalDate.now();
        Map<com.parking.domain.FormaPago, BigDecimal> totales = reporteService.totalesPorFormaPago(hoy);
        Map<EstadoTicket, Long> cant = reporteService.cantidadPorEstado(hoy);
        BigDecimal recNeta = reporteService.recaudacionNeta(hoy);

        StringBuilder sb = new StringBuilder("<html>");
        sb.append("Ocupados: ").append(reporteService.ocupacionActual()).append("/")
                .append(reporteService.capacidad())
                .append(" (Disp: ").append(reporteService.disponibles()).append(")").append(" - ");
        sb.append("Recaudación neta hoy: $ ").append(recNeta.setScale(2)).append(" - ");
        sb.append("Totales por forma de pago: ").append(totales).append(" - ");
        sb.append("Cantidad por estado hoy: ").append(cant);
        sb.append("</html>");
        lblKPIs.setText(sb.toString());
    }

    private void exportarCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("reporte.csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                // Exporta el contenido actualmente listado
                LocalDateTime desde = LocalDateTime.ofInstant(((java.util.Date)spDesde.getValue()).toInstant(), ZoneId.systemDefault());
                LocalDateTime hasta = LocalDateTime.ofInstant(((java.util.Date)spHasta.getValue()).toInstant(), ZoneId.systemDefault());
                List<TicketEstadia> list = reporteService.listado(desde, hasta, txtPatente.getText(),
                        (EstadoTicket) cmbEstado.getSelectedItem(),
                        (TipoVehiculo) cmbTipo.getSelectedItem(),
                        parseBig(txtImpMin.getText()), parseBig(txtImpMax.getText()));
                reporteService.exportarCSV(list, f);
                JOptionPane.showMessageDialog(this, "Exportado: " + f.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private BigDecimal parseBig(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}