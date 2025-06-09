package taichi.controller;

import taichi.model.Alumno;
import taichi.dao.AlumnoDAO;
import taichi.dao.PagoDAO; // Necesitamos el PagoDAO para verificar deudores
import taichi.model.PeriodoCuota; // También el PeriodoCuota para la lógica de deudores
import taichi.dao.PeriodoCuotaDAO; // Y su DAO para obtener períodos
import taichi.util.InputValidator; // Para futuras validaciones de entrada 

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level; // Para logging de errores
import java.util.logging.Logger; // Para logging de errores

public class AlumnoController {

    // Instancia del DAO para interactuar con la base de datos de Alumnos
    private AlumnoDAO alumnoDAO;
    private PeriodoCuotaDAO periodoCuotaDAO; // Para obtener periodos de cuota

    // Logger para registrar errores y mensajes importantes
    private static final Logger LOGGER = Logger.getLogger(AlumnoController.class.getName());

    public AlumnoController() {
        this.alumnoDAO = new AlumnoDAO();
        new PagoDAO();
        this.periodoCuotaDAO = new PeriodoCuotaDAO();
    }

    /**
     * Registra un nuevo alumno en el sistema.
     * Realiza validaciones básicas antes de intentar insertar.
     * @param nombreCompleto Nombre completo del alumno.
     * @param dni DNI del alumno.
     * @param fechaNacimiento Fecha de nacimiento del alumno.
     * @param direccion Dirección del alumno.
     * @param telefono Teléfono de contacto.
     * @param email Correo electrónico.
     * @param fechaInscripcion Fecha de inscripción.
     * @param activo Estado activo del alumno.
     * @return El objeto Alumno recién creado y con su ID asignado, o null si falla la validación/inserción.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public Alumno registrarNuevoAlumno(String nombreCompleto, String dni, LocalDate fechaNacimiento,
                                        String direccion, String telefono, String email,
                                        LocalDate fechaInscripcion, boolean activo) throws Exception {
        
        // --- 1. Validaciones de entrada ---
        if (InputValidator.isNullOrEmpty(nombreCompleto) || InputValidator.isNullOrEmpty(dni) || 
            fechaNacimiento == null || fechaInscripcion == null) {
            throw new IllegalArgumentException("Nombre, DNI, Fecha de Nacimiento y Fecha de Inscripción son campos obligatorios.");
        }
        if (!InputValidator.isValidDni(dni)) {
            throw new IllegalArgumentException("El formato del DNI no es válido.");
        }
        if (email != null && !email.isEmpty() && !InputValidator.isValidEmail(email, email, activo)) {
            throw new IllegalArgumentException("El formato del email no es válido.");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser en el futuro.");
        }
        if (fechaInscripcion.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de inscripción no puede ser en el futuro.");
        }

        // --- 2. Crear el objeto Alumno ---
        Alumno nuevoAlumno = new Alumno(nombreCompleto, dni, fechaNacimiento, direccion, telefono, email, fechaInscripcion, activo);

        // --- 3. Llamar al DAO para insertar en la BD ---
        try {
            int idGenerado = alumnoDAO.insertar(nuevoAlumno);
            if (idGenerado != -1) {
                nuevoAlumno.setIdAlumno(idGenerado); // Asegurar que el objeto tiene el ID
                return nuevoAlumno;
            } else {
                throw new Exception("No se pudo insertar el alumno en la base de datos.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar nuevo alumno en la BD: " + e.getMessage(), e);
            throw new Exception("Error al registrar alumno: " + e.getMessage()); // Re-lanzar una excepción más amigable
        }
    }

    /**
     * Obtiene la información de un alumno por su ID.
     * @param idAlumno ID del alumno a buscar.
     * @return El objeto Alumno si se encuentra, o null.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public Alumno obtenerAlumnoPorId(int idAlumno) throws Exception {
        if (idAlumno <= 0) {
            throw new IllegalArgumentException("El ID del alumno debe ser un número positivo.");
        }
        try {
            return alumnoDAO.obtenerPorId(idAlumno);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alumno por ID " + idAlumno + " de la BD: " + e.getMessage(), e);
            throw new Exception("Error al consultar alumno por ID: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de todos los alumnos activos.
     * @return Lista de objetos Alumno activos.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<Alumno> obtenerTodosLosAlumnos() throws Exception {
        try {
            // El DAO ya trae todos, aquí podríamos filtrar si quisiéramos solo activos
            return alumnoDAO.obtenerTodos(); 
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los alumnos de la BD: " + e.getMessage(), e);
            throw new Exception("Error al obtener listado de alumnos: " + e.getMessage());
        }
    }

    /**
     * Actualiza la información de un alumno existente.
     * @param alumno El objeto Alumno con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public boolean actualizarInformacionAlumno(Alumno alumno) throws Exception {
        // --- 1. Validaciones de entrada ---
        if (alumno == null) {
            throw new IllegalArgumentException("El objeto Alumno no puede ser nulo.");
        }
        if (alumno.getIdAlumno() <= 0) {
            throw new IllegalArgumentException("El ID del alumno es obligatorio para la actualización.");
        }
        if (InputValidator.isNullOrEmpty(alumno.getNombreCompleto()) || InputValidator.isNullOrEmpty(alumno.getDni()) || 
            alumno.getFechaNacimiento() == null || alumno.getFechaInscripcion() == null) {
            throw new IllegalArgumentException("Nombre, DNI, Fecha de Nacimiento y Fecha de Inscripción son campos obligatorios.");
        }
        if (!InputValidator.isValidDni(alumno.getDni())) {
            throw new IllegalArgumentException("El formato del DNI no es válido.");
        }
        if (alumno.getEmail() != null && !alumno.getEmail().isEmpty() && !InputValidator.isValidEmail(alumno.getEmail(), null, false)) {
            throw new IllegalArgumentException("El formato del email no es válido.");
        }
        if (alumno.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser en el futuro.");
        }
        if (alumno.getFechaInscripcion().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de inscripción no puede ser en el futuro.");
        }

        // --- 2. Llamar al DAO para actualizar en la BD ---
        try {
            return alumnoDAO.actualizar(alumno);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar alumno con ID " + alumno.getIdAlumno() + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al actualizar alumno: " + e.getMessage());
        }
    }

    /**
     * Da de baja (inactiva) a un alumno por su ID.
     * @param idAlumno ID del alumno a dar de baja.
     * @return true si el alumno fue dado de baja exitosamente, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean darDeBajaAlumno(int idAlumno) throws Exception {
        if (idAlumno <= 0) {
            throw new IllegalArgumentException("El ID del alumno debe ser un número positivo.");
        }
        try {
            // Primero, obtenemos el alumno para modificar solo el estado 'activo'
            Alumno alumno = alumnoDAO.obtenerPorId(idAlumno);
            if (alumno == null) {
                throw new Exception("Alumno no encontrado con ID: " + idAlumno);
            }
            if (!alumno.isActivo()) {
                System.out.println("El alumno con ID " + idAlumno + " ya está inactivo.");
                return true; // Ya está inactivo, consideramos que la operación fue "exitosa"
            }
            alumno.setActivo(false); // Cambiar el estado a inactivo
            return alumnoDAO.actualizar(alumno); // Actualizar en la base de datos
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al dar de baja al alumno con ID " + idAlumno + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al dar de baja alumno: " + e.getMessage());
        }
    }

    /**
     * Activa a un alumno por su ID.
     * @param idAlumno ID del alumno a activar.
     * @return true si el alumno fue activado exitosamente, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean activarAlumno(int idAlumno) throws Exception {
        if (idAlumno <= 0) {
            throw new IllegalArgumentException("El ID del alumno debe ser un número positivo.");
        }
        try {
            Alumno alumno = alumnoDAO.obtenerPorId(idAlumno);
            if (alumno == null) {
                throw new Exception("Alumno no encontrado con ID: " + idAlumno);
            }
            if (alumno.isActivo()) {
                System.out.println("El alumno con ID " + idAlumno + " ya está activo.");
                return true; // Ya está activo, consideramos que la operación fue "exitosa"
            }
            alumno.setActivo(true); // Cambiar el estado a activo
            return alumnoDAO.actualizar(alumno); // Actualizar en la base de datos
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al activar al alumno con ID " + idAlumno + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al activar alumno: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de alumnos que tienen alguna cuota pendiente (deudores)
     * para el período actual o un período específico.
     *
     * @param idPeriodo Si es 0, buscará deudores para el último período conocido.
     * Si es > 0, buscará deudores para ese período específico.
     * @return Lista de Alumnos deudores.
     * @throws Exception Si ocurre un error al obtener los deudores.
     */
    public List<Alumno> obtenerAlumnosDeudores(int idPeriodo) throws Exception {
        try {
            if (idPeriodo == 0) {
                // Si no se especifica un período, buscar el último período de cuota
                // Esto es una simplificación; en una app real, podrías tener un concepto de "período actual"
                List<PeriodoCuota> periodos = periodoCuotaDAO.obtenerTodos();
                if (periodos.isEmpty()) {
                    throw new Exception("No hay períodos de cuota definidos para verificar deudores.");
                }
                // Asume que el último período en la lista es el más reciente (podrías necesitar ordenar por fecha)
                idPeriodo = periodos.get(periodos.size() - 1).getIdPeriodo();
            } else if (periodoCuotaDAO.obtenerPorId(idPeriodo) == null) {
                throw new IllegalArgumentException("El período de cuota especificado no existe.");
            }

            return alumnoDAO.obtenerDeudoresPorPeriodo(idPeriodo);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener alumnos deudores de la BD: " + e.getMessage(), e);
            throw new Exception("Error al obtener lista de deudores: " + e.getMessage());
        }
    }

