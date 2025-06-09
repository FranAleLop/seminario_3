package taichi.controller;

import taichi.model.Pago;
import taichi.model.Alumno;
import taichi.model.PeriodoCuota;
import taichi.dao.PagoDAO;
import taichi.dao.AlumnoDAO; // Necesario para verificar existencia del alumno
import taichi.dao.PeriodoCuotaDAO; // Necesario para obtener detalles del periodo
import taichi.util.InputValidator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PagoController {

    private PagoDAO pagoDAO;
    private AlumnoDAO alumnoDAO; // Para validar la existencia del alumno
    private PeriodoCuotaDAO periodoCuotaDAO; // Para obtener los detalles del período y calcular recargos
    private static final Logger LOGGER = Logger.getLogger(PagoController.class.getName());

    public PagoController() {
        this.pagoDAO = new PagoDAO();
        this.alumnoDAO = new AlumnoDAO();
        this.periodoCuotaDAO = new PeriodoCuotaDAO();
    }

    /**
     * Registra un nuevo pago para un alumno y un período de cuota.
     * Incluye lógica para determinar si aplica recargo y si es un pago parcial/total.
     * @param idAlumno ID del alumno que realiza el pago.
     * @param idPeriodo ID del período de cuota al que corresponde el pago.
     * @param montoPagado Monto que el alumno está pagando.
     * @param tipoPago Tipo de pago (ej. "Efectivo", "Transferencia", "Tarjeta").
     * @return El objeto Pago recién creado y con su ID asignado.
     * @throws Exception Si ocurre un error lógico (validación) o de base de datos.
     */
    public Pago registrarNuevoPago(int idAlumno, int idPeriodo, double montoPagado, String tipoPago) throws Exception {
        // --- 1. Validaciones de entrada ---
        if (!InputValidator.isPositive(idAlumno) || !InputValidator.isPositive(idPeriodo)) {
            throw new IllegalArgumentException("Los IDs de Alumno y Período deben ser números positivos.");
        }
        if (montoPagado <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser un valor positivo.");
        }
        if (InputValidator.isNullOrEmpty(tipoPago)) {
            throw new IllegalArgumentException("El tipo de pago es obligatorio.");
        }

        // --- 2. Validar existencia de Alumno y Período de Cuota ---
        Alumno alumnoExistente;
        PeriodoCuota periodoExistente;
        try {
            alumnoExistente = alumnoDAO.obtenerPorId(idAlumno);
            if (alumnoExistente == null) {
                throw new Exception("No se encontró un alumno con el ID: " + idAlumno);
            }
            periodoExistente = periodoCuotaDAO.obtenerPorId(idPeriodo);
            if (periodoExistente == null) {
                throw new Exception("No se encontró un período de cuota con el ID: " + idPeriodo);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar alumno o período de cuota: " + e.getMessage(), e);
            throw new Exception("Error al procesar pago: " + e.getMessage());
        }

        // --- 3. Lógica de Negocio: Calcular recargo y determinar pago parcial ---
        LocalDate fechaActual = LocalDate.now();
        double montoBasePeriodo = periodoExistente.getMontoBase();
        double montoRecargoPeriodo = periodoExistente.getMontoRecargo();
        double montoTotalAPagar = montoBasePeriodo;
        double montoRecargoAplicado = 0.0;
        boolean esPagoParcial = false;

        // Verificar si aplica recargo
        if (fechaActual.isAfter(periodoExistente.getFechaVencimiento())) {
            montoTotalAPagar += montoRecargoPeriodo;
            montoRecargoAplicado = montoRecargoPeriodo;
            System.out.println("Pago realizado después de la fecha de vencimiento. Se aplica recargo de: " + montoRecargoPeriodo);
        }

        // Obtener el monto ya pagado para este período (en caso de pagos parciales previos)
        double montoPrevioPagado = pagoDAO.obtenerMontoTotalPagadoPorAlumnoYPeriodo(idAlumno, idPeriodo);
        double montoPendienteAntesDeEstePago = montoTotalAPagar - montoPrevioPagado;

        if (montoPagado < montoPendienteAntesDeEstePago) {
            esPagoParcial = true;
            System.out.println("Pago parcial detectado. Monto pagado: " + montoPagado + ", Monto pendiente: " + (montoPendienteAntesDeEstePago - montoPagado));
        } else if (montoPagado > montoPendienteAntesDeEstePago && montoPendienteAntesDeEstePago > 0) {
             // Si paga más de lo que debe pero aún quedaba algo, se considera que cubre el pendiente y quizás sobra (a decidir cómo manejar excedentes)
             System.out.println("El alumno pagó un excedente de: " + (montoPagado - montoPendienteAntesDeEstePago));
             // Para este caso, registramos el pago como no parcial si cubre o excede lo que debe.
             // En un sistema real, un excedente podría generar un crédito a favor. Aquí, simplemente se cubre la deuda.
             esPagoParcial = false; // Ya no es parcial si cubrió la deuda completa
        } else {
            // Es un pago completo (o el primer pago y cubre todo)
            esPagoParcial = false;
        }

        // --- 4. Crear el objeto Pago ---
        Pago nuevoPago = new Pago(idAlumno, idPeriodo, fechaActual, montoPagado, tipoPago, esPagoParcial, montoRecargoAplicado);

        // --- 5. Llamar al DAO para insertar en la BD ---
        try {
            int idGenerado = pagoDAO.insertar(nuevoPago);
            if (idGenerado != -1) {
                nuevoPago.setIdPago(idGenerado);
                System.out.println("Pago registrado con éxito para el alumno ID: " + idAlumno + ", Período ID: " + idPeriodo);
                return nuevoPago;
            } else {
                throw new Exception("No se pudo insertar el pago en la base de datos.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar pago en la BD: " + e.getMessage(), e);
            throw new Exception("Error al registrar pago: " + e.getMessage());
        }
    }

    /**
     * Obtiene la información de un pago por su ID.
     * @param idPago ID del pago a buscar.
     * @return El objeto Pago si se encuentra, o null.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public Pago obtenerPagoPorId(int idPago) throws Exception {
        if (!InputValidator.isPositive(idPago)) {
            throw new IllegalArgumentException("El ID del pago debe ser un número positivo.");
        }
        try {
            return pagoDAO.obtenerPorId(idPago);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener pago por ID " + idPago + " de la BD: " + e.getMessage(), e);
            throw new Exception("Error al consultar pago por ID: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de todos los pagos registrados.
     * @return Lista de objetos Pago.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<Pago> obtenerTodosLosPagos() throws Exception {
        try {
            return pagoDAO.obtenerTodos();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los pagos de la BD: " + e.getMessage(), e);
            throw new Exception("Error al obtener listado de pagos: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de pagos realizados por un alumno específico.
     * @param idAlumno ID del alumno.
     * @return Lista de pagos del alumno.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<Pago> obtenerPagosPorAlumno(int idAlumno) throws Exception {
        if (!InputValidator.isPositive(idAlumno)) {
            throw new IllegalArgumentException("El ID del alumno debe ser un número positivo.");
        }
        try {
            // El PagoDAO no tiene un método directo para esto. Lo podríamos añadir:
            // public List<Pago> obtenerPagosPorAlumno(int idAlumno)
            // Por ahora, obtenemos todos y filtramos en memoria (no recomendado para grandes volúmenes):
            List<Pago> todosLosPagos = pagoDAO.obtenerTodos();
            List<Pago> pagosDelAlumno = new java.util.ArrayList<>();
            for (Pago p : todosLosPagos) {
                if (p.getIdAlumno() == idAlumno) {
                    pagosDelAlumno.add(p);
                }
            }
            return pagosDelAlumno;
            // MEJOR OPCIÓN: Añadir el método específico en PagoDAO para eficiencia.
            // Por ejemplo: return pagoDAO.obtenerPagosPorAlumno(idAlumno);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener pagos para el alumno ID " + idAlumno + " de la BD: " + e.getMessage(), e);
            throw new Exception("Error al consultar pagos del alumno: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de pagos para un período de cuota específico.
     * @param idPeriodo ID del período de cuota.
     * @return Lista de pagos para el período.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<Pago> obtenerPagosPorPeriodo(int idPeriodo) throws Exception {
        if (!InputValidator.isPositive(idPeriodo)) {
            throw new IllegalArgumentException("El ID del período debe ser un número positivo.");
        }
        try {
            // Similar al anterior, lo mejor es añadir el método en PagoDAO.
            List<Pago> todosLosPagos = pagoDAO.obtenerTodos();
            List<Pago> pagosDelPeriodo = new java.util.ArrayList<>();
            for (Pago p : todosLosPagos) {
                if (p.getIdPeriodo() == idPeriodo) {
                    pagosDelPeriodo.add(p);
                }
            }
            return pagosDelPeriodo;
            // MEJOR OPCIÓN: Añadir el método específico en PagoDAO para eficiencia.
            // Por ejemplo: return pagoDAO.obtenerPagosPorPeriodo(idPeriodo);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener pagos para el período ID " + idPeriodo + " de la BD: " + e.getMessage(), e);
            throw new Exception("Error al consultar pagos del período: " + e.getMessage());
        }
    }

    /**
     * Actualiza la información de un pago existente.
     * IMPORTANTE: La actualización de pagos suele ser un proceso delicado en sistemas financieros.
     * Considera si esta operación es necesaria o si se deberían registrar ajustes/reversiones.
     * @param pago El objeto Pago con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public boolean actualizarInformacionPago(Pago pago) throws Exception {
        // --- 1. Validaciones de entrada ---
        if (pago == null) {
            throw new IllegalArgumentException("El objeto Pago no puede ser nulo.");
        }
        if (!InputValidator.isPositive(pago.getIdPago())) {
            throw new IllegalArgumentException("El ID del pago es obligatorio para la actualización.");
        }
        if (!InputValidator.isPositive(pago.getIdAlumno()) || !InputValidator.isPositive(pago.getIdPeriodo())) {
            throw new IllegalArgumentException("Los IDs de Alumno y Período son obligatorios para la actualización.");
        }
        if (pago.getMontoPagado() <= 0) {
            throw new IllegalArgumentException("El monto pagado debe ser un valor positivo.");
        }
        if (InputValidator.isNullOrEmpty(pago.getTipoPago())) {
            throw new IllegalArgumentException("El tipo de pago es obligatorio.");
        }

        // Re-validar la existencia del alumno y período si es necesario aquí,
        // o si es un cambio de ID de alumno/periodo, obtener el existente.
        // Asumiendo que idAlumno e idPeriodo no cambian en una actualización típica de pago.

        try {
            return pagoDAO.actualizar(pago);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar pago con ID " + pago.getIdPago() + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al actualizar pago: " + e.getMessage());
        }
    }

    /**
     * Elimina un pago por su ID.
     * IMPORTANTE: La eliminación de pagos suele ser una operación restringida en sistemas financieros.
     * Considera si esta operación es adecuada o si se debería optar por "reversiones".
     * @param idPago ID del pago a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean eliminarPago(int idPago) throws Exception {
        if (!InputValidator.isPositive(idPago)) {
            throw new IllegalArgumentException("El ID del pago debe ser un número positivo.");
        }
        try {
            // Lógica de negocio: antes de eliminar, ¿qué pasa si el pago era el único para un período?
            // ¿El alumno vuelve a ser deudor? Esta lógica podría ir aquí.
            return pagoDAO.eliminar(idPago);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar pago con ID " + idPago + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al eliminar pago: " + e.getMessage());
        }
    }

    /**
     * Calcula y devuelve la cantidad de pagos que incluyeron un recargo.
     * @return El número de pagos con recargo.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public int obtenerCantidadPagosConRecargo() throws Exception {
        try {
            return pagoDAO.contarPagosConRecargo();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar pagos con recargo: " + e.getMessage(), e);
            throw new Exception("Error al obtener cantidad de pagos con recargo: " + e.getMessage());
        }
    }

    /**
     * Verifica si un alumno es deudor para un período específico.
     * Un alumno es deudor si no ha pagado la cuota completa para ese período.
     * @param idAlumno ID del alumno.
     * @param idPeriodo ID del período de cuota.
     * @return true si el alumno es deudor, false si ya pagó la cuota completa.
     * @throws Exception Si ocurre un error de base de datos o si el período/alumno no existen.
     */
    public boolean esAlumnoDeudor(int idAlumno, int idPeriodo) throws Exception {
        if (!InputValidator.isPositive(idAlumno) || !InputValidator.isPositive(idPeriodo)) {
            throw new IllegalArgumentException("Los IDs de Alumno y Período deben ser números positivos.");
        }

        PeriodoCuota periodoExistente;
        try {
            periodoExistente = periodoCuotaDAO.obtenerPorId(idPeriodo);
            if (periodoExistente == null) {
                throw new Exception("El período de cuota ID " + idPeriodo + " no existe.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener período de cuota para verificar deuda: " + e.getMessage(), e);
            throw new Exception("Error al verificar deuda: " + e.getMessage());
        }

        double montoTotalRequerido = periodoExistente.getMontoBase();
        // Asume que si ya venció la fecha, el monto requerido incluye el recargo para ser considerado al día
        if (LocalDate.now().isAfter(periodoExistente.getFechaVencimiento())) {
            montoTotalRequerido += periodoExistente.getMontoRecargo();
        }

        try {
            double montoPagado = pagoDAO.obtenerMontoTotalPagadoPorAlumnoYPeriodo(idAlumno, idPeriodo);
            return montoPagado < montoTotalRequerido; // Es deudor si lo pagado es menor a lo requerido
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar el estado de deuda del alumno " + idAlumno + " para el período " + idPeriodo + ": " + e.getMessage(), e);
            throw new Exception("Error al verificar deuda del alumno: " + e.getMessage());
        }
    }

    public Pago registrarPago(int idAlumno, int idPeriodo, double montoPagado, LocalDate fechaPago, String text) {
        
        throw new UnsupportedOperationException("Unimplemented method 'registrarPago'");
    }

    public boolean actualizarPago(Pago pagoActualizar) {
        
        throw new UnsupportedOperationException("Unimplemented method 'actualizarPago'");
    }

     /**
     * Obtiene los pagos agrupados por alumno para un mes específico.
     * Retorna un mapa donde la clave es el Alumno y el valor es el monto total pagado por ese alumno en el mes.
     */
    public Map<Alumno, Double> obtenerPagosAgrupadosPorAlumnoYMes(YearMonth mes) throws Exception {
        List<Pago> pagosEnMes = pagoDAO.obtenerPagosPorMes(mes); // Este método lo crearemos en PagoDAO

        Map<Alumno, Double> pagosPorAlumno = new HashMap<>();
        for (Pago pago : pagosEnMes) {
            Alumno alumno = alumnoDAO.obtenerPorId(pago.getIdAlumno());
            if (alumno != null) {
                pagosPorAlumno.merge(alumno, pago.getMontoPagado(), Double::sum);
            }
        }
        return pagosPorAlumno;
    }

    /**
     * Obtiene el monto total de ganancias para un mes específico.
     */
    public double obtenerGananciasPorMes(YearMonth mes) throws Exception {
        return pagoDAO.obtenerSumaPagosPorMes(mes); // Este método lo crearemos en PagoDAO
    }
}