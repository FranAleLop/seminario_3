package taichi.dao;

import taichi.model.Usuario; // Importamos la clase Usuario
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS
import java.util.ArrayList;
import java.util.List;

// Importar una librería de hashing de contraseñas (ej. BCrypt) si la usas en la capa de servicio
// import org.mindrot.jbcrypt.BCrypt;

// Implementamos la interfaz IDAO, especificando que trabajamos con Usuario y su ID es Integer
public class UsuarioDAO implements IDAO<Usuario, Integer> {

    /**
     * Inserta un nuevo usuario en la base de datos.
     * La contraseña se asume que ha sido hasheada ANTES de ser pasada a este método (por ejemplo, en una capa de servicio).
     * @param usuario El objeto Usuario a insertar.
     * @return El objeto Usuario con el ID generado asignado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    @Override // Indica que este método implementa un método de la interfaz IDAO
    public Usuario crear(Usuario usuario) throws SQLException {
        // Columnas en BD: id_usuario, nombre_usuario, contrasena_hash, rol, activo
        String sql = "INSERT INTO usuarios (nombre_usuario, contrasena_hash, rol, activo) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getNombreUsuario());
            // CRÍTICO: La contraseña NUNCA debe almacenarse en texto plano.
            // Se asume que usuario.getContrasena() ya devuelve el hash de la contraseña.
            // Si no es así, DEBES implementar el hashing antes de llamar a este DAO.
            // Ejemplo de cómo se *podría* hashear aquí (aunque preferiblemente en una capa de servicio):
            // String hashedPass = BCrypt.hashpw(usuario.getContrasena(), BCrypt.gensalt());
            // pstmt.setString(2, hashedPass);
            pstmt.setString(2, usuario.getContrasena()); 
            pstmt.setString(3, usuario.getRol());
            pstmt.setBoolean(4, usuario.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del usuario falló, no se insertaron filas en la base de datos.");
            }
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1)); // Asignar el ID al objeto Usuario
                    System.out.println("Usuario insertado con ID: " + usuario.getIdUsuario());
                } else {
                    throw new SQLException("La creación del usuario falló, no se obtuvo ID generado de la base de datos.");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al crear el usuario '" + usuario.getNombreUsuario() + "' en la base de datos: " + e.getMessage(), e);
        }
        return usuario;
    }

    /**
     * Obtiene un usuario por su ID.
     * @param id El ID del usuario a buscar.
     * @return El objeto Usuario si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    @Override // Indica que este método implementa un método de la interfaz IDAO
    public Usuario obtenerPorId(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
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
        } catch (SQLException e) {
            throw new SQLException("Error al obtener el usuario con ID " + id + " de la base de datos: " + e.getMessage(), e);
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
        } catch (SQLException e) {
            throw new SQLException("Error al obtener el usuario por nombre '" + nombreUsuario + "' de la base de datos: " + e.getMessage(), e);
        }
        return usuario;
    }

    /**
     * Obtiene una lista de todos los usuarios.
     * @return Una lista de objetos Usuario. Puede estar vacía si no hay usuarios.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    @Override // Indica que este método implementa un método de la interfaz IDAO
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
        } catch (SQLException e) {
            throw new SQLException("Error al obtener todos los usuarios de la base de datos: " + e.getMessage(), e);
        }
        return usuarios;
    }

    /**
     * Actualiza la información de un usuario existente.
     * NOTA IMPORTANTE: Si la contraseña se actualiza, DEBE ser hasheada antes de llamar a este método.
     * Se asume que `usuario.getContrasena()` ya devuelve el hash actual (nueva o existente).
     * @param usuario El objeto Usuario con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    @Override // Indica que este método implementa un método de la interfaz IDAO
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
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el usuario con ID " + usuario.getIdUsuario() + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

    /**
     * Elimina un usuario por su ID.
     * @param id El ID del usuario a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean eliminar(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?"; // Nombre de tabla en minúsculas
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el usuario con ID " + id + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

    //Consultas Específicas de la Base de Datos

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
        } catch (SQLException e) {
            throw new SQLException("Error al verificar la existencia del nombre de usuario '" + nombreUsuario + "': " + e.getMessage(), e);
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
        } catch (SQLException e) {
            throw new SQLException("Error al obtener usuarios activos: " + e.getMessage(), e);
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