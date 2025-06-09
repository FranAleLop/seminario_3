package taichi.controller;

import taichi.model.DocumentoEstado;
import taichi.model.Alumno; // Para validar la existencia del alumno
import taichi.dao.DocumentoEstadoDAO;
import taichi.dao.AlumnoDAO; // Necesario para verificar existencia del alumno
import taichi.util.InputValidator;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentoEstadoController {

    private DocumentoEstadoDAO documentoEstadoDAO;
    private AlumnoDAO alumnoDAO; // Para validar la existencia del alumno
    private static final Logger LOGGER = Logger.getLogger(DocumentoEstadoController.class.getName());

    public DocumentoEstadoController() {
        this.documentoEstadoDAO = new DocumentoEstadoDAO();
        this.alumnoDAO = new AlumnoDAO();
    }

    /**
     * Registra un nuevo estado de documento para un alumno.
     * @param idAlumno ID del alumno al que pertenece el documento.
     * @param tipoDocumento Tipo de documento (ej. "DNI", "Certificado Médico", "Ficha de Inscripción").
     * @param estado Estado del documento (ej. "Pendiente", "Entregado", "Vencido").
     * @param observaciones Notas adicionales sobre el documento.
     * @return El objeto DocumentoEstado recién creado y con su ID asignado.
     * @throws Exception Si ocurre un error lógico (validación) o de base de datos.
     */
    public DocumentoEstado registrarDocumentoEstado(int idAlumno, String tipoDocumento, 
                                                 String estado, String observaciones) throws Exception {
        // --- 1. Validaciones de entrada ---
        if (!InputValidator.isPositive(idAlumno)) {
            throw new IllegalArgumentException("El ID del alumno debe ser un número positivo.");
        }
        if (InputValidator.isNullOrEmpty(tipoDocumento) || InputValidator.isNullOrEmpty(estado)) {
            throw new IllegalArgumentException("El tipo de documento y el estado son campos obligatorios.");
        }
        // Puedes añadir una validación para los estados permitidos, ej:
        // if (!List.of("Pendiente", "Entregado", "Vencido").contains(estado)) {
        //     throw new IllegalArgumentException("El estado del documento no es válido.");
        // }

        // --- 2. Validar existencia del Alumno ---
        try {
            Alumno alumnoExistente = alumnoDAO.obtenerPorId(idAlumno);
            if (alumnoExistente == null) {
                throw new Exception("No se encontró un alumno con el ID: " + idAlumno);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar la existencia del alumno: " + e.getMessage(), e);
            throw new Exception("Error al registrar documento: " + e.getMessage());
        }

        // --- 3. Crear el objeto DocumentoEstado ---
        DocumentoEstado nuevoDocumentoEstado = new DocumentoEstado(idAlumno, tipoDocumento, estado, observaciones);

        // --- 4. Llamar al DAO para insertar en la BD ---
        try {
            int idGenerado = documentoEstadoDAO.insertar(nuevoDocumentoEstado);
            if (idGenerado != -1) {
                nuevoDocumentoEstado.setIdDocumentoEstado(idGenerado);
                System.out.println("DocumentoEstado registrado con éxito para el alumno ID: " + idAlumno + ", Tipo: " + tipoDocumento);
                return nuevoDocumentoEstado;
            } else {
                throw new Exception("No se pudo insertar el estado del documento en la base de datos.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar DocumentoEstado en la BD: " + e.getMessage(), e);
            throw new Exception("Error al registrar estado de documento: " + e.getMessage());
        }
    }

    /**
     * Obtiene la información de un registro de estado de documento por su ID.
     * @param idDocumentoEstado ID del registro a buscar.
     * @return El objeto DocumentoEstado si se encuentra, o null.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public DocumentoEstado obtenerDocumentoEstadoPorId(int idDocumentoEstado) throws Exception {
        if (!InputValidator.isPositive(idDocumentoEstado)) {
            throw new IllegalArgumentException("El ID del estado de documento debe ser un número positivo.");
        }
        try {
            return documentoEstadoDAO.obtenerPorId(idDocumentoEstado);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener DocumentoEstado por ID " + idDocumentoEstado + " de la BD: " + e.getMessage(), e);
            throw new Exception("Error al consultar estado de documento por ID: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de todos los registros de estado de documentos.
     * @return Lista de objetos DocumentoEstado.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<DocumentoEstado> obtenerTodosLosDocumentosEstado() throws Exception {
        try {
            return documentoEstadoDAO.obtenerTodos();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los DocumentosEstado de la BD: " + e.getMessage(), e);
            throw new Exception("Error al obtener listado de estados de documentos: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de registros de estado de documentos para un alumno específico.
     * @param idAlumno ID del alumno.
     * @return Lista de DocumentoEstado del alumno.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<DocumentoEstado> obtenerDocumentosEstadoPorAlumno(int idAlumno) throws Exception {
        if (!InputValidator.isPositive(idAlumno)) {
            throw new IllegalArgumentException("El ID del alumno debe ser un número positivo.");
        }
        try {
            // Este método ya lo tenemos en el DAO, lo usamos directamente
            return documentoEstadoDAO.obtenerDocumentosPendientesPorAlumno(idAlumno);
            // NOTA: El método en el DAO actualmente solo trae "Pendientes".
            // Si necesitas TODOS los documentos de un alumno, deberías añadir un nuevo método en DocumentoEstadoDAO:
            // public List<DocumentoEstado> obtenerTodosDocumentosPorAlumno(int idAlumno) { ... }
            // Y llamarlo aquí.
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener documentos para el alumno ID " + idAlumno + " de la BD: " + e.getMessage(), e);
            throw new Exception("Error al consultar documentos del alumno: " + e.getMessage());
        }
    }

    /**
     * Actualiza la información de un registro de estado de documento existente.
     * @param documentoEstado El objeto DocumentoEstado con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public boolean actualizarDocumentoEstado(DocumentoEstado documentoEstado) throws Exception {
        // --- 1. Validaciones de entrada ---
        if (documentoEstado == null) {
            throw new IllegalArgumentException("El objeto DocumentoEstado no puede ser nulo.");
        }
        if (!InputValidator.isPositive(documentoEstado.getIdDocumentoEstado())) {
            throw new IllegalArgumentException("El ID del estado de documento es obligatorio para la actualización.");
        }
        if (!InputValidator.isPositive(documentoEstado.getIdAlumno())) {
            throw new IllegalArgumentException("El ID del alumno es obligatorio para la actualización.");
        }
        if (InputValidator.isNullOrEmpty(documentoEstado.getTipoDocumento()) || InputValidator.isNullOrEmpty(documentoEstado.getEstado())) {
            throw new IllegalArgumentException("El tipo de documento y el estado son campos obligatorios.");
        }
        // Puedes añadir la misma validación de estados permitidos que en 'registrarDocumentoEstado'.

        // --- 2. Validar existencia del Alumno si el ID cambió (o por seguridad) ---
        // Asumimos que idAlumno no cambia en una actualización típica de DocumentoEstado.
        // Si pudiera cambiar, se necesitaría una verificación similar a la de 'registrarDocumentoEstado'.

        try {
            return documentoEstadoDAO.actualizar(documentoEstado);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar DocumentoEstado con ID " + documentoEstado.getIdDocumentoEstado() + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al actualizar estado de documento: " + e.getMessage());
        }
    }

    /**
     * Elimina un registro de estado de documento por su ID.
     * @param idDocumentoEstado ID del registro a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean eliminarDocumentoEstado(int idDocumentoEstado) throws Exception {
        if (!InputValidator.isPositive(idDocumentoEstado)) {
            throw new IllegalArgumentException("El ID del estado de documento debe ser un número positivo.");
        }
        try {
            return documentoEstadoDAO.eliminar(idDocumentoEstado);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar DocumentoEstado con ID " + idDocumentoEstado + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al eliminar estado de documento: " + e.getMessage());
        }
    }

    public DocumentoEstado registrarDocumentoEstado(int idAlumno, String text, LocalDate fechaPresentacion,
            boolean selected, String text2) {
        
        throw new UnsupportedOperationException("Unimplemented method 'registrarDocumentoEstado'");
    }

    
}