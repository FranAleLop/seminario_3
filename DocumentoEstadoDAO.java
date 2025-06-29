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

// Implementamos la interfaz IDAO, especificando que trabajamos con DocumentoEstado y su ID es Integer
public class DocumentoEstadoDAO implements IDAO<DocumentoEstado, Integer> {

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public DocumentoEstado crear(DocumentoEstado documentoEstado) throws SQLException {
        // Columnas en BD: id_documento, id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones
        String sql = "INSERT INTO documentos_estado (id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, documentoEstado.getIdAlumno());
            pstmt.setString(2, documentoEstado.getTipoDocumento());

            // Mapeo de estado String a boolean 'presentado' y fecha 'fecha_presentacion'
            boolean presentado = "Entregado".equalsIgnoreCase(documentoEstado.getEstado());
            LocalDate fechaPresentacion = documentoEstado.getFechaPresentacion(); 

            // Si el estado es "Entregado" Y hay fecha, la guardamos. De lo contrario, null.
            // Es importante que si 'presentado' es true, 'fechaPresentacion' no sea null para asignar la fecha.
            // Si 'presentado' es false, 'fechaPresentacion' siempre será null en la BD para este campo.
            pstmt.setDate(3, presentado && fechaPresentacion != null ? Date.valueOf(fechaPresentacion) : null);
            pstmt.setBoolean(4, presentado); 
            pstmt.setString(5, documentoEstado.getObservaciones());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del registro de documento falló, no se insertaron filas en la base de datos.");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    documentoEstado.setIdDocumento(rs.getInt(1)); // Asignar el ID generado al objeto
                    System.out.println("DocumentoEstado insertado con ID: " + documentoEstado.getIdDocumento());
                } else {
                    throw new SQLException("La creación del registro de documento falló, no se obtuvo ID generado de la base de datos.");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al crear el registro de documento en la base de datos: " + e.getMessage(), e);
        }
        return documentoEstado;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public DocumentoEstado obtenerPorId(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "SELECT id_documento, id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones FROM documentos_estado WHERE id_documento = ?";
        DocumentoEstado documentoEstado = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    documentoEstado = mapResultSetToDocumentoEstado(rs); // Usamos el método auxiliar de mapeo
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener el registro de documento con ID " + id + " de la base de datos: " + e.getMessage(), e);
        }
        return documentoEstado;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public List<DocumentoEstado> obtenerTodos() throws SQLException {
        List<DocumentoEstado> documentosEstado = new ArrayList<>();
        String sql = "SELECT id_documento, id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones FROM documentos_estado";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                documentosEstado.add(mapResultSetToDocumentoEstado(rs)); // Usamos el método auxiliar de mapeo
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener todos los registros de documentos de la base de datos: " + e.getMessage(), e);
        }
        return documentosEstado;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean actualizar(DocumentoEstado documentoEstado) throws SQLException {
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
            pstmt.setInt(6, documentoEstado.getIdDocumento()); 

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el registro de documento con ID " + documentoEstado.getIdDocumento() + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean eliminar(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "DELETE FROM documentos_estado WHERE id_documento = ?"; 
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el registro de documento con ID " + id + ": " + e.getMessage(), e);
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
        String sql = "SELECT id_documento, id_alumno, tipo_documento, fecha_presentacion, presentado, observaciones FROM documentos_estado WHERE id_alumno = ? AND presentado = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    documentosPendientes.add(mapResultSetToDocumentoEstado(rs)); // Usamos el método auxiliar de mapeo
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener documentos pendientes para el alumno con ID " + idAlumno + ": " + e.getMessage(), e);
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
                    documentos.add(mapResultSetToDocumentoEstado(rs)); // Usamos el método auxiliar de mapeo
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener documentos para el alumno con ID " + idAlumno + ": " + e.getMessage(), e);
        }
        return documentos;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto DocumentoEstado.
     * Centraliza la lógica de conversión de datos de la base de datos a objetos Java.
     * @param rs El ResultSet que contiene los datos del registro de documento.
     * @return Un objeto DocumentoEstado con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al leer del ResultSet.
     */
    private DocumentoEstado mapResultSetToDocumentoEstado(ResultSet rs) throws SQLException {
        LocalDate fechaPresentacion = rs.getDate("fecha_presentacion") != null ? rs.getDate("fecha_presentacion").toLocalDate() : null;
        boolean presentado = rs.getBoolean("presentado");
        String estado = presentado ? "Entregado" : "Pendiente"; // Mapear 'presentado' (boolean) a 'estado' (String)
        
        return new DocumentoEstado(
            rs.getInt("id_documento"), 
            rs.getInt("id_alumno"),
            rs.getString("tipo_documento"),
            estado, // Mapeado de boolean 'presentado'
            fechaPresentacion, 
            rs.getString("observaciones")
        );
    }
}