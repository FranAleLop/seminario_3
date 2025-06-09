package taichi.gui;

import taichi.controller.PagoController;
import taichi.controller.AlumnoController;
import taichi.controller.PeriodoCuotaController;
import taichi.model.Pago;
import taichi.model.Alumno;
import taichi.model.PeriodoCuota;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects; // Para el combobox de Alumno

public class PagoPanel extends JPanel {

    private PagoController pagoController;
    private AlumnoController alumnoController; // Necesario para cargar alumnos
    private PeriodoCuotaController periodoCuotaController; // Necesario para cargar períodos

    // Componentes de la UI
    private JTextField txtIdPago;
    private JComboBox<Alumno> cmbAlumno; // ComboBox para seleccionar alumno
    private JComboBox<PeriodoCuota> cmbPeriodoCuota; // ComboBox para seleccionar período
    private JTextField txtMontoPagado;
    private JTextField txtFechaPago; // Formato YYYY-MM-DD
    private JTextArea txtObservaciones; // Para notas adicionales

    private JButton btnRegistrarPago;
    private JButton btnActualizarPago;
    private JButton btnEliminarPago;
    private JButton btnLimpiar;
    private JButton btnConsultarPagosAlumno; // Nuevo botón para consultar pagos por alumno

    private JTable pagoTable;
    private DefaultTableModel tableModel;

    // Formateador de fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PagoPanel(PagoController pagoController, AlumnoController alumnoController, PeriodoCuotaController periodoCuotaController) {
        this.pagoController = pagoController;
        this.alumnoController = alumnoController;
        this.periodoCuotaController = periodoCuotaController;
        setLayout(new BorderLayout());

        // --- Panel de Formulario ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registro de Pago"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Márgenes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Inicializar componentes
        txtIdPago = new JTextField(10);
        txtIdPago.setEditable(false);
        cmbAlumno = new JComboBox<>();
        cmbPeriodoCuota = new JComboBox<>();
        txtMontoPagado = new JTextField(10);
        txtFechaPago = new JTextField(10);
        txtObservaciones = new JTextArea(3, 20); // 3 filas, 20 columnas
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        JScrollPane scrollObservaciones = new JScrollPane(txtObservaciones);

