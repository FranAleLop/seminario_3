package taichi.dao;

import taichi.model.Usuario; // Importamos la clase Usuario
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS
import java.util.ArrayList;
import java.util.List;

// Importar una librería de hashing de contraseñas.
// Para esto necesitarás añadir una dependencia a tu proyecto, por ejemplo, BCrypt.
// Ejemplo con BCrypt (necesitarías la librería jBCrypt en tu pom.xml o build.gradle):
// import org.mindrot.jbcrypt.BCrypt;
// O si estás usando Spring Security, podrías usar su PasswordEncoder.

public class UsuarioDAO {

    /**
     * Inserta un nuevo usuario en la base de datos.
     * La contraseña se debe hashear ANTES de ser almacenada.
     * @param usuario El objeto Usuario a insertar.
     * @return El objeto Usuario con el ID generado asignado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Usuario crear(Usuario usuario) throws SQLException { // Cambiamos el nombre a 'crear' para consistencia
        // Ajustamos la sentencia SQL para que coincida con la tabla 'usuarios' en nuestro esquema MySQL.
        // Columnas en BD: id_usuario, nombre_usuario, contrasena_hash, rol, activo
        String sql = "INSERT INTO usuarios (nombre_usuario, contrasena_hash, rol, activo) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getNombreUsuario());
            // CRÍTICO: La contraseña NUNCA debe almacenarse en texto plano.
            // Asumo que tu modelo Usuario ya tiene la contraseña hasheada aquí,
            // o que la lógica de hashing se realiza antes de llamar a este método.
            // Si no es así, DEBES implementarlo aquí o en tu capa de servicio.
            // Ejemplo (descomenta si usas BCrypt):
            // String hashedPass = BCrypt.hashpw(usuario.getContrasena(), BCrypt.gensalt());
            // pstmt.setString(2, hashedPass);
            
            // Por ahora, si tu modelo Usuario ya te da el hash (idealmente), lo usamos.
            // Si Usuario.getContrasena() devuelve el texto plano, ESTO ES UNA VULNERABILIDAD.
            pstmt.setString(2, usuario.getContrasena()); // Asumo que getContrasena() ahora devuelve el hash.
                                                        // Renombra el campo a 'contrasena_hash' en tu modelo Usuario si es un hash.
            pstmt.setString(3, usuario.getRol());
            pstmt.setBoolean(4, usuario.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del usuario falló, no se insertaron filas.");
            }
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1)); // Asignar el ID al objeto Usuario
                    System.out.println("Usuario insertado con ID: " + usuario.getIdUsuario());
                } else {
                    throw new SQLException("La creación del usuario falló, no se obtuvo ID generado.");
                }
            }
        }
        return usuario;
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id El ID del usuario a buscar.
     * @return El objeto Usuario si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Usuario obtenerPorId(int id) throws SQLException {
        // Seleccionamos las columnas según el esquema MySQL
        // id_usuario, nombre_usuario, contrasena_hash, rol, activo
        String sql = "SELECT id_usuario, nombre_usuario, contrasena_hash, rol, activo FROM usuarios WHERE id_usuario = ?";
        Usuario usuario = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = mapResultSetToUsuario(rs); // Usamos el método auxiliar
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
        // Seleccionamos las columnas según el esquema MySQL, incluyendo contrasena_hash
        String sql = "SELECT id_usuario, nombre_usuario, contrasena_hash, rol, activo FROM usuarios WHERE nombre_usuario = ?";
        Usuario usuario = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreUsuario);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    usuario = mapResultSetToUsuario(rs); // Usamos el método auxiliar
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
        // Seleccionamos las columnas según el esquema MySQL, incluyendo contrasena_hash
        String sql = "SELECT id_usuario, nombre_usuario, contrasena_hash, rol, activo FROM usuarios";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs)); // Usamos el método auxiliar
            }
        }
        return usuarios;
    }

    /**
     * Actualiza la información de un usuario existente.
     * NOTA IMPORTANTE: Si la contraseña se actualiza, DEBE ser hasheada antes de llamar a este método.
     * Si no se va a actualizar la contraseña, considera tener un método `actualizarSinContrasena` o
     * asegurar que `usuario.getContrasena()` devuelva el hash actual.
     * @param usuario El objeto Usuario con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizar(Usuario usuario) throws SQLException {
        // Ajustamos la sentencia SQL para que coincida con la tabla 'usuarios' en MySQL.
        // La columna de contraseña debe ser 'contrasena_hash'.
        String sql = "UPDATE usuarios SET nombre_usuario = ?, contrasena_hash = ?, rol = ?, activo = ? WHERE id_usuario = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNombreUsuario());
            // Asumo que usuario.getContrasena() ya devuelve el hash de la contraseña (nueva o existente).
            pstmt.setString(2, usuario.getContrasena()); 
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
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?"; // Nombre de tabla en minúsculas
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
     * Verifica si existe un usuario con el nombre de usuario dado (para evitar duplicados).
     * @param nombreUsuario El nombre de usuario a verificar.
     * @return true si el nombre de usuario ya existe, false en caso contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean existeNombreUsuario(String nombreUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE nombre_usuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombreUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Obtiene una lista de todos los usuarios que están activos.
     * @return Una lista de objetos Usuario activos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Usuario> obtenerUsuariosActivos() throws SQLException {
        List<Usuario> usuariosActivos = new ArrayList<>();
        String sql = "SELECT id_usuario, nombre_usuario, contrasena_hash, rol, activo FROM usuarios WHERE activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                usuariosActivos.add(mapResultSetToUsuario(rs));
            }
        }
        return usuariosActivos;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Usuario.
     * Extrae los datos de la fila actual del ResultSet y crea un objeto Usuario.
     * @param rs El ResultSet del que extraer los datos.
     * @return Un objeto Usuario con los datos de la fila actual.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt("id_usuario"),
            rs.getString("nombre_usuario"),
            rs.getString("contrasena_hash"), // Leer el hash de la contraseña
            rs.getString("rol"),
            rs.getBoolean("activo")
        );
    }
}