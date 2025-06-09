package taichi.gui;

import taichi.controller.ClaseController;
import taichi.model.Clase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClasePanel extends JPanel {

    private ClaseController claseController;

    // Componentes de la UI
    private JTextField txtIdClase;
    private JTextField txtNombreClase;
    private JTextField txtDescripcion;
    private JTextField txtHorario;
    private JTextField txtCupoMaximo;
    private JCheckBox chkActiva;

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnInactivar; // O dar de baja
    private JButton btnActivar;
    private JButton btnLimpiar;

    private JTable claseTable;
    private DefaultTableModel tableModel;

    public ClasePanel(ClaseController claseController) {
        this.claseController = claseController;
        setLayout(new BorderLayout());

        // --- Panel de Formulario ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Detalles de la Clase"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Márgenes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inicializar componentes
        txtIdClase = new JTextField(10);
        txtIdClase.setEditable(false);
        txtNombreClase = new JTextField(20);
        txtDescripcion = new JTextField(30); // Más espacio para descripciones
        txtHorario = new JTextField(25); // Ej: "Lunes y Miércoles 18:00-19:30"
        txtCupoMaximo = new JTextField(5);
        chkActiva = new JCheckBox("Activa");

        // Añadir etiquetas y campos al formulario
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("ID Clase:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtIdClase, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Nombre Clase:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtNombreClase, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtDescripcion, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Horario:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtHorario, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Cupo Máximo:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtCupoMaximo, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; formPanel.add(chkActiva, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // --- Panel de Botones ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar Nueva");
        btnActualizar = new JButton("Actualizar");
        btnInactivar = new JButton("Inactivar Clase");
        btnActivar = new JButton("Activar Clase");
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
        String[] columnNames = {"ID", "Nombre Clase", "Descripción", "Horario", "Cupo Máximo", "Activa"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que las celdas de la tabla no sean editables
            }
        };
        claseTable = new JTable(tableModel);
        claseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(claseTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Listeners de Eventos ---
        btnGuardar.addActionListener(e -> guardarClase());
        btnActualizar.addActionListener(e -> actualizarClase());
        btnInactivar.addActionListener(e -> inactivarClase());
        btnActivar.addActionListener(e -> activarClase());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        claseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && claseTable.getSelectedRow() != -1) {
                mostrarClaseSeleccionada();
            }
        });

        // Cargar clases al iniciar el panel
        cargarClases();
    }

    private void guardarClase() {
        try {
            if (txtNombreClase.getText().isEmpty() || txtHorario.getText().isEmpty() || txtCupoMaximo.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre de Clase, Horario y Cupo Máximo son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int cupoMaximo = Integer.parseInt(txtCupoMaximo.getText());

            Clase nuevaClase = claseController.registrarNuevaClase(
                    txtNombreClase.getText(),
                    txtDescripcion.getText(),
                    txtHorario.getText(),
                    cupoMaximo,
                    chkActiva.isSelected()
            );

            JOptionPane.showMessageDialog(this, "Clase guardada con éxito. ID: " + nuevaClase.getIdClase(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarClases();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El Cupo Máximo debe ser un número entero válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar clase: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void actualizarClase() {
        if (txtIdClase.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una clase de la tabla para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idClase = Integer.parseInt(txtIdClase.getText());
            if (txtNombreClase.getText().isEmpty() || txtHorario.getText().isEmpty() || txtCupoMaximo.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre de Clase, Horario y Cupo Máximo son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int cupoMaximo = Integer.parseInt(txtCupoMaximo.getText());

            Clase claseActualizar = new Clase(
                    idClase,
                    txtNombreClase.getText(),
                    txtDescripcion.getText(),
                    txtHorario.getText(),
                    cupoMaximo,
                    chkActiva.isSelected()
            );

            boolean exito = claseController.actualizarInformacionClase(claseActualizar);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Clase actualizada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarClases();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar la clase.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El Cupo Máximo debe ser un número entero válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar clase: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void inactivarClase() {
        if (txtIdClase.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una clase de la tabla para inactivar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de inactivar esta clase?", "Confirmar Inactivación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idClase = Integer.parseInt(txtIdClase.getText());
                boolean exito = claseController.inactivarClase(idClase);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Clase inactivada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarClases();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo inactivar la clase.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de clase inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al inactivar clase: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void activarClase() {
        if (txtIdClase.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una clase de la tabla para activar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de activar esta clase?", "Confirmar Activación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idClase = Integer.parseInt(txtIdClase.getText());
                boolean exito = claseController.activarClase(idClase);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Clase activada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarClases();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo activar la clase.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de clase inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al activar clase: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void limpiarCampos() {
        txtIdClase.setText("");
        txtNombreClase.setText("");
        txtDescripcion.setText("");
        txtHorario.setText("");
        txtCupoMaximo.setText("");
        chkActiva.setSelected(true); // Por defecto, nueva clase activa
        claseTable.clearSelection();
    }

    private void cargarClases() {
        tableModel.setRowCount(0);
        try {
            List<Clase> clases = claseController.obtenerTodasLasClases();
            for (Clase clase : clases) {
                tableModel.addRow(new Object[]{
                    clase.getIdClase(),
                    clase.getNombreClase(),
                    clase.getDescripcion(),
                    clase.getHorario(),
                    clase.getCupoMaximo(),
                    clase.isActiva() ? "Sí" : "No"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar clases: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void mostrarClaseSeleccionada() {
        int selectedRow = claseTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtIdClase.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombreClase.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtDescripcion.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtHorario.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtCupoMaximo.setText(tableModel.getValueAt(selectedRow, 4).toString());
            chkActiva.setSelected(tableModel.getValueAt(selectedRow, 5).equals("Sí"));
        }
    }
}