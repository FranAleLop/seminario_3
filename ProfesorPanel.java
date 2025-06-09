package taichi.gui;

import taichi.controller.ProfesorController;
import taichi.model.Profesor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ProfesorPanel extends JPanel {

    private ProfesorController profesorController;

    // Componentes de la UI
    private JTextField txtIdProfesor;
    private JTextField txtNombreCompleto;
    private JTextField txtDni;
    private JTextField txtFechaNacimiento; // Formato YYYY-MM-DD
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtFechaContratacion; // Formato YYYY-MM-DD
    private JCheckBox chkActivo;

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnDarDeBaja;
    private JButton btnActivar;
    private JButton btnLimpiar;

    private JTable profesorTable;
    private DefaultTableModel tableModel;

    // Formateador de fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ProfesorPanel(ProfesorController profesorController) {
        this.profesorController = profesorController;
        setLayout(new BorderLayout());

        // --- Panel de Formulario ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Profesor"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Márgenes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inicializar componentes
        txtIdProfesor = new JTextField(10);
        txtIdProfesor.setEditable(false);
        txtNombreCompleto = new JTextField(20);
        txtDni = new JTextField(15);
        txtFechaNacimiento = new JTextField(10);
        txtDireccion = new JTextField(25);
        txtTelefono = new JTextField(15);
        txtEmail = new JTextField(20);
        txtFechaContratacion = new JTextField(10);
        chkActivo = new JCheckBox("Activo");

        // Añadir etiquetas y campos al formulario
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("ID Profesor:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtIdProfesor, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Nombre Completo:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtNombreCompleto, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("DNI:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtDni, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Fecha Nac. (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtFechaNacimiento, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtDireccion, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtTelefono, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Fecha Contr. (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtFechaContratacion, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; formPanel.add(chkActivo, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // --- Panel de Botones ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar Nuevo");
        btnActualizar = new JButton("Actualizar");
        btnDarDeBaja = new JButton("Dar de Baja");
        btnActivar = new JButton("Activar Profesor");
        btnLimpiar = new JButton("Limpiar Campos");

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnDarDeBaja);
        buttonPanel.add(btnActivar);
        buttonPanel.add(btnLimpiar);

        // Añadir paneles de formulario y botones al panel principal
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Configuración de la Tabla ---
        String[] columnNames = {"ID", "Nombre Completo", "DNI", "Fecha Nac.", "Dirección", "Teléfono", "Email", "Fecha Contr.", "Activo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que las celdas de la tabla no sean editables
            }
        };
        profesorTable = new JTable(tableModel);
        profesorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(profesorTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Listeners de Eventos ---
        btnGuardar.addActionListener(e -> guardarProfesor());
        btnActualizar.addActionListener(e -> actualizarProfesor());
        btnDarDeBaja.addActionListener(e -> darDeBajaProfesor());
        btnActivar.addActionListener(e -> activarProfesor());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        profesorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && profesorTable.getSelectedRow() != -1) {
                mostrarProfesorSeleccionado();
            }
        });

        // Cargar profesores al iniciar el panel
        cargarProfesores();
    }

    private void guardarProfesor() {
        try {
            if (txtNombreCompleto.getText().isEmpty() || txtDni.getText().isEmpty() ||
                txtFechaNacimiento.getText().isEmpty() || txtFechaContratacion.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre, DNI, Fecha Nacimiento y Fecha Contratación son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate fechaNacimiento = LocalDate.parse(txtFechaNacimiento.getText(), DATE_FORMATTER);
            LocalDate fechaContratacion = LocalDate.parse(txtFechaContratacion.getText(), DATE_FORMATTER);

            Profesor nuevoProfesor = profesorController.registrarNuevoProfesor(
                    txtNombreCompleto.getText(),
                    txtDni.getText(),
                    fechaNacimiento,
                    txtDireccion.getText(),
                    txtTelefono.getText(),
                    txtEmail.getText(),
                    fechaContratacion,
                    chkActivo.isSelected()
            );

            JOptionPane.showMessageDialog(this, "Profesor guardado con éxito. ID: " + nuevoProfesor.getIdProfesor(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarProfesores();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use YYYY-MM-DD.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar profesor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void actualizarProfesor() {
        if (txtIdProfesor.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un profesor de la tabla para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idProfesor = Integer.parseInt(txtIdProfesor.getText());
            if (txtNombreCompleto.getText().isEmpty() || txtDni.getText().isEmpty() ||
                txtFechaNacimiento.getText().isEmpty() || txtFechaContratacion.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre, DNI, Fecha Nacimiento y Fecha Contratación son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate fechaNacimiento = LocalDate.parse(txtFechaNacimiento.getText(), DATE_FORMATTER);
            LocalDate fechaContratacion = LocalDate.parse(txtFechaContratacion.getText(), DATE_FORMATTER);

            Profesor profesorActualizar = new Profesor(
                    idProfesor,
                    txtNombreCompleto.getText(),
                    txtDni.getText(),
                    fechaNacimiento,
                    txtDireccion.getText(),
                    txtTelefono.getText(),
                    txtEmail.getText(),
                    fechaContratacion,
                    chkActivo.isSelected()
            );

            boolean exito = profesorController.actualizarInformacionProfesor(profesorActualizar);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Profesor actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarProfesores();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el profesor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID de profesor inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use YYYY-MM-DD.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar profesor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void darDeBajaProfesor() {
        if (txtIdProfesor.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un profesor de la tabla para dar de baja.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de dar de baja a este profesor?", "Confirmar Baja", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idProfesor = Integer.parseInt(txtIdProfesor.getText());
                boolean exito = profesorController.darDeBajaProfesor(idProfesor);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Profesor dado de baja exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarProfesores();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo dar de baja al profesor.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de profesor inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al dar de baja profesor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void activarProfesor() {
        if (txtIdProfesor.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un profesor de la tabla para activar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de activar a este profesor?", "Confirmar Activación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idProfesor = Integer.parseInt(txtIdProfesor.getText());
                boolean exito = profesorController.activarProfesor(idProfesor);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Profesor activado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarProfesores();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo activar al profesor.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de profesor inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al activar profesor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void limpiarCampos() {
        txtIdProfesor.setText("");
        txtNombreCompleto.setText("");
        txtDni.setText("");
        txtFechaNacimiento.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtFechaContratacion.setText("");
        chkActivo.setSelected(true); // Por defecto, nuevo profesor activo
        profesorTable.clearSelection();
    }

    private void cargarProfesores() {
        tableModel.setRowCount(0);
        try {
            List<Profesor> profesores = profesorController.obtenerTodosLosProfesores();
            for (Profesor profesor : profesores) {
                tableModel.addRow(new Object[]{
                    profesor.getIdProfesor(),
                    profesor.getNombreCompleto(),
                    profesor.getDni(),
                    profesor.getFechaNacimiento().format(DATE_FORMATTER),
                    profesor.getDireccion(),
                    profesor.getTelefono(),
                    profesor.getEmail(),
                    profesor.getFechaContratacion().format(DATE_FORMATTER),
                    profesor.isActivo() ? "Sí" : "No"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar profesores: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void mostrarProfesorSeleccionado() {
        int selectedRow = profesorTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtIdProfesor.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombreCompleto.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtDni.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtFechaNacimiento.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtDireccion.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtTelefono.setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtEmail.setText(tableModel.getValueAt(selectedRow, 6).toString());
            txtFechaContratacion.setText(tableModel.getValueAt(selectedRow, 7).toString());
            chkActivo.setSelected(tableModel.getValueAt(selectedRow, 8).equals("Sí"));
        }
    }
}