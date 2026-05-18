package com.parking.ui;

import com.parking.repository.TicketEstadiaRepository;
import com.parking.service.CalculoTarifaService;
import com.parking.service.ReporteService;
import com.parking.service.TicketService;
import com.parking.ui.components.EntradaSalidaPanel;
import com.parking.ui.components.ReportesPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final ReporteService reporteService;

    private JLabel lblHeader;

    public MainFrame() {
        super("Playa de Estacionamiento - Gestión");
        TicketEstadiaRepository repo = new TicketEstadiaRepository();
        CalculoTarifaService calc = new CalculoTarifaService();
        TicketService ticketService = new TicketService(repo, calc);
        this.reporteService = new ReporteService(repo);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        buildUI(ticketService, calc);
        refreshHeader();
    }

    private void buildUI(TicketService ticketService, CalculoTarifaService calc) {
        setLayout(new BorderLayout(10,10));

        lblHeader = new JLabel("", SwingConstants.CENTER);
        lblHeader.setFont(lblHeader.getFont().deriveFont(Font.BOLD, 16f));
        add(lblHeader, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        Runnable onChanged = this::refreshHeader;
        tabs.addTab("Entradas/Salidas", new com.parking.ui.components.EntradaSalidaPanel(ticketService, calc, onChanged));
        tabs.addTab("Reportes", new com.parking.ui.components.ReportesPanel(reporteService));
        add(tabs, BorderLayout.CENTER);
    }

    private void refreshHeader() {
        long ocup = reporteService.ocupacionActual();
        int cap = reporteService.capacidad();
        int disp = reporteService.disponibles();
        lblHeader.setText("Ocupación: " + ocup + "/" + cap + " | Disponibles: " + disp);
    }
}