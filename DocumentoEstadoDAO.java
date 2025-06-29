package taichi.dao;

import taichi.model.DocumentoEstado; // Importamos la clase DocumentoEstado
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS
import java.sql.Date;     // Necesario para java.sql.Date
import java.time.LocalDate; // Necesario para LocalDate
import java.util.ArrayList;
import java.util.List;

public class DocumentoEstadoDAO {

    /**
     * Inserta un nuevo registro de estado de documento en la base de datos.
     * @param documentoEstado El objeto DocumentoEstado a insertar.
     * @return El objeto DocumentoEstado con el ID generado asignado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public DocumentoEstado crear(DocumentoEstado documentoEstado) throws SQLException { // Cambiamos el nombre a 'crear'
        // Ajustamos la sentencia SQL para que coincida con la tabla 'documentos_estado' en nuestro esquema MySQL.
        // Columnas en BD: id_documento, id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones
        // En tu DAO original: 'estado' (String)
        // En nuestro esquema MySQL: 'presentado' (boolean) y 'fecha_presentacion' (DATE)
        String sql = "INSERT INTO documentos_estado (id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, documentoEstado.getIdAlumno());
            // NOTA: 'tipo_documento' en tu modelo DocumentoEstado y en el DAO es un String.
            // En la BD 'documentos_estado' también es 'tipo_documento' VARCHAR(100). Coincide bien.
            pstmt.setString(2, documentoEstado.getTipoDocumento());

            // AQUI LA PRINCIPAL DIFERENCIA: Mapeo de estado String a boolean 'presentado' y fecha 'fecha_presentacion'
            boolean presentado = "Entregado".equalsIgnoreCase(documentoEstado.getEstado());
            LocalDate fechaPresentacion = documentoEstado.getFechaPresentacion(); // Asumo que DocumentoEstado tiene getFechaPresentacion()

            // Si el estado es "Entregado", guardamos la fecha de presentación. De lo contrario, null.
            pstmt.setDate(3, presentado && fechaPresentacion != null ? Date.valueOf(fechaPresentacion) : null);
            pstmt.setBoolean(4, presentado); // El campo 'presentado' de la BD se basa en si el estado es "Entregado"
            pstmt.setString(5, documentoEstado.getObservaciones());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del registro de documento falló, no se insertaron filas.");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    documentoEstado.setIdDocumento(rs.getInt(1)); // Asignar el ID generado al objeto
                    System.out.println("DocumentoEstado insertado con ID: " + documentoEstado.getIdDocumento());
                } else {
                    throw new SQLException("La creación del registro de documento falló, no se obtuvo ID generado.");
                }
            }
        }
        return documentoEstado;
    }

    /**
     * Obtiene un registro de estado de documento por su ID.
     * @param id El ID del registro a buscar.
     * @return El objeto DocumentoEstado si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public DocumentoEstado obtenerPorId(int id) throws SQLException {
        // Seleccionamos las columnas según el esquema MySQL
        // id_documento (PK), id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones
        String sql = "SELECT id_documento, id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones FROM documentos_estado WHERE id_documento = ?";
        DocumentoEstado documentoEstado = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate fechaPresentacion = rs.getDate("fecha_presentacion") != null ? rs.getDate("fecha_presentacion").toLocalDate() : null;
                    boolean presentado = rs.getBoolean("presentado");
                    String estado = presentado ? "Entregado" : "Pendiente"; // Mapear 'presentado' (boolean) a 'estado' (String)
                    
                    documentoEstado = new DocumentoEstado(
                        rs.getInt("id_documento"), // En el schema DB es 'id_documento', no 'id_documento_estado'
                        rs.getInt("id_alumno"),
                        rs.getString("tipo_documento"),
                        estado, // Mapeado de boolean 'presentado'
                        fechaPresentacion, // Nueva propiedad
                        rs.getString("observaciones")
                    );
                }
            }
        }
        return documentoEstado;
    }

    /**
     * Obtiene una lista de todos los registros de estado de documentos.
     * @return Una lista de objetos DocumentoEstado. Puede estar vacía si no hay registros.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<DocumentoEstado> obtenerTodos() throws SQLException {
        List<DocumentoEstado> documentosEstado = new ArrayList<>();
        String sql = "SELECT id_documento, id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones FROM documentos_estado";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                LocalDate fechaPresentacion = rs.getDate("fecha_presentacion") != null ? rs.getDate("fecha_presentacion").toLocalDate() : null;
                boolean presentado = rs.getBoolean("presentado");
                String estado = presentado ? "Entregado" : "Pendiente";
                
                DocumentoEstado doc = new DocumentoEstado(
                    rs.getInt("id_documento"),
                    rs.getInt("id_alumno"),
                    rs.getString("tipo_documento"),
                    estado,
                    fechaPresentacion,
                    rs.getString("observaciones")
                );
                documentosEstado.add(doc);
            }
        }
        return documentosEstado;
    }

    /**
     * Actualiza la información de un registro de estado de documento existente.
     * @param documentoEstado El objeto DocumentoEstado con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizar(DocumentoEstado documentoEstado) throws SQLException {
        // Columnas en BD: id_documento, id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones
        String sql = "UPDATE documentos_estado SET id_alumno = ?, tipo_documento = ?, fecha_presentacion = ?, presentado = ?, observaciones = ? WHERE id_documento = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, documentoEstado.getIdAlumno());
            pstmt.setString(2, documentoEstado.getTipoDocumento());
            
            boolean presentado = "Entregado".equalsIgnoreCase(documentoEstado.getEstado());
            LocalDate fechaPresentacion = documentoEstado.getFechaPresentacion();

            pstmt.setDate(3, presentado && fechaPresentacion != null ? Date.valueOf(fechaPresentacion) : null);
            pstmt.setBoolean(4, presentado);
            pstmt.setString(5, documentoEstado.getObservaciones());
            pstmt.setInt(6, documentoEstado.getIdDocumento()); // 'id_documento', no 'id_documento_estado'

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }

    /**
     * Elimina un registro de estado de documento por su ID.
     * @param id El ID del registro a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM documentos_estado WHERE id_documento = ?"; // 'documentos_estado' y 'id_documento'
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }

    // --- Consultas específicas de la BD ---

    /**
     * Obtiene una lista de documentos pendientes (no presentados) para un alumno específico.
     * @param idAlumno El ID del alumno.
     * @return Una lista de objetos DocumentoEstado con 'presentado = FALSE' para el alumno dado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<DocumentoEstado> obtenerDocumentosPendientesPorAlumno(int idAlumno) throws SQLException {
        List<DocumentoEstado> documentosPendientes = new ArrayList<>();
        // En MySQL, 'presentado' es un BOOLEAN (TINYINT(1)). 'FALSE' es 0.
        String sql = "SELECT id_documento, id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones FROM documentos_estado WHERE id_alumno = ? AND presentado = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate fechaPresentacion = rs.getDate("fecha_presentacion") != null ? rs.getDate("fecha_presentacion").toLocalDate() : null;
                    boolean presentado = rs.getBoolean("presentado");
                    String estado = presentado ? "Entregado" : "Pendiente";
                    
                    DocumentoEstado doc = new DocumentoEstado(
                        rs.getInt("id_documento"),
                        rs.getInt("id_alumno"),
                        rs.getString("tipo_documento"),
                        estado,
                        fechaPresentacion,
                        rs.getString("observaciones")
                    );
                    documentosPendientes.add(doc);
                }
            }
        }
        return documentosPendientes;
    }

    /**
     * Obtiene todos los estados de documentos para un alumno específico.
     * Útil para ver el historial completo de documentos de un alumno.
     * @param idAlumno El ID del alumno.
     * @return Una lista de objetos DocumentoEstado para el alumno dado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<DocumentoEstado> obtenerDocumentosPorAlumno(int idAlumno) throws SQLException {
        List<DocumentoEstado> documentos = new ArrayList<>();
        String sql = "SELECT id_documento, id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones FROM documentos_estado WHERE id_alumno = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate fechaPresentacion = rs.getDate("fecha_presentacion") != null ? rs.getDate("fecha_presentacion").toLocalDate() : null;
                    boolean presentado = rs.getBoolean("presentado");
                    String estado = presentado ? "Entregado" : "Pendiente";
                    
                    DocumentoEstado doc = new DocumentoEstado(
                        rs.getInt("id_documento"),
                        rs.getInt("id_alumno"),
                        rs.getString("tipo_documento"),
                        estado,
                        fechaPresentacion,
                        rs.getString("observaciones")
                    );
                    documentos.add(doc);
                }
            }
        }
        return documentos;
    }
}