package taichi.gui;

import taichi.controller.PeriodoCuotaController;
import taichi.model.PeriodoCuota;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.Normalizer.Form;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PeriodoCuotaPanel extends JPanel {

    private PeriodoCuotaController periodoCuotaController;

    // Componentes de la UI
    private JTextField txtIdPeriodo;
    private JTextField txtNombrePeriodo;
    private JTextField txtMontoBase;
    private JTextField txtMontoRecargo;
    private JTextField txtFechaVencimiento; // Formato YYYY-MM-DD
    private JCheckBox chkActivo;

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnInactivar;
    private JButton btnActivar;
    private JButton btnLimpiar;

    private JTable periodoCuotaTable;
    private DefaultTableModel tableModel;

    // Formateador de fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PeriodoCuotaPanel(PeriodoCuotaController periodoCuotaController) {
        this.periodoCuotaController = periodoCuotaController;
        setLayout(new BorderLayout());

        // --- Panel de Formulario ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Período de Cuota"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Márgenes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inicializar componentes
        txtIdPeriodo = new JTextField(10);
        txtIdPeriodo.setEditable(false);
        txtNombrePeriodo = new JTextField(20);
        txtMontoBase = new JTextField(10);
        txtMontoRecargo = new JTextField(10);
        txtFechaVencimiento = new JTextField(10);
        chkActivo = new JCheckBox("Activo");

        // Añadir etiquetas y campos al formulario
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("ID Período:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtIdPeriodo, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Nombre Período:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtNombrePeriodo, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Monto Base:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtMontoBase, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Monto Recargo:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtMontoRecargo, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Fecha Venc. (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtFechaVencimiento, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; formPanel.add(chkActivo, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // --- Panel de Botones ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar Nuevo");
        btnActualizar = new JButton("Actualizar");
        btnInactivar = new JButton("Inactivar Período");
        btnActivar = new JButton("Activar Período");
        btnLimpiar = new JButton("Limpiar Campos");

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnInactivar);
        buttonPanel.add(btnActivar);
        buttonPanel.add(btnLimpiar);

        // Añadir paneles de formulario y botones al panel principal
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Configuración de la Tabla ---
        String[] columnNames = {"ID", "Nombre Período", "Monto Base", "Monto Recargo", "Fecha Venc.", "Activo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que las celdas de la tabla no sean editables
            }
        };
        periodoCuotaTable = new JTable(tableModel);
        periodoCuotaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(periodoCuotaTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Listeners de Eventos ---
        btnGuardar.addActionListener(e -> guardarPeriodoCuota());
        btnActualizar.addActionListener(e -> actualizarPeriodoCuota());
        btnInactivar.addActionListener(e -> inactivarPeriodoCuota());
        btnActivar.addActionListener(e -> activarPeriodoCuota());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        periodoCuotaTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && periodoCuotaTable.getSelectedRow() != -1) {
                mostrarPeriodoCuotaSeleccionado();
            }
        });

        // Cargar períodos de cuota al iniciar el panel
        cargarPeriodosCuota();
    }

    private void guardarPeriodoCuota() {
        try {
            if (txtNombrePeriodo.getText().isEmpty() || txtMontoBase.getText().isEmpty() ||
                txtMontoRecargo.getText().isEmpty() || txtFechaVencimiento.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double montoBase = Double.parseDouble(txtMontoBase.getText());
            double montoRecargo = Double.parseDouble(txtMontoRecargo.getText());
            LocalDate fechaVencimiento = LocalDate.parse(txtFechaVencimiento.getText(), DATE_FORMATTER);

            PeriodoCuota nuevoPeriodo = periodoCuotaController.registrarNuevoPeriodoCuota(
                    txtNombrePeriodo.getText(),
                    montoBase,
                    montoRecargo,
                    fechaVencimiento,
                    chkActivo.isSelected()
            );

            JOptionPane.showMessageDialog(this, "Período de Cuota guardado con éxito. ID: " + nuevoPeriodo.getIdPeriodo(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarPeriodosCuota();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Monto Base y Monto Recargo deben ser valores numéricos válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha de vencimiento inválido. Use YYYY-MM-DD.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar período de cuota: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void actualizarPeriodoCuota() {
        if (txtIdPeriodo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un período de cuota de la tabla para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idPeriodo = Integer.parseInt(txtIdPeriodo.getText());
            if (txtNombrePeriodo.getText().isEmpty() || txtMontoBase.getText().isEmpty() ||
                txtMontoRecargo.getText().isEmpty() || txtFechaVencimiento.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double montoBase = Double.parseDouble(txtMontoBase.getText());
            double montoRecargo = Double.parseDouble(txtMontoRecargo.getText());
            LocalDate fechaVencimiento = LocalDate.parse(txtFechaVencimiento.getText(), DATE_FORMATTER);

            PeriodoCuota periodoActualizar = new PeriodoCuota(
                    idPeriodo,
                    txtNombrePeriodo.getText(),
                    montoBase,
                    fechaVencimiento,
                    chkActivo.isSelected()
            );

            boolean exito = periodoCuotaController.actualizarInformacionPeriodoCuota(periodoActualizar);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Período de Cuota actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarPeriodosCuota();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el período de cuota.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Monto Base y Monto Recargo deben ser valores numéricos válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha de vencimiento inválido. Use YYYY-MM-DD.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar período de cuota: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void inactivarPeriodoCuota() {
        if (txtIdPeriodo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un período de cuota de la tabla para inactivar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de inactivar este período de cuota?", "Confirmar Inactivación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idPeriodo = Integer.parseInt(txtIdPeriodo.getText());
                boolean exito = periodoCuotaController.inactivarPeriodoCuota(idPeriodo);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Período de Cuota inactivado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarPeriodosCuota();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo inactivar el período de cuota.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de período inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al inactivar período de cuota: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void activarPeriodoCuota() {
        if (txtIdPeriodo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un período de cuota de la tabla para activar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de activar este período de cuota?", "Confirmar Activación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idPeriodo = Integer.parseInt(txtIdPeriodo.getText());
                boolean exito = periodoCuotaController.activarPeriodoCuota(idPeriodo);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Período de Cuota activado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarPeriodosCuota();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo activar el período de cuota.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de período inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al activar período de cuota: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void limpiarCampos() {
        txtIdPeriodo.setText("");
        txtNombrePeriodo.setText("");
        txtMontoBase.setText("");
        txtMontoRecargo.setText("");
        txtFechaVencimiento.setText("");
        chkActivo.setSelected(true); // Por defecto, nuevo período activo
        periodoCuotaTable.clearSelection();
    }

    private void cargarPeriodosCuota() {
        tableModel.setRowCount(0);
        try {
            List<PeriodoCuota> periodos = periodoCuotaController.obtenerTodosLosPeriodosCuota();
            for (PeriodoCuota periodo : periodos) {
                tableModel.addRow(new Object[]{
                    periodo.getIdPeriodo(),
                    periodo.getNombrePeriodo(),
                    String.format("%.2f", periodo.getMontoBase()), // Formato para decimales
                    String.format("%.2f", periodo.getMontoRecargo()), // Formato para decimales
                    periodo.getFechaVencimiento().format(DATE_FORMATTER),
                    periodo.isActivo() ? "Sí" : "No"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar períodos de cuota: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void mostrarPeriodoCuotaSeleccionado() {
        int selectedRow = periodoCuotaTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtIdPeriodo.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombrePeriodo.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtMontoBase.setText(tableModel.getValueAt(selectedRow, 2).toString().replace(",", ".")); // Asegurarse de usar punto como decimal
            txtMontoRecargo.setText(tableModel.getValueAt(selectedRow, 3).toString().replace(",", ".")); // Asegurarse de usar punto como decimal
            txtFechaVencimiento.setText(tableModel.getValueAt(selectedRow, 4).toString());
            chkActivo.setSelected(tableModel.getValueAt(selectedRow, 5).equals("Sí"));
        }
    }
}