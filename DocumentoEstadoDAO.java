package taichi.dao;

import taichi.model.DocumentoEstado; // Importamos la clase DocumentoEstado
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentoEstadoDAO {

    /**
     * Inserta un nuevo registro de estado de documento en la base de datos.
     * @param documentoEstado El objeto DocumentoEstado a insertar.
     * @return El ID generado para el nuevo registro, o -1 si falla.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int insertar(DocumentoEstado documentoEstado) throws SQLException {
        String sql = "INSERT INTO DocumentosEstado (id_alumno, tipo_documento, estado, observaciones) VALUES (?, ?, ?, ?)";
        int idGenerado = -1;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, documentoEstado.getIdAlumno());
            pstmt.setString(2, documentoEstado.getTipoDocumento());
            pstmt.setString(3, documentoEstado.getEstado());
            pstmt.setString(4, documentoEstado.getObservaciones());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        documentoEstado.setIdDocumentoEstado(idGenerado); // Asignar el ID al objeto DocumentoEstado
                        System.out.println("DocumentoEstado insertado con ID: " + idGenerado);
                    }
                }
            }
        }
        return idGenerado;
    }

    /**
     * Obtiene un registro de estado de documento por su ID.
     * @param id El ID del registro a buscar.
     * @return El objeto DocumentoEstado si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public DocumentoEstado obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_documento_estado, id_alumno, tipo_documento, estado, observaciones FROM DocumentosEstado WHERE id_documento_estado = ?";
        DocumentoEstado documentoEstado = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    documentoEstado = new DocumentoEstado(
                        rs.getInt("id_documento_estado"),
                        rs.getInt("id_alumno"),
                        rs.getString("tipo_documento"),
                        rs.getString("estado"),
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
        String sql = "SELECT id_documento_estado, id_alumno, tipo_documento, estado, observaciones FROM DocumentosEstado";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                DocumentoEstado documentoEstado = new DocumentoEstado(
                    rs.getInt("id_documento_estado"),
                    rs.getInt("id_alumno"),
                    rs.getString("tipo_documento"),
                    rs.getString("estado"),
                    rs.getString("observaciones")
                );
                documentosEstado.add(documentoEstado);
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
        String sql = "UPDATE DocumentosEstado SET id_alumno = ?, tipo_documento = ?, estado = ?, observaciones = ? WHERE id_documento_estado = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, documentoEstado.getIdAlumno());
            pstmt.setString(2, documentoEstado.getTipoDocumento());
            pstmt.setString(3, documentoEstado.getEstado());
            pstmt.setString(4, documentoEstado.getObservaciones());
            pstmt.setInt(5, documentoEstado.getIdDocumentoEstado());

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
        String sql = "DELETE FROM DocumentosEstado WHERE id_documento_estado = ?";
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
     * Obtiene una lista de documentos pendientes para un alumno específico.
     * @param idAlumno El ID del alumno.
     * @return Una lista de objetos DocumentoEstado con estado "Pendiente" para el alumno dado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<DocumentoEstado> obtenerDocumentosPendientesPorAlumno(int idAlumno) throws SQLException {
        List<DocumentoEstado> documentosPendientes = new ArrayList<>();
        String sql = "SELECT id_documento_estado, id_alumno, tipo_documento, estado, observaciones FROM DocumentosEstado WHERE id_alumno = ? AND estado = 'Pendiente'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    DocumentoEstado doc = new DocumentoEstado(
                        rs.getInt("id_documento_estado"),
                        rs.getInt("id_alumno"),
                        rs.getString("tipo_documento"),
                        rs.getString("estado"),
                        rs.getString("observaciones")
                    );
                    documentosPendientes.add(doc);
                }
            }
        }
        return documentosPendientes;
    }
}