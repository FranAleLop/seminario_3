package taichi.gui;

import taichi.controller.PagoController;
import taichi.controller.AlumnoController;
import taichi.model.Pago;
import taichi.model.Alumno;
import taichi.model.PeriodoCuota;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map; // Para el reporte de deudas

public class ReportesPagosPanel extends JPanel {

    private PagoController pagoController;
    private AlumnoController alumnoController;

    // Componentes de la UI
    private JComboBox<String> cmbTipoReporte;
    private JButton btnGenerarReporte;

    private JTable reportesTable;
    private DefaultTableModel tableModel;

    // Formateador de fechas
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ReportesPagosPanel(PagoController pagoController, AlumnoController alumnoController) {
        this.pagoController = pagoController;
        this.alumnoController = alumnoController;
        setLayout(new BorderLayout());

        // --- Panel de Controles ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Selección de Reporte"));

        String[] tiposReporte = {
            "Alumnos que Pagaron este Mes",
            "Alumnos que No Pagaron este Mes",
            "Alumnos con Deuda (Meses Anteriores)",
            "Ganancias de este Mes"
        };
        cmbTipoReporte = new JComboBox<>(tiposReporte);
        btnGenerarReporte = new JButton("Generar Reporte");

        controlPanel.add(new JLabel("Seleccione Tipo de Reporte:"));
        controlPanel.add(cmbTipoReporte);
        controlPanel.add(btnGenerarReporte);

        add(controlPanel, BorderLayout.NORTH);

        // --- Configuración de la Tabla de Reportes ---
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportesTable = new JTable(tableModel);
        reportesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(reportesTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Listener de Eventos ---
        btnGenerarReporte.addActionListener(e -> generarReporteSeleccionado());

        // Cargar un reporte por defecto al inicio (opcional, podrías dejarla vacía)
        // generarReporteAlumnosPagaronEsteMes();
    }

    private void generarReporteSeleccionado() {
        String tipoSeleccionado = (String) cmbTipoReporte.getSelectedItem();
        if (tipoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un tipo de reporte.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        switch (tipoSeleccionado) {
            case "Alumnos que Pagaron este Mes":
                generarReporteAlumnosPagaronEsteMes();
                break;
            case "Alumnos que No Pagaron este Mes":
                generarReporteAlumnosNoPagaronEsteMes();
                break;
            case "Alumnos con Deuda (Meses Anteriores)":
                generarReporteAlumnosConDeudaAnterior();
                break;
            case "Ganancias de este Mes":
                generarReporteGananciasMensuales();
                break;
            default:
                JOptionPane.showMessageDialog(this, "Tipo de reporte no reconocido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setTableColumns(String[] columnNames) {
        tableModel.setColumnIdentifiers(columnNames);
        tableModel.setRowCount(0); // Limpiar filas existentes
    }

    private void generarReporteAlumnosPagaronEsteMes() {
        setTableColumns(new String[]{"ID Alumno", "Nombre Alumno", "Monto Total Pagado (este mes)"});
        YearMonth currentMonth = YearMonth.now();

        try {
            // Suponemos que PagoController tiene un método para esto.
            // Si no, lo crearemos en la siguiente sección.
            Map<Alumno, Double> pagosEsteMes = pagoController.obtenerPagosAgrupadosPorAlumnoYMes(currentMonth);

            if (pagosEsteMes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ningún alumno ha realizado pagos este mes.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (Map.Entry<Alumno, Double> entry : pagosEsteMes.entrySet()) {
                Alumno alumno = entry.getKey();
                Double montoPagado = entry.getValue();
                tableModel.addRow(new Object[]{
                    alumno.getIdAlumno(),
                    alumno.getNombreCompleto(),
                    String.format("%.2f", montoPagado)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte de alumnos que pagaron: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void generarReporteAlumnosNoPagaronEsteMes() {
        setTableColumns(new String[]{"ID Alumno", "Nombre Alumno"});
        YearMonth currentMonth = YearMonth.now();

        try {
            // Suponemos que AlumnoController tiene un método para esto,
            // que internamente usa PagoController.
            List<Alumno> alumnosNoPagaron = alumnoController.obtenerAlumnosNoPagaronEnMes(currentMonth);

            if (alumnosNoPagaron.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los alumnos han pagado este mes (o no hay alumnos registrados).", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (Alumno alumno : alumnosNoPagaron) {
                tableModel.addRow(new Object[]{
                    alumno.getIdAlumno(),
                    alumno.getNombreCompleto()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte de alumnos que no pagaron: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void generarReporteAlumnosConDeudaAnterior() {
        setTableColumns(new String[]{"ID Alumno", "Nombre Alumno", "Períodos Adeudados", "Monto Total Adeudado"});
        YearMonth currentMonth = YearMonth.now();

        try {
            // Suponemos que AlumnoController tiene un método que devuelve un mapa de alumnos y sus deudas.
            Map<Alumno, Map<PeriodoCuota, Double>> alumnosConDeuda = alumnoController.obtenerAlumnosConDeudaAnteriorA(currentMonth);

            if (alumnosConDeuda.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay alumnos con deudas de meses anteriores.", "Información", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            for (Map.Entry<Alumno, Map<PeriodoCuota, Double>> entry : alumnosConDeuda.entrySet()) {
                Alumno alumno = entry.getKey();
                Map<PeriodoCuota, Double> deudas = entry.getValue();

                StringBuilder periodosAdeudados = new StringBuilder();
                double montoTotalAdeudado = 0.0;

                for (Map.Entry<PeriodoCuota, Double> deudaEntry : deudas.entrySet()) {
                    periodosAdeudados.append(deudaEntry.getKey().getNombrePeriodo())
                                     .append(" ($")
                                     .append(String.format("%.2f", deudaEntry.getValue()))
                                     .append("), ");
                    montoTotalAdeudado += deudaEntry.getValue();
                }
                // Eliminar la última coma y espacio si hay períodos
                if (periodosAdeudados.length() > 0) {
                    periodosAdeudados.setLength(periodosAdeudados.length() - 2);
                }

                tableModel.addRow(new Object[]{
                    alumno.getIdAlumno(),
                    alumno.getNombreCompleto(),
                    periodosAdeudados.toString(),
                    String.format("%.2f", montoTotalAdeudado)
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte de deudas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void generarReporteGananciasMensuales() {
        setTableColumns(new String[]{"Mes/Año", "Ganancia Total"});
        YearMonth currentMonth = YearMonth.now();

        try {
            double ganancias = pagoController.obtenerGananciasPorMes(currentMonth);
            tableModel.addRow(new Object[]{
                currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                String.format("%.2f", ganancias)
            });
            JOptionPane.showMessageDialog(this, "Reporte de ganancias generado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte de ganancias: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}