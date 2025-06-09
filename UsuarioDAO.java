package taichi.dao;

import taichi.model.Usuario; // Importamos la clase Usuario
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    /**
     * Inserta un nuevo usuario en la base de datos.
     * @param usuario El objeto Usuario a insertar.
     * @return El ID generado para el nuevo usuario, o -1 si falla.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO Usuarios (nombre_usuario, contrasena, rol, activo) VALUES (?, ?, ?, ?)";
        int idGenerado = -1;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getNombreUsuario());
            pstmt.setString(2, usuario.getContrasena()); // NOTA: En un sistema real, se debería almacenar el hash de la contraseña
            pstmt.setString(3, usuario.getRol());
            pstmt.setBoolean(4, usuario.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        usuario.setIdUsuario(idGenerado); // Asignar el ID al objeto Usuario
                        System.out.println("Usuario insertado con ID: " + idGenerado);
                    }
                }
            }
        }
        return idGenerado;
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id El ID del usuario a buscar.
     * @return El objeto Usuario si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Usuario obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, contrasena, rol, activo FROM Usuarios WHERE id_usuario = ?";
        Usuario usuario = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre_usuario"),
                        rs.getString("contrasena"),
                        rs.getString("rol"),
                        rs.getBoolean("activo")
                    );
                }
            }
        }
        return usuario;
    }

    /**
     * Obtiene un usuario por su nombre de usuario.
     * Este método es clave para la autenticación.
     * @param nombreUsuario El nombre de usuario a buscar.
     * @return El objeto Usuario si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Usuario obtenerPorNombreUsuario(String nombreUsuario) throws SQLException {
        String sql = "SELECT id_usuario, nombre_usuario, contrasena, rol, activo FROM Usuarios WHERE nombre_usuario = ?";
        Usuario usuario = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreUsuario);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre_usuario"),
                        rs.getString("contrasena"),
                        rs.getString("rol"),
                        rs.getBoolean("activo")
                    );
                }
            }
        }
        return usuario;
    }

    /**
     * Obtiene una lista de todos los usuarios.
     * @return Una lista de objetos Usuario. Puede estar vacía si no hay usuarios.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Usuario> obtenerTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre_usuario, contrasena, rol, activo FROM Usuarios";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre_usuario"),
                    rs.getString("contrasena"),
                    rs.getString("rol"),
                    rs.getBoolean("activo")
                );
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }

    /**
     * Actualiza la información de un usuario existente.
     * @param usuario El objeto Usuario con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE Usuarios SET nombre_usuario = ?, contrasena = ?, rol = ?, activo = ? WHERE id_usuario = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNombreUsuario());
            pstmt.setString(2, usuario.getContrasena()); // NOTA: Hashing para seguridad
            pstmt.setString(3, usuario.getRol());
            pstmt.setBoolean(4, usuario.isActivo());
            pstmt.setInt(5, usuario.getIdUsuario());

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }

    /**
     * Elimina un usuario por su ID.
     * @param id El ID del usuario a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Usuarios WHERE id_usuario = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }
}