        public List<Alumno> obtenerAlumnosNoPagaronEnMes(YearMonth mes) throws Exception {
        List<Alumno> todosAlumnos = alumnoDAO.obtenerTodos(); // Asume que AlumnoDAO tiene obtenerTodos()
        List<Integer> idsAlumnosConPagoEnMes = null;
        try {
            idsAlumnosConPagoEnMes = PagoDAO.obtenerIdsAlumnosConPagoEnMes(mes);
        } catch (SQLException e) {
        
            e.printStackTrace();
        } // Este método lo crearemos en PagoDAO

        List<Alumno> alumnosNoPagaron = new ArrayList<>();
        for (Alumno alumno : todosAlumnos) {
            if (!idsAlumnosConPagoEnMes.contains(alumno.getIdAlumno())) {
                alumnosNoPagaron.add(alumno);
            }
        }
        return alumnosNoPagaron;
    }

    /**
     * Obtiene un mapa de alumnos y sus deudas pendientes de meses anteriores al mes dado.
     * Retorna Map<Alumno, Map<PeriodoCuota, Double>> donde la clave es el alumno,
     * y el valor es un mapa de PeriodoCuota adeudado y el monto de la cuota.
     */
    public Map<Alumno, Map<PeriodoCuota, Double>> obtenerAlumnosConDeudaAnteriorA(YearMonth mesActual) throws Exception {
        Map<Alumno, Map<PeriodoCuota, Double>> deudasPorAlumno = new HashMap<>();
        List<Alumno> todosAlumnos = alumnoDAO.obtenerTodos(); // Ojo: si hay muchos alumnos inactivos, considerar filtrar

        for (Alumno alumno : todosAlumnos) {
            // Obtenemos los períodos de cuota anteriores al mes actual
            List<PeriodoCuota> periodosAnteriores = periodoCuotaDAO.obtenerPeriodosAnterioresA(mesActual); // Crear este método en PeriodoCuotaDAO

            Map<PeriodoCuota, Double> deudasAlumno = new HashMap<>();

            for (PeriodoCuota periodo : periodosAnteriores) {
                // Verificar si el alumno ha pagado por este período
                // Asumimos que una cuota por alumno por periodo, o que el monto adeudado es el esperado de la cuota del periodo
                double montoPagado = 0;
                try {
                    montoPagado = PagoDAO.obtenerSumaPagosPorAlumnoYPeriodo(alumno.getIdAlumno(), periodo.getIdPeriodo());
                } catch (SQLException e) {
                    
                    e.printStackTrace();
                } // Crear este método en PagoDAO

                // Si no hay pagos o el monto pagado es menor a la cuota esperada del periodo
                // Se asume que PeriodoCuota tiene un getMontoCuotaEsperado() o similar.
                // Si el monto de la cuota se define en la Clase o Alumno, la lógica cambia.
                // Aquí, asumimos que el PeriodoCuota tiene el "monto esperado" de la cuota.
                // Si no existe, deberás adaptar esta lógica o usar un valor por defecto.
                double montoCuotaEsperado = periodo.getMontoBase(); // Asumiendo que PeriodoCuota tiene getMonto()

                if (montoPagado < montoCuotaEsperado) {
                    deudasAlumno.put(periodo, montoCuotaEsperado - montoPagado);
                }
            }

            if (!deudasAlumno.isEmpty()) {
                deudasPorAlumno.put(alumno, deudasAlumno);
            }
        }
        return deudasPorAlumno;
    }
}
    