package taichi.model;

import java.time.LocalDate; // Necesario para manejar fechas

public class PeriodoCuota {

    // Atributos (variables de instancia)
    // Corresponden a las columnas de la tabla 'PeriodosCuota'
    private int idPeriodo;               // id_periodo (clave primaria)
    private String nombrePeriodo;        // nombre_periodo (ej. "Marzo 2025")
    private LocalDate fechaInicio;       // fecha_inicio
    private LocalDate fechaFin;          // fecha_fin
    private LocalDate fechaVencimiento;  // fecha_vencimiento (para pago sin recargo)
    private double montoBase;            // monto_base (monto de la cuota sin recargo)
    private double montoRecargo;         // monto_recargo (monto fijo del recargo)

    // --- Constructores ---

    // Constructor vacío
    public PeriodoCuota(int idPeriodo2, String string, double montoBase2, LocalDate fechaVencimiento2, boolean b) {
    }

    // Constructor sin idPeriodo (para crear un NUEVO período antes de insertarlo en la BD)
    public PeriodoCuota(String nombrePeriodo2, LocalDate fechaInicio2, LocalDate fechaFin2,
                        LocalDate fechaVencimiento2, double montoBase2, double montoRecargo2) {
        this.nombrePeriodo = nombrePeriodo2;
        this.fechaInicio = fechaInicio2;
        this.fechaFin = fechaFin2;
        this.fechaVencimiento = fechaVencimiento2;
        this.montoBase = montoBase2;
        this.montoRecargo = montoRecargo2;
    }

    // Constructor con idPeriodo (para cuando recuperas un período de la BD)
    public PeriodoCuota(int idPeriodo, String nombrePeriodo, LocalDate fechaInicio, LocalDate fechaFin,
                        LocalDate fechaVencimiento, double montoBase, double montoRecargo) {
        this.idPeriodo = idPeriodo;
        this.nombrePeriodo = nombrePeriodo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaVencimiento = fechaVencimiento;
        this.montoBase = montoBase;
        this.montoRecargo = montoRecargo;
    }

    // --- Métodos Getters y Setters (Encapsulamiento) ---

    public int getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(int idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public String getNombrePeriodo() {
        return nombrePeriodo;
    }

    public void setNombrePeriodo(String nombrePeriodo) {
        this.nombrePeriodo = nombrePeriodo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public double getMontoBase() {
        return montoBase;
    }

    public void setMontoBase(double montoBase) {
        this.montoBase = montoBase;
    }

    public double getMontoRecargo() {
        return montoRecargo;
    }

    public void setMontoRecargo(double montoRecargo) {
        this.montoRecargo = montoRecargo;
    }

    @Override
    public String toString() {
        return "PeriodoCuota{" +
               "idPeriodo=" + idPeriodo +
               ", nombrePeriodo='" + nombrePeriodo + '\'' +
               ", fechaInicio=" + fechaInicio +
               ", fechaFin=" + fechaFin +
               ", fechaVencimiento=" + fechaVencimiento +
               ", montoBase=" + montoBase +
               ", montoRecargo=" + montoRecargo +
               '}';
    }

    public boolean isActivo() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isActivo'");
    }
}