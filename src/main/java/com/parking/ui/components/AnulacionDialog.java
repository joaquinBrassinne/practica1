package com.parking.ui.components;

import com.parking.service.TicketService;

import javax.swing.*;
import java.awt.*;

public class AnulacionDialog extends JDialog {

    private final TicketService service;
    private final Long idTicket;
    private JTextArea txtMotivo;
    private boolean success = false;

    public AnulacionDialog(Window owner, TicketService service, Long idTicket) {
        super(owner, "Anulación de Ticket", ModalityType.APPLICATION_MODAL);
        this.service = service;
        this.idTicket = idTicket;
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setLayout(new BorderLayout(10,10));
        txtMotivo = new JTextArea(5, 40);
        add(new JScrollPane(txtMotivo), BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Anular");
        JButton btnCancel = new JButton("Cancelar");
        btnOk.addActionListener(e -> onAnular());
        btnCancel.addActionListener(e -> dispose());
        south.add(btnCancel);
        south.add(btnOk);
        add(south, BorderLayout.SOUTH);
    }

    private void onAnular() {
        try {
            String motivo = txtMotivo.getText();
            service.anular(idTicket, motivo);
            success = true;
            dispose();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() { return success; }
}