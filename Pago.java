package taichi.model;

import java.time.LocalDate; // Necesario para manejar fechas

public class Pago {

    // Atributos (variables de instancia)
    // Corresponden a las columnas de la tabla 'Pagos'
    private int idPago;                     // id_pago (clave primaria)
    private int idAlumno;                   // id_alumno (clave foránea a Alumnos)
    private int idPeriodo;                  // id_periodo (clave foránea a PeriodosCuota)
    private LocalDate fechaPago;            // fecha_pago
    private double montoPagado;             // monto_pagado
    private String tipoPago;                // tipo_pago (ej. "Efectivo", "Transferencia")
    private boolean esPagoParcial;          // es_pago_parcial (TRUE/FALSE)
    private double montoRecargoAplicado;    // monto_recargo_aplicado

    // --- Constructores ---

    // Constructor vacío
    public Pago(int idPago2, int i, int j, double montoPagado2, LocalDate fechaPago2, String string) {
    }

    // Constructor sin idPago (para crear un NUEVO pago antes de insertarlo en la BD)
    public Pago(int idAlumno, int idPeriodo, LocalDate fechaPago, double montoPagado,
                String tipoPago, boolean esPagoParcial, double montoRecargoAplicado) {
        this.idAlumno = idAlumno;
        this.idPeriodo = idPeriodo;
        this.fechaPago = fechaPago;
        this.montoPagado = montoPagado;
        this.tipoPago = tipoPago;
        this.esPagoParcial = esPagoParcial;
        this.montoRecargoAplicado = montoRecargoAplicado;
    }

    // Constructor con idPago (para cuando recuperas un pago de la BD)
    public Pago(int idPago, int idAlumno, int idPeriodo, LocalDate fechaPago, double montoPagado,
                String tipoPago, boolean esPagoParcial, double montoRecargoAplicado) {
        this.idPago = idPago;
        this.idAlumno = idAlumno;
        this.idPeriodo = idPeriodo;
        this.fechaPago = fechaPago;
        this.montoPagado = montoPagado;
        this.tipoPago = tipoPago;
        this.esPagoParcial = esPagoParcial;
        this.montoRecargoAplicado = montoRecargoAplicado;
    }

    // --- Métodos Getters y Setters (Encapsulamiento) ---

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(int idAlumno) {
        this.idAlumno = idAlumno;
    }

    public int getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(int idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public boolean isEsPagoParcial() { // Para booleanos, el getter suele ser 'isNombre'
        return esPagoParcial;
    }

    public void setEsPagoParcial(boolean esPagoParcial) {
        this.esPagoParcial = esPagoParcial;
    }

    public double getMontoRecargoAplicado() {
        return montoRecargoAplicado;
    }

    public void setMontoRecargoAplicado(double montoRecargoAplicado) {
        this.montoRecargoAplicado = montoRecargoAplicado;
    }

    @Override
    public String toString() {
        return "Pago{" +
               "idPago=" + idPago +
               ", idAlumno=" + idAlumno +
               ", idPeriodo=" + idPeriodo +
               ", fechaPago=" + fechaPago +
               ", montoPagado=" + montoPagado +
               ", tipoPago='" + tipoPago + '\'' +
               ", esPagoParcial=" + esPagoParcial +
               ", montoRecargoAplicado=" + montoRecargoAplicado +
               '}';
    }

    public Object getObservaciones() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getObservaciones'");
    }
}