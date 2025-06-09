package taichi.controller;
import taichi.model.Clase;
import taichi.dao.ClaseDAO;
import taichi.util.InputValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClaseController {

    private ClaseDAO claseDAO;
    private static final Logger LOGGER = Logger.getLogger(ClaseController.class.getName());

    public ClaseController() {
        this.claseDAO = new ClaseDAO();
    }

    /**
     * Registra una nueva clase en el sistema.
     * Realiza validaciones antes de intentar insertar.
     * @param nombreClase Nombre de la clase (ej. "Tai Chi Chuan Principiantes").
     * @param descripcion Descripción de la clase.
     * @param horario Horario de la clase (ej. "Lunes y Miércoles 18:00-19:30").
     * @param cupoMaximo Cupo máximo de alumnos para la clase.
     * @param activa Indica si la clase está activa.
     * @return El objeto Clase recién creado y con su ID asignado, o null si falla la validación/inserción.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public Clase registrarNuevaClase(String nombreClase, String descripcion, String horario, 
                                     int cupoMaximo, boolean activa) throws Exception {
        
        // --- 1. Validaciones de entrada ---
        if (InputValidator.isNullOrEmpty(nombreClase) || InputValidator.isNullOrEmpty(horario)) {
            throw new IllegalArgumentException("El nombre de la clase y el horario son campos obligatorios.");
        }
        if (cupoMaximo <= 0) {
            throw new IllegalArgumentException("El cupo máximo debe ser un número positivo.");
        }

        // --- 2. Crear el objeto Clase ---
        Clase nuevaClase = new Clase(nombreClase, descripcion, horario, cupoMaximo, activa);

        // --- 3. Llamar al DAO para insertar en la BD ---
        try {
            int idGenerado = claseDAO.insertar(nuevaClase);
            if (idGenerado != -1) {
                nuevaClase.setIdClase(idGenerado);
                return nuevaClase;
            } else {
                throw new Exception("No se pudo insertar la clase en la base de datos.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar nueva clase en la BD: " + e.getMessage(), e);
            throw new Exception("Error al registrar clase: " + e.getMessage());
        }
    }

    /**
     * Obtiene la información de una clase por su ID.
     * @param idClase ID de la clase a buscar.
     * @return El objeto Clase si se encuentra, o null.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public Clase obtenerClasePorId(int idClase) throws Exception {
        if (!InputValidator.isPositive(idClase)) {
            throw new IllegalArgumentException("El ID de la clase debe ser un número positivo.");
        }
        try {
            return claseDAO.obtenerPorId(idClase);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener clase por ID " + idClase + " de la BD: " + e.getMessage(), e);
            throw new Exception("Error al consultar clase por ID: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de todas las clases activas.
     * @return Lista de objetos Clase.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<Clase> obtenerTodasLasClases() throws Exception {
        try {
            // Actualmente el DAO trae todas. Aquí se podría filtrar por 'activa' si fuese necesario,
            // o crear un método específico en el DAO para 'obtenerClasesActivas'.
            return claseDAO.obtenerTodos(); 
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todas las clases de la BD: " + e.getMessage(), e);
            throw new Exception("Error al obtener listado de clases: " + e.getMessage());
        }
    }

    /**
     * Actualiza la información de una clase existente.
     * @param clase El objeto Clase con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public boolean actualizarInformacionClase(Clase clase) throws Exception {
        // --- 1. Validaciones de entrada ---
        if (clase == null) {
            throw new IllegalArgumentException("El objeto Clase no puede ser nulo.");
        }
        if (!InputValidator.isPositive(clase.getIdClase())) {
            throw new IllegalArgumentException("El ID de la clase es obligatorio para la actualización.");
        }
        if (InputValidator.isNullOrEmpty(clase.getNombreClase()) || InputValidator.isNullOrEmpty(clase.getHorario())) {
            throw new IllegalArgumentException("El nombre de la clase y el horario son campos obligatorios.");
        }
        if (clase.getCupoMaximo() <= 0) {
            throw new IllegalArgumentException("El cupo máximo debe ser un número positivo.");
        }

        // --- 2. Llamar al DAO para actualizar en la BD ---
        try {
            return claseDAO.actualizar(clase);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar clase con ID " + clase.getIdClase() + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al actualizar clase: " + e.getMessage());
        }
    }

    /**
     * Activa una clase por su ID.
     * @param idClase ID de la clase a activar.
     * @return true si la clase fue activada exitosamente, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean activarClase(int idClase) throws Exception {
        if (!InputValidator.isPositive(idClase)) {
            throw new IllegalArgumentException("El ID de la clase debe ser un número positivo.");
        }
        try {
            Clase clase = claseDAO.obtenerPorId(idClase);
            if (clase == null) {
                throw new Exception("Clase no encontrada con ID: " + idClase);
            }
            if (clase.isActiva()) {
                System.out.println("La clase con ID " + idClase + " ya está activa.");
                return true; 
            }
            clase.setActiva(true); 
            return claseDAO.actualizar(clase);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al activar la clase con ID " + idClase + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al activar clase: " + e.getMessage());
        }
    }

    /**
     * Inactiva una clase por su ID.
     * @param idClase ID de la clase a inactivar.
     * @return true si la clase fue inactivada exitosamente, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean inactivarClase(int idClase) throws Exception {
        if (!InputValidator.isPositive(idClase)) {
            throw new IllegalArgumentException("El ID de la clase debe ser un número positivo.");
        }
        try {
            Clase clase = claseDAO.obtenerPorId(idClase);
            if (clase == null) {
                throw new Exception("Clase no encontrada con ID: " + idClase);
            }
            if (!clase.isActiva()) {
                System.out.println("La clase con ID " + idClase + " ya está inactiva.");
                return true; 
            }
            clase.setActiva(false); 
            return claseDAO.actualizar(clase);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al inactivar la clase con ID " + idClase + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al inactivar clase: " + e.getMessage());
        }
    }
}