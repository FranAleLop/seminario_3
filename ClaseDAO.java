package taichi.dao;

import taichi.model.Clase; // Importamos la clase Clase
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS
import java.sql.Time; // Necesario para java.sql.Time
import java.time.LocalTime; // Necesario para LocalTime (si tu modelo Clase usa LocalTime para el horario)
import java.util.ArrayList;
import java.util.List;

// Implementamos la interfaz IDAO, especificando que trabajamos con Clase y su ID es Integer
public class ClaseDAO implements IDAO<Clase, Integer> {

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public Clase crear(Clase clase) throws SQLException {
        String sql = "INSERT INTO clases (nombre_clase, horario, dia_semana, id_profesor, capacidad_maxima, activa) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, clase.getNombreClase());
            
            // Manejo del horario: si tu modelo Clase usa LocalTime, mapeamos a java.sql.Time
            if (clase.getHorario() instanceof LocalTime) {
                pstmt.setTime(2, Time.valueOf((LocalTime) clase.getHorario()));
            } else if (clase.getHorario() != null) {
                // Si getHorario() devuelve un String (ej. "HH:MM:SS" o "HH:MM")
                pstmt.setString(2, clase.getHorario().toString());
            } else {
                pstmt.setNull(2, java.sql.Types.TIME); // Si el horario puede ser nulo
            }
            
            pstmt.setString(3, clase.getDiaSemana()); 
            
            // Si el id_profesor puede ser nulo, ajusta. Nuestro schema permite NULL.
            if (clase.getIdProfesor() != null && clase.getIdProfesor() > 0) {
                pstmt.setInt(4, clase.getIdProfesor());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(5, clase.getCapacidadMaxima());
            pstmt.setBoolean(6, clase.isActiva());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación de la clase falló, no se insertaron filas en la base de datos.");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    clase.setIdClase(rs.getInt(1)); // Asignar el ID generado al objeto Clase
                    System.out.println("Clase insertada con ID: " + clase.getIdClase());
                } else {
                    throw new SQLException("La creación de la clase falló, no se obtuvo ID generado de la base de datos.");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al crear la clase en la base de datos: " + e.getMessage(), e);
        }
        return clase;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public Clase obtenerPorId(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "SELECT id_clase, nombre_clase, horario, dia_semana, id_profesor, capacidad_maxima, activa FROM clases WHERE id_clase = ?";
        Clase clase = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    clase = mapResultSetToClase(rs); // Usamos el método auxiliar de mapeo
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener la clase con ID " + id + " de la base de datos: " + e.getMessage(), e);
        }
        return clase;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public List<Clase> obtenerTodos() throws SQLException {
        List<Clase> clases = new ArrayList<>();
        String sql = "SELECT id_clase, nombre_clase, horario, dia_semana, id_profesor, capacidad_maxima, activa FROM clases";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                clases.add(mapResultSetToClase(rs)); // Usamos el método auxiliar de mapeo
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener todas las clases de la base de datos: " + e.getMessage(), e);
        }
        return clases;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean actualizar(Clase clase) throws SQLException {
        String sql = "UPDATE clases SET nombre_clase = ?, horario = ?, dia_semana = ?, id_profesor = ?, capacidad_maxima = ?, activa = ? WHERE id_clase = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, clase.getNombreClase());
            
            if (clase.getHorario() instanceof LocalTime) {
                pstmt.setTime(2, Time.valueOf((LocalTime) clase.getHorario()));
            } else if (clase.getHorario() != null) {
                pstmt.setString(2, clase.getHorario().toString());
            } else {
                pstmt.setNull(2, java.sql.Types.TIME);
            }

            pstmt.setString(3, clase.getDiaSemana());
            if (clase.getIdProfesor() != null && clase.getIdProfesor() > 0) {
                pstmt.setInt(4, clase.getIdProfesor());
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.setInt(5, clase.getCapacidadMaxima());
            pstmt.setBoolean(6, clase.isActiva());
            pstmt.setInt(7, clase.getIdClase());

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar la clase con ID " + clase.getIdClase() + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean eliminar(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "DELETE FROM clases WHERE id_clase = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar la clase con ID " + id + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }
    
    // --- Métodos Adicionales Útiles ---

    /**
     * Obtiene una lista de clases por día de la semana.
     * @param diaSemana El día de la semana (ej. "Lunes").
     * @return Lista de clases que se imparten ese día.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Clase> obtenerClasesPorDia(String diaSemana) throws SQLException {
        List<Clase> clasesPorDia = new ArrayList<>();
        String sql = "SELECT id_clase, nombre_clase, horario, dia_semana, id_profesor, capacidad_maxima, activa FROM clases WHERE dia_semana = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, diaSemana);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    clasesPorDia.add(mapResultSetToClase(rs)); // Usamos el método auxiliar de mapeo
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener clases por día '" + diaSemana + "': " + e.getMessage(), e);
        }
        return clasesPorDia;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Clase.
     * Centraliza la lógica de conversión de datos de la base de datos a objetos Java.
     * @param rs El ResultSet que contiene los datos de la clase.
     * @return Un objeto Clase con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al leer del ResultSet.
     */
    private Clase mapResultSetToClase(ResultSet rs) throws SQLException {
        Time dbTime = rs.getTime("horario");
        Object horario = (dbTime != null) ? dbTime.toLocalTime() : null; // Convierte a LocalTime

        return new Clase(
            rs.getInt("id_clase"),
            rs.getString("nombre_clase"),
            horario, // Mapeado a LocalTime o String según tu modelo Clase
            rs.getString("dia_semana"),
            rs.getInt("id_profesor"),
            rs.getInt("capacidad_maxima"),
            rs.getBoolean("activa")
        );
    }
}