package taichi.controller;

import taichi.model.PeriodoCuota;
import taichi.dao.PeriodoCuotaDAO;
import taichi.util.InputValidator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeriodoCuotaController {

    private PeriodoCuotaDAO periodoCuotaDAO;
    private static final Logger LOGGER = Logger.getLogger(PeriodoCuotaController.class.getName());

    public PeriodoCuotaController() {
        this.periodoCuotaDAO = new PeriodoCuotaDAO();
    }

    /**
     * Registra un nuevo período de cuota en el sistema.
     * Realiza validaciones antes de intentar insertar.
     * @param nombrePeriodo Nombre descriptivo del período (ej. "Cuota Marzo 2025").
     * @param fechaInicio Fecha de inicio del período.
     * @param fechaFin Fecha de fin del período.
     * @param fechaVencimiento Fecha de vencimiento para el pago sin recargo.
     * @param montoBase Monto base de la cuota.
     * @param montoRecargo Monto adicional en caso de pago tardío.
     * @return El objeto PeriodoCuota recién creado y con su ID asignado, o null si falla.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public PeriodoCuota registrarNuevoPeriodoCuota(String nombrePeriodo, LocalDate fechaInicio,
                                                 LocalDate fechaFin, LocalDate fechaVencimiento,
                                                 double montoBase, double montoRecargo) throws Exception {
        
        // --- 1. Validaciones de entrada ---
        if (InputValidator.isNullOrEmpty(nombrePeriodo)) {
            throw new IllegalArgumentException("El nombre del período es obligatorio.");
        }
        if (fechaInicio == null || fechaFin == null || fechaVencimiento == null) {
            throw new IllegalArgumentException("Las fechas de inicio, fin y vencimiento son obligatorias.");
        }
        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        if (fechaVencimiento.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser posterior a la fecha de fin del período.");
        }
        if (montoBase <= 0) {
            throw new IllegalArgumentException("El monto base de la cuota debe ser un valor positivo.");
        }
        if (montoRecargo < 0) {
            throw new IllegalArgumentException("El monto de recargo no puede ser negativo.");
        }
        
        // Puedes añadir una validación para asegurar que el período no se superponga con otros existentes.
        // Esto sería una lógica de negocio más avanzada que implicaría consultar todos los períodos y comparar fechas.

        // --- 2. Crear el objeto PeriodoCuota ---
        PeriodoCuota nuevoPeriodo = new PeriodoCuota(nombrePeriodo, fechaInicio, fechaFin, fechaVencimiento, montoBase, montoRecargo);

        // --- 3. Llamar al DAO para insertar en la BD ---
        try {
            int idGenerado = periodoCuotaDAO.insertar(nuevoPeriodo);
            if (idGenerado != -1) {
                nuevoPeriodo.setIdPeriodo(idGenerado);
                return nuevoPeriodo;
            } else {
                throw new Exception("No se pudo insertar el período de cuota en la base de datos.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar nuevo período de cuota en la BD: " + e.getMessage(), e);
            throw new Exception("Error al registrar período de cuota: " + e.getMessage());
        }
    }

    /**
     * Obtiene la información de un período de cuota por su ID.
     * @param idPeriodo ID del período a buscar.
     * @return El objeto PeriodoCuota si se encuentra, o null.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public PeriodoCuota obtenerPeriodoCuotaPorId(int idPeriodo) throws Exception {
        if (!InputValidator.isPositive(idPeriodo)) {
            throw new IllegalArgumentException("El ID del período debe ser un número positivo.");
        }
        try {
            return periodoCuotaDAO.obtenerPorId(idPeriodo);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener período de cuota por ID " + idPeriodo + " de la BD: " + e.getMessage(), e);
            throw new Exception("Error al consultar período de cuota por ID: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de todos los períodos de cuota.
     * @return Lista de objetos PeriodoCuota.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<PeriodoCuota> obtenerTodosLosPeriodosCuota() throws Exception {
        try {
            return periodoCuotaDAO.obtenerTodos(); 
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los períodos de cuota de la BD: " + e.getMessage(), e);
            throw new Exception("Error al obtener listado de períodos de cuota: " + e.getMessage());
        }
    }

    /**
     * Actualiza la información de un período de cuota existente.
     * @param periodo El objeto PeriodoCuota con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public boolean actualizarInformacionPeriodoCuota(PeriodoCuota periodo) throws Exception {
        // --- 1. Validaciones de entrada ---
        if (periodo == null) {
            throw new IllegalArgumentException("El objeto PeriodoCuota no puede ser nulo.");
        }
        if (!InputValidator.isPositive(periodo.getIdPeriodo())) {
            throw new IllegalArgumentException("El ID del período es obligatorio para la actualización.");
        }
        if (InputValidator.isNullOrEmpty(periodo.getNombrePeriodo())) {
            throw new IllegalArgumentException("El nombre del período es obligatorio.");
        }
        if (periodo.getFechaInicio() == null || periodo.getFechaFin() == null || periodo.getFechaVencimiento() == null) {
            throw new IllegalArgumentException("Las fechas de inicio, fin y vencimiento son obligatorias.");
        }
        if (periodo.getFechaInicio().isAfter(periodo.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        if (periodo.getFechaVencimiento().isAfter(periodo.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser posterior a la fecha de fin del período.");
        }
        if (periodo.getMontoBase() <= 0) {
            throw new IllegalArgumentException("El monto base de la cuota debe ser un valor positivo.");
        }
        if (periodo.getMontoRecargo() < 0) {
            throw new IllegalArgumentException("El monto de recargo no puede ser negativo.");
        }

        // --- 2. Llamar al DAO para actualizar en la BD ---
        try {
            return periodoCuotaDAO.actualizar(periodo);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar período de cuota con ID " + periodo.getIdPeriodo() + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al actualizar período de cuota: " + e.getMessage());
        }
    }

    /**
     * Elimina un período de cuota por su ID.
     * NOTA: Esta operación debe manejarse con cuidado, ya que los pagos están asociados a períodos.
     * Una eliminación física podría romper la integridad referencial si hay pagos asociados.
     * En un sistema real, se preferiría una "baja lógica" o verificar dependencias.
     * @param idPeriodo ID del período a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean eliminarPeriodoCuota(int idPeriodo) throws Exception {
        if (!InputValidator.isPositive(idPeriodo)) {
            throw new IllegalArgumentException("El ID del período debe ser un número positivo.");
        }
        try {
            // **IMPORTANTE**: Aquí deberías añadir lógica para verificar si hay pagos asociados
            // a este período. Si los hay, la eliminación directa causaría un error de integridad.
            // Opción 1: Bloquear la eliminación.
            // Opción 2: Marcar el período como inactivo (si tuvieras un campo 'activo' en PeriodosCuota).
            // Opción 3: Eliminar en cascada (generalmente no recomendado para datos financieros).
            
            // Por simplicidad para el prototipo, se permite la eliminación directa, pero tenlo en cuenta.
            
            return periodoCuotaDAO.eliminar(idPeriodo);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar período de cuota con ID " + idPeriodo + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al eliminar período de cuota: " + e.getMessage());
        }
    }

    public PeriodoCuota registrarNuevoPeriodoCuota(String text, double montoBase, double montoRecargo,
            LocalDate fechaVencimiento, boolean selected) {
        
        throw new UnsupportedOperationException("Unimplemented method 'registrarNuevoPeriodoCuota'");
    }

    public boolean inactivarPeriodoCuota(int idPeriodo) {
        
        throw new UnsupportedOperationException("Unimplemented method 'inactivarPeriodoCuota'");
    }

    public boolean activarPeriodoCuota(int idPeriodo) {
         
        throw new UnsupportedOperationException("Unimplemented method 'activarPeriodoCuota'");
    }
}