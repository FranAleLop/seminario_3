package taichi.dao;

import taichi.model.Profesor; // Importamos la clase Profesor
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProfesorDAO {

    /**
     * Inserta un nuevo profesor en la base de datos.
     * @param profesor El objeto Profesor a insertar.
     * @return El ID generado para el nuevo profesor, o -1 si falla.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int insertar(Profesor profesor) throws SQLException {
        String sql = "INSERT INTO Profesores (nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_contratacion, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, profesor.getNombreCompleto());
            pstmt.setString(2, profesor.getDni());
            pstmt.setDate(3, java.sql.Date.valueOf(profesor.getFechaNacimiento()));
            pstmt.setString(4, profesor.getDireccion());
            pstmt.setString(5, profesor.getTelefono());
            pstmt.setString(6, profesor.getEmail());
            pstmt.setDate(7, java.sql.Date.valueOf(profesor.getFechaContratacion()));
            pstmt.setBoolean(8, profesor.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        profesor.setIdProfesor(idGenerado); // Asignar el ID al objeto Profesor
                        System.out.println("Profesor insertado con ID: " + idGenerado);
                    }
                }
            }
        }
        return idGenerado;
    }

    /**
     * Obtiene un profesor por su ID.
     * @param id El ID del profesor a buscar.
     * @return El objeto Profesor si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Profesor obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_profesor, nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_contratacion, activo FROM Profesores WHERE id_profesor = ?";
        Profesor profesor = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    profesor = new Profesor(
                        rs.getInt("id_profesor"),
                        rs.getString("nombre_completo"),
                        rs.getString("dni"),
                        rs.getDate("fecha_nacimiento").toLocalDate(),
                        rs.getString("direccion"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getDate("fecha_contratacion").toLocalDate(),
                        rs.getBoolean("activo")
                    );
                }
            }
        }
        return profesor;
    }

    /**
     * Obtiene una lista de todos los profesores.
     * @return Una lista de objetos Profesor. Puede estar vacía si no hay profesores.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Profesor> obtenerTodos() throws SQLException {
        List<Profesor> profesores = new ArrayList<>();
        String sql = "SELECT id_profesor, nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_contratacion, activo FROM Profesores";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Profesor profesor = new Profesor(
                    rs.getInt("id_profesor"),
                    rs.getString("nombre_completo"),
                    rs.getString("dni"),
                    rs.getDate("fecha_nacimiento").toLocalDate(),
                    rs.getString("direccion"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getDate("fecha_contratacion").toLocalDate(),
                    rs.getBoolean("activo")
                );
                profesores.add(profesor);
            }
        }
        return profesores;
    }

    /**
     * Actualiza la información de un profesor existente.
     * @param profesor El objeto Profesor con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizar(Profesor profesor) throws SQLException {
        String sql = "UPDATE Profesores SET nombre_completo = ?, dni = ?, fecha_nacimiento = ?, direccion = ?, telefono = ?, email = ?, fecha_contratacion = ?, activo = ? WHERE id_profesor = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, profesor.getNombreCompleto());
            pstmt.setString(2, profesor.getDni());
            pstmt.setDate(3, java.sql.Date.valueOf(profesor.getFechaNacimiento()));
            pstmt.setString(4, profesor.getDireccion());
            pstmt.setString(5, profesor.getTelefono());
            pstmt.setString(6, profesor.getEmail());
            pstmt.setDate(7, java.sql.Date.valueOf(profesor.getFechaContratacion()));
            pstmt.setBoolean(8, profesor.isActivo());
            pstmt.setInt(9, profesor.getIdProfesor());

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }

    /**
     * Elimina un profesor por su ID.
     * @param id El ID del profesor a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Profesores WHERE id_profesor = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }
}