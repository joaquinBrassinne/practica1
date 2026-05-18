package com.parking.ui.models;

import com.parking.domain.TicketEstadia;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class TicketTableModel extends AbstractTableModel {

    private final String[] cols = {"ID", "Ticket", "Patente", "Tipo", "Entrada", "Estado", "Importe"};
    private final List<TicketEstadia> data = new ArrayList<>();

    public void setData(List<TicketEstadia> list) {
        data.clear();
        if (list != null) data.addAll(list);
        fireTableDataChanged();
    }

    public TicketEstadia getAt(int row) {
        return data.get(row);
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int column) { return cols[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TicketEstadia t = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> t.getId();
            case 1 -> t.getNroTicket();
            case 2 -> t.getPatente();
            case 3 -> t.getTipoVehiculo();
            case 4 -> t.getHoraEntrada();
            case 5 -> t.getEstado();
            case 6 -> t.getImporteCalculado();
            default -> "";
        };
    }
}