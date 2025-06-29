package taichi.dao;

import taichi.model.Profesor; // Importamos la clase Profesor
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS
import java.time.LocalDate; // Importación correcta
import java.util.ArrayList;
import java.util.List;

// Implementamos la interfaz IDAO, especificando que trabajamos con Profesor y su ID es Integer
public class ProfesorDAO implements IDAO<Profesor, Integer> {

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public Profesor crear(Profesor profesor) throws SQLException {
        // Columnas en BD: id_profesor, nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_contratacion, activo
        String sql = "INSERT INTO profesores (nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_contratacion, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, profesor.getNombreCompleto());
            pstmt.setString(2, profesor.getDni());
            pstmt.setDate(3, java.sql.Date.valueOf(profesor.getFechaNacimiento())); // Convertir LocalDate a java.sql.Date
            pstmt.setString(4, profesor.getDireccion());
            pstmt.setString(5, profesor.getTelefono());
            pstmt.setString(6, profesor.getEmail());
            pstmt.setDate(7, java.sql.Date.valueOf(profesor.getFechaContratacion()));
            pstmt.setBoolean(8, profesor.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del profesor falló, no se insertaron filas en la base de datos.");
            }
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    profesor.setIdProfesor(rs.getInt(1)); // Asignar el ID al objeto Profesor
                    System.out.println("Profesor insertado con ID: " + profesor.getIdProfesor());
                } else {
                    throw new SQLException("La creación del profesor falló, no se obtuvo ID generado de la base de datos.");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al crear el profesor en la base de datos: " + e.getMessage(), e);
        }
        return profesor;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public Profesor obtenerPorId(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        // Seleccionamos las columnas según el esquema MySQL
        // id_profesor, nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_contratacion, activo
        String sql = "SELECT id_profesor, nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_contratacion, activo FROM profesores WHERE id_profesor = ?";
        Profesor profesor = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    profesor = mapResultSetToProfesor(rs); // Usamos el método auxiliar
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener el profesor con ID " + id + " de la base de datos: " + e.getMessage(), e);
        }
        return profesor;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public List<Profesor> obtenerTodos() throws SQLException {
        List<Profesor> profesores = new ArrayList<>();
        // Seleccionamos las columnas según el esquema MySQL
        String sql = "SELECT id_profesor, nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_contratacion, activo FROM profesores";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                profesores.add(mapResultSetToProfesor(rs)); // Usamos el método auxiliar
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener todos los profesores de la base de datos: " + e.getMessage(), e);
        }
        return profesores;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean actualizar(Profesor profesor) throws SQLException {
        // Ajustamos la sentencia SQL para que coincida con la tabla 'profesores' en MySQL.
        String sql = "UPDATE profesores SET nombre_completo = ?, dni = ?, fecha_nacimiento = ?, direccion = ?, telefono = ?, email = ?, fecha_contratacion = ?, activo = ? WHERE id_profesor = ?";
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
            pstmt.setInt(9, profesor.getIdProfesor()); // Cláusula WHERE

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el profesor con ID " + profesor.getIdProfesor() + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean eliminar(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "DELETE FROM profesores WHERE id_profesor = ?"; // Nombre de tabla en minúsculas
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el profesor con ID " + id + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

   // Consultas Específicas de la Base de Datos

    /**
     * Obtiene una lista de todos los profesores que están activos.
     * @return Una lista de objetos Profesor activos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Profesor> obtenerProfesoresActivos() throws SQLException {
        List<Profesor> profesoresActivos = new ArrayList<>();
        String sql = "SELECT id_profesor, nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_contratacion, activo FROM profesores WHERE activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                profesoresActivos.add(mapResultSetToProfesor(rs));
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener profesores activos: " + e.getMessage(), e);
        }
        return profesoresActivos;
    }

    /**
     * Busca profesores por su nombre completo o parte de él.
     * @param nombre El nombre o parte del nombre a buscar.
     * @return Una lista de profesores que coinciden con la búsqueda.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Profesor> buscarPorNombre(String nombre) throws SQLException {
        List<Profesor> profesoresEncontrados = new ArrayList<>();
        String sql = "SELECT id_profesor, nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_contratacion, activo FROM profesores WHERE nombre_completo LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + nombre + "%"); // Búsqueda parcial con comodines
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    profesoresEncontrados.add(mapResultSetToProfesor(rs));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar profesores por nombre: " + e.getMessage(), e);
        }
        return profesoresEncontrados;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Profesor.
     * Extrae los datos de la fila actual del ResultSet y crea un objeto Profesor.
     * @param rs El ResultSet del que extraer los datos.
     * @return Un objeto Profesor con los datos de la fila actual.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private Profesor mapResultSetToProfesor(ResultSet rs) throws SQLException {
        return new Profesor(
            rs.getInt("id_profesor"),
            rs.getString("nombre_completo"),
            rs.getString("dni"),
            rs.getDate("fecha_nacimiento").toLocalDate(), // Convertir java.sql.Date a LocalDate
            rs.getString("direccion"),
            rs.getString("telefono"),
            rs.getString("email"),
            rs.getDate("fecha_contratacion").toLocalDate(), // Convertir java.sql.Date a LocalDate
            rs.getBoolean("activo")
        );
    }
}