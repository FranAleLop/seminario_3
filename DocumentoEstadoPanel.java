package taichi.gui;

import taichi.controller.DocumentoEstadoController;
import taichi.controller.AlumnoController;
import taichi.model.DocumentoEstado;
import taichi.model.Alumno;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

public class DocumentoEstadoPanel extends JPanel {

    private DocumentoEstadoController documentoEstadoController;
    private AlumnoController alumnoController;

    // Componentes de la UI
    private JTextField txtIdDocumentoEstado;
    private JComboBox<Alumno> cmbAlumno; // ComboBox para seleccionar alumno
    private JTextField txtTipoDocumento;
    private JTextField txtFechaPresentacion; // Formato YYYY-MM-DD
    private JCheckBox chkEntregado;
    private JTextArea txtNotas; // Para observaciones adicionales

    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnConsultarDocumentosAlumno; // Nuevo botón para filtrar

    private JTable documentoEstadoTable;
    private DefaultTableModel tableModel;

    // Formateador de fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DocumentoEstadoPanel(DocumentoEstadoController documentoEstadoController, AlumnoController alumnoController) {
        this.documentoEstadoController = documentoEstadoController;
        this.alumnoController = alumnoController;
        setLayout(new BorderLayout());

        // --- Panel de Formulario ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Estado de Documentos del Alumno"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Márgenes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inicializar componentes
        txtIdDocumentoEstado = new JTextField(10);
        txtIdDocumentoEstado.setEditable(false);
        cmbAlumno = new JComboBox<>();
        txtTipoDocumento = new JTextField(20);
        txtFechaPresentacion = new JTextField(10);
        chkEntregado = new JCheckBox("Entregado");
        txtNotas = new JTextArea(3, 20); // 3 filas, 20 columnas
        txtNotas.setLineWrap(true);
        txtNotas.setWrapStyleWord(true);
        JScrollPane scrollNotas = new JScrollPane(txtNotas);


        // Añadir etiquetas y campos al formulario
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("ID Registro:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtIdDocumentoEstado, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Alumno:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(cmbAlumno, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Tipo Documento:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtTipoDocumento, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Fecha Presentación (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtFechaPresentacion, gbc);
        
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; formPanel.add(chkEntregado, gbc);
        gbc.gridwidth = 1; // Reset gridwidth
        row++;

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Notas:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(scrollNotas, gbc);

        // --- Panel de Botones ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar Nuevo");
        btnActualizar = new JButton("Actualizar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar Campos");
        btnConsultarDocumentosAlumno = new JButton("Consultar Documentos de Alumno Seleccionado");

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnActualizar);
        buttonPanel.add(btnEliminar);
        buttonPanel.add(btnLimpiar);
        buttonPanel.add(btnConsultarDocumentosAlumno);

