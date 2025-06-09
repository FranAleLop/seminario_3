package taichi.gui;

import taichi.controller.AlumnoController;
import taichi.model.Alumno;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AlumnoPanel extends JPanel {

    private AlumnoController alumnoController;

    // Componentes de la UI
    private JTextField txtIdAlumno;
    private JTextField txtNombreCompleto;
    private JTextField txtDni;
    private JTextField txtFechaNacimiento; // Formato YYYY-MM-DD
    private JTextField txtDireccion;
    private JTextField txtTelefono;
    private JTextField txtEmail;
    private JTextField txtFechaInscripcion; // Formato YYYY-MM-DD
    private JCheckBox chkActivo;

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar; // O dar de baja
    private JButton btnLimpiar;
    private JButton btnActivar; // Botón para activar un alumno inactivo

    private JTable alumnoTable;
    private DefaultTableModel tableModel;

    // Formateador de fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public AlumnoPanel(AlumnoController alumnoController) {
        this.alumnoController = alumnoController;
        setLayout(new BorderLayout());

        // --- Panel de Formulario ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Detalles del Alumno"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Márgenes entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inicializar componentes
        txtIdAlumno = new JTextField(10);
        txtIdAlumno.setEditable(false); // El ID se genera automáticamente
        txtNombreCompleto = new JTextField(20);
        txtDni = new JTextField(15);
        txtFechaNacimiento = new JTextField(10);
        txtDireccion = new JTextField(25);
        txtTelefono = new JTextField(15);
        txtEmail = new JTextField(20);
        txtFechaInscripcion = new JTextField(10);
        chkActivo = new JCheckBox("Activo");

        // Añadir etiquetas y campos al formulario
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("ID Alumno:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtIdAlumno, gbc);

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

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Fecha Insc. (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtFechaInscripcion, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; formPanel.add(chkActivo, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        // --- Panel de Botones ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar Nuevo");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Dar de Baja");
        btnLimpiar = new JButton("Limpiar Campos");
        btnActivar = new JButton("Activar Alumno");

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnActivar); // Añadir botón para activar
        buttonPanel.add(btnLimpiar);

        // Añadir paneles de formulario y botones al panel principal
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Configuración de la Tabla ---
        String[] columnNames = {"ID", "Nombre Completo", "DNI", "Fecha Nac.", "Dirección", "Teléfono", "Email", "Fecha Insc.", "Activo"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer que las celdas de la tabla no sean editables
            }
        };
        alumnoTable = new JTable(tableModel);
        alumnoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Solo una fila a la vez
        JScrollPane scrollPane = new JScrollPane(alumnoTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Listeners de Eventos ---
        btnGuardar.addActionListener(e -> guardarAlumno());
        btnActualizar.addActionListener(e -> actualizarAlumno());
        btnEliminar.addActionListener(e -> darDeBajaAlumno());
        btnActivar.addActionListener(e -> activarAlumno()); // Listener para el botón activar
        btnLimpiar.addActionListener(e -> limpiarCampos());

        // Listener para la selección de fila en la tabla
        alumnoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && alumnoTable.getSelectedRow() != -1) {
                mostrarAlumnoSeleccionado();
            }
        });

        // Cargar alumnos al iniciar el panel
        cargarAlumnos();
    }

    private void guardarAlumno() {
        try {
            // Validaciones básicas de la UI antes de llamar al controlador
            if (txtNombreCompleto.getText().isEmpty() || txtDni.getText().isEmpty() ||
                txtFechaNacimiento.getText().isEmpty() || txtFechaInscripcion.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre, DNI, Fecha Nacimiento y Fecha Inscripción son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Parsear fechas
            LocalDate fechaNacimiento = LocalDate.parse(txtFechaNacimiento.getText(), DATE_FORMATTER);
            LocalDate fechaInscripcion = LocalDate.parse(txtFechaInscripcion.getText(), DATE_FORMATTER);

            // Llamar al controlador para registrar el alumno
            Alumno nuevoAlumno = alumnoController.registrarNuevoAlumno(
                    txtNombreCompleto.getText(),
                    txtDni.getText(),
                    fechaNacimiento,
                    txtDireccion.getText(),
                    txtTelefono.getText(),
                    txtEmail.getText(),
                    fechaInscripcion,
                    chkActivo.isSelected()
            );

            JOptionPane.showMessageDialog(this, "Alumno guardado con éxito. ID: " + nuevoAlumno.getIdAlumno(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarAlumnos(); // Recargar la tabla
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use YYYY-MM-DD.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar alumno: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void actualizarAlumno() {
        if (txtIdAlumno.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un alumno de la tabla para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idAlumno = Integer.parseInt(txtIdAlumno.getText());
            // Validaciones básicas de la UI
            if (txtNombreCompleto.getText().isEmpty() || txtDni.getText().isEmpty() ||
                txtFechaNacimiento.getText().isEmpty() || txtFechaInscripcion.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre, DNI, Fecha Nacimiento y Fecha Inscripción son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate fechaNacimiento = LocalDate.parse(txtFechaNacimiento.getText(), DATE_FORMATTER);
            LocalDate fechaInscripcion = LocalDate.parse(txtFechaInscripcion.getText(), DATE_FORMATTER);

            Alumno alumnoActualizar = new Alumno(
                    idAlumno,
                    txtNombreCompleto.getText(),
                    txtDni.getText(),
                    fechaNacimiento,
                    txtDireccion.getText(),
                    txtTelefono.getText(),
                    txtEmail.getText(),
                    fechaInscripcion,
                    chkActivo.isSelected()
            );

            boolean exito = alumnoController.actualizarInformacionAlumno(alumnoActualizar);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Alumno actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarAlumnos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el alumno.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID de alumno inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use YYYY-MM-DD.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar alumno: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void darDeBajaAlumno() {
        if (txtIdAlumno.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un alumno de la tabla para dar de baja.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de dar de baja a este alumno?", "Confirmar Baja", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idAlumno = Integer.parseInt(txtIdAlumno.getText());
                boolean exito = alumnoController.darDeBajaAlumno(idAlumno);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Alumno dado de baja exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarAlumnos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo dar de baja al alumno.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de alumno inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al dar de baja alumno: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void activarAlumno() {
        if (txtIdAlumno.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un alumno de la tabla para activar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de activar a este alumno?", "Confirmar Activación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idAlumno = Integer.parseInt(txtIdAlumno.getText());
                boolean exito = alumnoController.activarAlumno(idAlumno);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Alumno activado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarAlumnos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo activar al alumno.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de alumno inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al activar alumno: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }


    private void limpiarCampos() {
        txtIdAlumno.setText("");
        txtNombreCompleto.setText("");
        txtDni.setText("");
        txtFechaNacimiento.setText("");
        txtDireccion.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtFechaInscripcion.setText("");
        chkActivo.setSelected(true); // Por defecto, nuevo alumno activo
        alumnoTable.clearSelection(); // Desseleccionar cualquier fila
    }

    private void cargarAlumnos() {
        // Limpiar la tabla antes de cargar nuevos datos
        tableModel.setRowCount(0); 
        try {
            List<Alumno> alumnos = alumnoController.obtenerTodosLosAlumnos();
            for (Alumno alumno : alumnos) {
                tableModel.addRow(new Object[]{
                    alumno.getIdAlumno(),
                    alumno.getNombreCompleto(),
                    alumno.getDni(),
                    alumno.getFechaNacimiento().format(DATE_FORMATTER),
                    alumno.getDireccion(),
                    alumno.getTelefono(),
                    alumno.getEmail(),
                    alumno.getFechaInscripcion().format(DATE_FORMATTER),
                    alumno.isActivo() ? "Sí" : "No"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar alumnos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void mostrarAlumnoSeleccionado() {
        int selectedRow = alumnoTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtIdAlumno.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtNombreCompleto.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtDni.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtFechaNacimiento.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtDireccion.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtTelefono.setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtEmail.setText(tableModel.getValueAt(selectedRow, 6).toString());
            txtFechaInscripcion.setText(tableModel.getValueAt(selectedRow, 7).toString());
            chkActivo.setSelected(tableModel.getValueAt(selectedRow, 8).equals("Sí"));
        }
    }
}