        // Añadir etiquetas y campos al formulario
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("ID Pago:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtIdPago, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Alumno:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(cmbAlumno, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Período Cuota:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(cmbPeriodoCuota, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Monto Pagado:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtMontoPagado, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Fecha Pago (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(txtFechaPago, gbc);

        gbc.gridx = 0; gbc.gridy = row; formPanel.add(new JLabel("Observaciones:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; formPanel.add(scrollObservaciones, gbc);

        // --- Panel de Botones ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnRegistrarPago = new JButton("Registrar Pago");
        btnActualizarPago = new JButton("Actualizar Pago");
        btnEliminarPago = new JButton("Eliminar Pago");
        btnLimpiar = new JButton("Limpiar Campos");
        btnConsultarPagosAlumno = new JButton("Consultar Pagos de Alumno Seleccionado");

        buttonPanel.add(btnRegistrarPago);
        buttonPanel.add(btnActualizarPago);
        buttonPanel.add(btnEliminarPago);
        buttonPanel.add(btnLimpiar);
        buttonPanel.add(btnConsultarPagosAlumno);


        // Añadir paneles de formulario y botones al panel principal
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Configuración de la Tabla ---
        String[] columnNames = {"ID Pago", "ID Alumno", "Alumno", "ID Período", "Período", "Monto", "Fecha Pago", "Observaciones"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pagoTable = new JTable(tableModel);
        pagoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(pagoTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Listeners de Eventos ---
        btnRegistrarPago.addActionListener(e -> registrarPago());
        btnActualizarPago.addActionListener(e -> actualizarPago());
        btnEliminarPago.addActionListener(e -> eliminarPago());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnConsultarPagosAlumno.addActionListener(e -> consultarPagosAlumnoSeleccionado());

        pagoTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && pagoTable.getSelectedRow() != -1) {
                mostrarPagoSeleccionado();
            }
        });

        // Cargar datos iniciales
        cargarAlumnosEnComboBox();
        cargarPeriodosCuotaEnComboBox();
        cargarTodosLosPagos(); // Carga todos los pagos al inicio
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

    private void cargarPeriodosCuotaEnComboBox() {
        cmbPeriodoCuota.removeAllItems();
        try {
            List<PeriodoCuota> periodos = periodoCuotaController.obtenerTodosLosPeriodosCuota();
            for (PeriodoCuota periodo : periodos) {
                cmbPeriodoCuota.addItem(periodo);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar períodos de cuota: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void registrarPago() {
        try {
            Alumno selectedAlumno = (Alumno) cmbAlumno.getSelectedItem();
            PeriodoCuota selectedPeriodo = (PeriodoCuota) cmbPeriodoCuota.getSelectedItem();

            if (selectedAlumno == null || selectedPeriodo == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un Alumno y un Período de Cuota.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtMontoPagado.getText().isEmpty() || txtFechaPago.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Monto Pagado y Fecha de Pago son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double montoPagado = Double.parseDouble(txtMontoPagado.getText());
            LocalDate fechaPago = LocalDate.parse(txtFechaPago.getText(), DATE_FORMATTER);

            Pago nuevoPago = pagoController.registrarPago(
                    selectedAlumno.getIdAlumno(),
                    selectedPeriodo.getIdPeriodo(),
                    montoPagado,
                    fechaPago,
                    txtObservaciones.getText()
            );

            JOptionPane.showMessageDialog(this, "Pago registrado con éxito. ID: " + nuevoPago.getIdPago(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            cargarTodosLosPagos(); // Recargar la tabla con todos los pagos
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Monto Pagado debe ser un valor numérico válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use YYYY-MM-DD.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al registrar pago: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void actualizarPago() {
        if (txtIdPago.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un pago de la tabla para actualizar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int idPago = Integer.parseInt(txtIdPago.getText());
            Alumno selectedAlumno = (Alumno) cmbAlumno.getSelectedItem();
            PeriodoCuota selectedPeriodo = (PeriodoCuota) cmbPeriodoCuota.getSelectedItem();

            if (selectedAlumno == null || selectedPeriodo == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un Alumno y un Período de Cuota.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtMontoPagado.getText().isEmpty() || txtFechaPago.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Monto Pagado y Fecha de Pago son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double montoPagado = Double.parseDouble(txtMontoPagado.getText());
            LocalDate fechaPago = LocalDate.parse(txtFechaPago.getText(), DATE_FORMATTER);

            Pago pagoActualizar = new Pago(
                    idPago,
                    selectedAlumno.getIdAlumno(),
                    selectedPeriodo.getIdPeriodo(),
                    montoPagado,
                    fechaPago,
                    txtObservaciones.getText()
            );

            boolean exito = pagoController.actualizarPago(pagoActualizar);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Pago actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarTodosLosPagos();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el pago.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID de pago o Monto Pagado inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use YYYY-MM-DD.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Error de validación: " + e.getMessage(), "Error de Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar pago: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void eliminarPago() {
        if (txtIdPago.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un pago de la tabla para eliminar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este pago?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int idPago = Integer.parseInt(txtIdPago.getText());
                boolean exito = pagoController.eliminarPago(idPago);
                if (exito) {
                    JOptionPane.showMessageDialog(this, "Pago eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarTodosLosPagos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el pago.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID de pago inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar pago: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void limpiarCampos() {
        txtIdPago.setText("");
        cmbAlumno.setSelectedIndex(-1); // Desseleccionar
        cmbPeriodoCuota.setSelectedIndex(-1); // Desseleccionar
        txtMontoPagado.setText("");
        txtFechaPago.setText("");
        txtObservaciones.setText("");
        pagoTable.clearSelection();
        cargarTodosLosPagos(); // Vuelve a cargar todos los pagos después de limpiar
    }

    private void cargarTodosLosPagos() {
        tableModel.setRowCount(0);
        try {
            List<Pago> pagos = pagoController.obtenerTodosLosPagos();
            for (Pago pago : pagos) {
                // Para mostrar los nombres en la tabla, necesitamos obtener el Alumno y PeriodoCuota
                Alumno alumno = alumnoController.obtenerAlumnoPorId(pago.getIdAlumno());
                PeriodoCuota periodoCuota = periodoCuotaController.obtenerPeriodoCuotaPorId(pago.getIdPeriodo());

                String nombreAlumno = (alumno != null) ? alumno.getNombreCompleto() : "Desconocido";
                String nombrePeriodo = (periodoCuota != null) ? periodoCuota.getNombrePeriodo() : "Desconocido";

                tableModel.addRow(new Object[]{
                    pago.getIdPago(),
                    pago.getIdAlumno(),
                    nombreAlumno,
                    pago.getIdPeriodo(),
                    nombrePeriodo,
                    String.format("%.2f", pago.getMontoPagado()),
                    pago.getFechaPago().format(DATE_FORMATTER),
                    pago.getObservaciones()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar pagos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void consultarPagosAlumnoSeleccionado() {
        Alumno selectedAlumno = (Alumno) cmbAlumno.getSelectedItem();
        if (selectedAlumno == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un alumno del ComboBox para consultar sus pagos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.setRowCount(0); // Limpiar tabla
        try {
            List<Pago> pagos = pagoController.obtenerPagosPorAlumno(selectedAlumno.getIdAlumno());
            for (Pago pago : pagos) {
                PeriodoCuota periodoCuota = periodoCuotaController.obtenerPeriodoCuotaPorId(pago.getIdPeriodo());
                String nombrePeriodo = (periodoCuota != null) ? periodoCuota.getNombrePeriodo() : "Desconocido";

                tableModel.addRow(new Object[]{
                    pago.getIdPago(),
                    pago.getIdAlumno(),
                    selectedAlumno.getNombreCompleto(), // Ya tenemos el alumno seleccionado
                    pago.getIdPeriodo(),
                    nombrePeriodo,
                    String.format("%.2f", pago.getMontoPagado()),
                    pago.getFechaPago().format(DATE_FORMATTER),
                    pago.getObservaciones()
                });
            }
            if (pagos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El alumno seleccionado no tiene pagos registrados.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al consultar pagos del alumno: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private void mostrarPagoSeleccionado() {
        int selectedRow = pagoTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtIdPago.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtMontoPagado.setText(tableModel.getValueAt(selectedRow, 5).toString().replace(",", "."));
            txtFechaPago.setText(tableModel.getValueAt(selectedRow, 6).toString());
            txtObservaciones.setText(tableModel.getValueAt(selectedRow, 7) != null ? tableModel.getValueAt(selectedRow, 7).toString() : "");

            // Seleccionar el Alumno correcto en el ComboBox
            int idAlumnoEnTabla = (int) tableModel.getValueAt(selectedRow, 1);
            for (int i = 0; i < cmbAlumno.getItemCount(); i++) {
                Alumno alumnoEnCmb = cmbAlumno.getItemAt(i);
                if (alumnoEnCmb != null && alumnoEnCmb.getIdAlumno() == idAlumnoEnTabla) {
                    cmbAlumno.setSelectedItem(alumnoEnCmb);
                    break;
                }
            }
            
            // Seleccionar el Período de Cuota correcto en el ComboBox
            int idPeriodoEnTabla = (int) tableModel.getValueAt(selectedRow, 3);
            for (int i = 0; i < cmbPeriodoCuota.getItemCount(); i++) {
                PeriodoCuota periodoEnCmb = cmbPeriodoCuota.getItemAt(i);
                if (periodoEnCmb != null && periodoEnCmb.getIdPeriodo() == idPeriodoEnTabla) {
                    cmbPeriodoCuota.setSelectedItem(periodoEnCmb);
                    break;
                }
            }
        }
    }
}