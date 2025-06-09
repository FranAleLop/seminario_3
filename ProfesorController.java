package taichi.controller;

import taichi.model.Profesor;
import taichi.dao.ProfesorDAO;
import taichi.util.InputValidator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfesorController {

    private ProfesorDAO profesorDAO;
    private static final Logger LOGGER = Logger.getLogger(ProfesorController.class.getName());

    public ProfesorController() {
        this.profesorDAO = new ProfesorDAO();
    }

    /**
     * Registra un nuevo profesor en el sistema.
     * Realiza validaciones antes de intentar insertar.
     * @param nombreCompleto Nombre completo del profesor.
     * @param dni DNI del profesor.
     * @param fechaNacimiento Fecha de nacimiento del profesor.
     * @param direccion Dirección del profesor.
     * @param telefono Teléfono de contacto.
     * @param email Correo electrónico.
     * @param fechaContratacion Fecha de contratación.
     * @param activo Estado activo del profesor.
     * @return El objeto Profesor recién creado y con su ID asignado, o null si falla la validación/inserción.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public Profesor registrarNuevoProfesor(String nombreCompleto, String dni, LocalDate fechaNacimiento,
                                          String direccion, String telefono, String email,
                                          LocalDate fechaContratacion, boolean activo) throws Exception {
        
        // --- 1. Validaciones de entrada ---
        if (InputValidator.isNullOrEmpty(nombreCompleto) || InputValidator.isNullOrEmpty(dni) || 
            fechaNacimiento == null || fechaContratacion == null) {
            throw new IllegalArgumentException("Nombre, DNI, Fecha de Nacimiento y Fecha de Contratación son campos obligatorios.");
        }
        if (!InputValidator.isValidDni(dni)) {
            throw new IllegalArgumentException("El formato del DNI no es válido.");
        }
        if (email != null && !email.isEmpty() && !InputValidator.isValidEmail(email, email, activo)) {
            throw new IllegalArgumentException("El formato del email no es válido.");
        }
        if (InputValidator.isNullOrFutureDate(fechaNacimiento)) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula ni en el futuro.");
        }
        if (InputValidator.isNullOrFutureDate(fechaContratacion)) {
            throw new IllegalArgumentException("La fecha de contratación no puede ser nula ni en el futuro.");
        }

        // Puedes añadir una validación para la edad mínima del profesor, si es necesario
        // if (!InputValidator.isOfMinimumAge(fechaNacimiento, 20)) { // Ejemplo: mínimo 20 años
        //    throw new IllegalArgumentException("El profesor debe tener al menos 20 años.");
        // }

        // --- 2. Crear el objeto Profesor ---
        Profesor nuevoProfesor = new Profesor(nombreCompleto, dni, fechaNacimiento, direccion, telefono, email, fechaContratacion, activo);

        // --- 3. Llamar al DAO para insertar en la BD ---
        try {
            int idGenerado = profesorDAO.insertar(nuevoProfesor);
            if (idGenerado != -1) {
                nuevoProfesor.setIdProfesor(idGenerado);
                return nuevoProfesor;
            } else {
                throw new Exception("No se pudo insertar el profesor en la base de datos.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar nuevo profesor en la BD: " + e.getMessage(), e);
            throw new Exception("Error al registrar profesor: " + e.getMessage());
        }
    }

    /**
     * Obtiene la información de un profesor por su ID.
     * @param idProfesor ID del profesor a buscar.
     * @return El objeto Profesor si se encuentra, o null.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public Profesor obtenerProfesorPorId(int idProfesor) throws Exception {
        if (!InputValidator.isPositive(idProfesor)) {
            throw new IllegalArgumentException("El ID del profesor debe ser un número positivo.");
        }
        try {
            return profesorDAO.obtenerPorId(idProfesor);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener profesor por ID " + idProfesor + " de la BD: " + e.getMessage(), e);
            throw new Exception("Error al consultar profesor por ID: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de todos los profesores activos.
     * @return Lista de objetos Profesor activos.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<Profesor> obtenerTodosLosProfesores() throws Exception {
        try {
            // El DAO ya trae todos. Si en el futuro quieres filtrar solo activos,
            // lo harías aquí o añadirías un método específico en el DAO.
            return profesorDAO.obtenerTodos(); 
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los profesores de la BD: " + e.getMessage(), e);
            throw new Exception("Error al obtener listado de profesores: " + e.getMessage());
        }
    }

    /**
     * Actualiza la información de un profesor existente.
     * @param profesor El objeto Profesor con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public boolean actualizarInformacionProfesor(Profesor profesor) throws Exception {
        // --- 1. Validaciones de entrada ---
        if (profesor == null) {
            throw new IllegalArgumentException("El objeto Profesor no puede ser nulo.");
        }
        if (!InputValidator.isPositive(profesor.getIdProfesor())) {
            throw new IllegalArgumentException("El ID del profesor es obligatorio para la actualización.");
        }
        if (InputValidator.isNullOrEmpty(profesor.getNombreCompleto()) || InputValidator.isNullOrEmpty(profesor.getDni()) || 
            profesor.getFechaNacimiento() == null || profesor.getFechaContratacion() == null) {
            throw new IllegalArgumentException("Nombre, DNI, Fecha de Nacimiento y Fecha de Contratación son campos obligatorios.");
        }
        if (!InputValidator.isValidDni(profesor.getDni())) {
            throw new IllegalArgumentException("El formato del DNI no es válido.");
        }
        if (profesor.getEmail() != null && !profesor.getEmail().isEmpty() && !InputValidator.isValidEmail(profesor.getEmail(), null, false)) {
            throw new IllegalArgumentException("El formato del email no es válido.");
        }
        if (InputValidator.isNullOrFutureDate(profesor.getFechaNacimiento())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula ni en el futuro.");
        }
        if (InputValidator.isNullOrFutureDate(profesor.getFechaContratacion())) {
            throw new IllegalArgumentException("La fecha de contratación no puede ser nula ni en el futuro.");
        }

        // --- 2. Llamar al DAO para actualizar en la BD ---
        try {
            return profesorDAO.actualizar(profesor);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar profesor con ID " + profesor.getIdProfesor() + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al actualizar profesor: " + e.getMessage());
        }
    }

    /**
     * Da de baja (inactiva) a un profesor por su ID.
     * @param idProfesor ID del profesor a dar de baja.
     * @return true si el profesor fue dado de baja exitosamente, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean darDeBajaProfesor(int idProfesor) throws Exception {
        if (!InputValidator.isPositive(idProfesor)) {
            throw new IllegalArgumentException("El ID del profesor debe ser un número positivo.");
        }
        try {
            Profesor profesor = profesorDAO.obtenerPorId(idProfesor);
            if (profesor == null) {
                throw new Exception("Profesor no encontrado con ID: " + idProfesor);
            }
            if (!profesor.isActivo()) {
                System.out.println("El profesor con ID " + idProfesor + " ya está inactivo.");
                return true; 
            }
            profesor.setActivo(false); 
            return profesorDAO.actualizar(profesor);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al dar de baja al profesor con ID " + idProfesor + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al dar de baja profesor: " + e.getMessage());
        }
    }

    /**
     * Activa a un profesor por su ID.
     * @param idProfesor ID del profesor a activar.
     * @return true si el profesor fue activado exitosamente, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean activarProfesor(int idProfesor) throws Exception {
        if (!InputValidator.isPositive(idProfesor)) {
            throw new IllegalArgumentException("El ID del profesor debe ser un número positivo.");
        }
        try {
            Profesor profesor = profesorDAO.obtenerPorId(idProfesor);
            if (profesor == null) {
                throw new Exception("Profesor no encontrado con ID: " + idProfesor);
            }
            if (profesor.isActivo()) {
                System.out.println("El profesor con ID " + idProfesor + " ya está activo.");
                return true; 
            }
            profesor.setActivo(true); 
            return profesorDAO.actualizar(profesor);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al activar al profesor con ID " + idProfesor + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al activar profesor: " + e.getMessage());
        }
    }
}