        // Añadir paneles de formulario y botones al panel principal
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Configuración de la Tabla ---
        String[] columnNames = {"ID", "ID Alumno", "Alumno", "Tipo Documento", "Fecha Presentación", "Entregado", "Notas"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        documentoEstadoTable = new JTable(tableModel);
        documentoEstadoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(documentoEstadoTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Listeners de Eventos ---
        btnGuardar.addActionListener(e -> guardarDocumentoEstado());
        btnActualizar.addActionListener(e -> actualizarDocumentoEstado());
        btnEliminar.addActionListener(e -> eliminarDocumentoEstado());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnConsultarDocumentosAlumno.addActionListener(e -> consultarDocumentosAlumnoSeleccionado());

        documentoEstadoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && documentoEstadoTable.getSelectedRow() != -1) {
                mostrarDocumentoEstadoSeleccionado();
            }
        });

        // Cargar datos iniciales
        cargarAlumnosEnComboBox();
        cargarTodosLosDocumentosEstado();
    }

    private void cargarAlumnosEnComboBox() {
        cmbAlumno.removeAllItems();
        try {
            List<Alumno> alumnos = alumnoController.obtenerTodosLosAlumnos();
            for (Alumno alumno : alumnos) {
                cmbAlumno.addItem(alumno);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar alumnos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void guardarDocumentoEstado() {
        try {
            Alumno selectedAlumno = (Alumno) cmbAlumno.getSelectedItem();
            if (selectedAlumno == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un Alumno.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtTipoDocumento.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tipo de Documento es obligatorio.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate fechaPresentacion = null;
            if (!txtFechaPresentacion.getText().isEmpty()) {
                fechaPresentacion = LocalDate.parse(txtFechaPresentacion.getText(), DATE_FORMATTER);
            }

            DocumentoEstado nuevoDocumento = documentoEstadoController.registrarDocumentoEstado(
                    selectedAlumno.getIdAlumno(),
                    txtTipoDocumento.getText(),
                    fechaPresentacion,
                    chkEntregado.isSelected(),
                    txtNotas.getText()
            );

            JOptionPane.showMessageDialog(this, "Documento registrado con éxito. ID: " + nuevoDocumento.getIdDocumentoEstado(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarTodosLosDocumentosEstado();
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use YYYY-MM-DD.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar documento: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void actualizarDocumentoEstado() {
        if (txtIdDocumentoEstado.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un registro de la tabla para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idDocumentoEstado = Integer.parseInt(txtIdDocumentoEstado.getText());
            Alumno selectedAlumno = (Alumno) cmbAlumno.getSelectedItem();
            if (selectedAlumno == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un Alumno.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtTipoDocumento.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tipo de Documento es obligatorio.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate fechaPresentacion = null;
            if (!txtFechaPresentacion.getText().isEmpty()) {
                fechaPresentacion = LocalDate.parse(txtFechaPresentacion.getText(), DATE_FORMATTER);
            }

            DocumentoEstado documentoActualizar = new DocumentoEstado(
                    idDocumentoEstado,
                    selectedAlumno.getIdAlumno(),
                    txtTipoDocumento.getText(),
                    fechaPresentacion,
                    chkEntregado.isSelected(),
                    txtNotas.getText()
            );

            boolean exito = documentoEstadoController.actualizarDocumentoEstado(documentoActualizar);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Documento actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarTodosLosDocumentosEstado();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el documento.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID de registro inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use YYYY-MM-DD.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar documento: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void eliminarDocumentoEstado() {
        if (txtIdDocumentoEstado.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un registro de la tabla para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este registro de documento?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idDocumentoEstado = Integer.parseInt(txtIdDocumentoEstado.getText());
                boolean exito = documentoEstadoController.eliminarDocumentoEstado(idDocumentoEstado);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Documento eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarTodosLosDocumentosEstado();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el documento.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de registro inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar documento: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void limpiarCampos() {
        txtIdDocumentoEstado.setText("");
        cmbAlumno.setSelectedIndex(-1);
        txtTipoDocumento.setText("");
        txtFechaPresentacion.setText("");
        chkEntregado.setSelected(false);
        txtNotas.setText("");
        documentoEstadoTable.clearSelection();
        cargarTodosLosDocumentosEstado(); // Volver a cargar todos los documentos
    }

    private void cargarTodosLosDocumentosEstado() {
        tableModel.setRowCount(0);
        try {
            List<DocumentoEstado> documentos = documentoEstadoController.obtenerTodosLosDocumentosEstado();
            for (DocumentoEstado doc : documentos) {
                Alumno alumno = alumnoController.obtenerAlumnoPorId(doc.getIdAlumno());
                String nombreAlumno = (alumno != null) ? alumno.getNombreCompleto() : "Desconocido";

                tableModel.addRow(new Object[]{
                    doc.getIdDocumentoEstado(),
                    doc.getIdAlumno(),
                    nombreAlumno,
                    doc.getTipoDocumento(),
                    (doc.getFechaPresentacion() != null) ? ((LocalDate) doc.getFechaPresentacion()).format(DATE_FORMATTER) : "",
                    doc.isEntregado() ? "Sí" : "No",
                    doc.getNotas()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar documentos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void consultarDocumentosAlumnoSeleccionado() {
        Alumno selectedAlumno = (Alumno) cmbAlumno.getSelectedItem();
        if (selectedAlumno == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un alumno del ComboBox para consultar sus documentos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setRowCount(0); // Limpiar tabla
        try {
            List<DocumentoEstado> documentos = documentoEstadoController.obtenerDocumentosEstadoPorAlumno(selectedAlumno.getIdAlumno());
            for (DocumentoEstado doc : documentos) {
                tableModel.addRow(new Object[]{
                    doc.getIdDocumentoEstado(),
                    doc.getIdAlumno(),
                    selectedAlumno.getNombreCompleto(), // Ya tenemos el alumno seleccionado
                    doc.getTipoDocumento(),
                    (doc.getFechaPresentacion() != null) ? ((LocalDate) doc.getFechaPresentacion()).format(DATE_FORMATTER) : "",
                    doc.isEntregado() ? "Sí" : "No",
                    doc.getNotas()
                });
            }
            if (documentos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El alumno seleccionado no tiene documentos registrados.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al consultar documentos del alumno: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private void mostrarDocumentoEstadoSeleccionado() {
        int selectedRow = documentoEstadoTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtIdDocumentoEstado.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtTipoDocumento.setText(tableModel.getValueAt(selectedRow, 3).toString());
            
            Object fechaObj = tableModel.getValueAt(selectedRow, 4);
            txtFechaPresentacion.setText((fechaObj != null) ? fechaObj.toString() : "");

            chkEntregado.setSelected(tableModel.getValueAt(selectedRow, 5).equals("Sí"));
            txtNotas.setText(tableModel.getValueAt(selectedRow, 6) != null ? tableModel.getValueAt(selectedRow, 6).toString() : "");

            // Seleccionar el Alumno correcto en el ComboBox
            int idAlumnoEnTabla = (int) tableModel.getValueAt(selectedRow, 1);
            for (int i = 0; i < cmbAlumno.getItemCount(); i++) {
                Alumno alumnoEnCmb = cmbAlumno.getItemAt(i);
                if (alumnoEnCmb != null && alumnoEnCmb.getIdAlumno() == idAlumnoEnTabla) {
                    cmbAlumno.setSelectedItem(alumnoEnCmb);
                    break;
                }
            }
        }
    }
}