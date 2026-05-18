package com.parking.ui.components;

import com.parking.domain.FormaPago;
import com.parking.domain.TicketEstadia;
import com.parking.service.CalculoTarifaService;
import com.parking.service.TicketService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class CheckoutDialog extends JDialog {

    private final TicketEstadia ticket;
    private final TicketService ticketService;
    private final CalculoTarifaService calculo;
    private JLabel lblDetalle;
    private JComboBox<FormaPago> cmbPago;
    private boolean success = false;

    public CheckoutDialog(Window owner, TicketEstadia ticket, TicketService service, CalculoTarifaService calculo) {
        super(owner, "Cobro / Salida", ModalityType.APPLICATION_MODAL);
        this.ticket = ticket;
        this.ticketService = service;
        this.calculo = calculo;
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout(10,10));
        JPanel p = new JPanel(new GridLayout(0,1,5,5));
        CalculoTarifaService.ResultadoCalculo r = calculo.calcular(ticket.getTipoVehiculo(), ticket.getHoraEntrada(), java.time.LocalDateTime.now());

        lblDetalle = new JLabel("<html>Patente: <b>" + ticket.getPatente() + "</b><br/>" +
                "Duración: " + r.minutosTotales + " min (Cobrables: " + r.minutosCobrables + " min)<br/>" +
                "Bloques: " + r.bloques + " - Tarifa: " + r.tarifaAplicada + "<br/>" +
                "Importe: <b>$ " + formatMoney(r.importeFinal) + "</b></html>");
        p.add(lblDetalle);

        JPanel pagar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pagar.add(new JLabel("Forma de pago:"));
        cmbPago = new JComboBox<>(FormaPago.values());
        pagar.add(cmbPago);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Confirmar y Cerrar");
        JButton btnCancel = new JButton("Cancelar");
        btnOk.addActionListener(e -> onConfirm());
        btnCancel.addActionListener(e -> dispose());
        south.add(btnCancel);
        south.add(btnOk);

        add(p, BorderLayout.CENTER);
        add(pagar, BorderLayout.NORTH);
        add(south, BorderLayout.SOUTH);
    }

    private String formatMoney(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2).toPlainString();
    }

    private void onConfirm() {
        try {
            ticketService.registrarSalida(ticket.getId(), (FormaPago) cmbPago.getSelectedItem());
            success = true;
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }
}