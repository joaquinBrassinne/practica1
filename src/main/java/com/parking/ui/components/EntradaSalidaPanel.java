package com.parking.ui.components;

import com.parking.domain.TicketEstadia;
import com.parking.domain.TipoVehiculo;
import com.parking.repository.TicketEstadiaRepository;
import com.parking.service.CalculoTarifaService;
import com.parking.service.DomainException;
import com.parking.service.TicketService;
import com.parking.ui.models.TicketTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EntradaSalidaPanel extends JPanel {

    private final TicketService ticketService;
    private final CalculoTarifaService calculo;
    private JTextField txtPatente;
    private JComboBox<TipoVehiculo> cmbTipo;
    private TicketTableModel tableModel;
    private Runnable onChanged;

    public EntradaSalidaPanel(TicketService service, CalculoTarifaService calculo, Runnable onChanged) {
        this.ticketService = service;
        this.calculo = calculo;
        this.onChanged = onChanged;
        buildUI();
        refreshTabla();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10,10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Patente:"));
        txtPatente = new JTextField(8);
        form.add(txtPatente);
        form.add(new JLabel("Tipo:"));
        cmbTipo = new JComboBox<>(TipoVehiculo.values());
        form.add(cmbTipo);
        JButton btnEntrada = new JButton("Registrar Entrada");
        btnEntrada.addActionListener(e -> registrarEntrada());
        form.add(btnEntrada);

        add(form, BorderLayout.NORTH);

        tableModel = new TicketTableModel();
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalida = new JButton("Registrar Salida/Cobro");
        JButton btnAnular = new JButton("Anular Ticket");
        actions.add(btnAnular);
        actions.add(btnSalida);

        btnSalida.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un ticket ABIERTO"); return; }
            TicketEstadia t = tableModel.getAt(row);
            CheckoutDialog dlg = new CheckoutDialog(SwingUtilities.getWindowAncestor(this), t, ticketService, calculo);
            dlg.setVisible(true);
            if (dlg.isSuccess()) {
                refreshTabla();
                onChanged.run();
            }
        });

        btnAnular.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un ticket"); return; }
            TicketEstadia t = tableModel.getAt(row);
            AnulacionDialog dlg = new AnulacionDialog(SwingUtilities.getWindowAncestor(this), ticketService, t.getId());
            dlg.setVisible(true);
            if (dlg.isSuccess()) {
                refreshTabla();
                onChanged.run();
            }
        });

        add(actions, BorderLayout.SOUTH);
    }

    private void registrarEntrada() {
        try {
            String patente = txtPatente.getText();
            TipoVehiculo tipo = (TipoVehiculo) cmbTipo.getSelectedItem();
            ticketService.registrarEntrada(patente, tipo);
            txtPatente.setText("");
            refreshTabla();
            onChanged.run();
        } catch (DomainException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTabla() {
        List<TicketEstadia> abiertos = ticketService.listarAbiertos();
        tableModel.setData(abiertos);
    }
}