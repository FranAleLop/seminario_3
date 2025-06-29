package taichi.dao;

import taichi.model.Clase; // Importamos la clase Clase
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS
import java.sql.Time; // Necesario para java.sql.Time
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime; // Necesario para LocalTime (si tu modelo Clase usa LocalTime para el horario)

public class ClaseDAO {

    /**
     * Inserta una nueva clase en la base de datos.
     *
     * @param clase El objeto Clase a insertar.
     * @return El objeto Clase con el ID generado asignado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Clase crear(Clase clase) throws SQLException { // Cambiamos el nombre a 'crear' para consistencia
        // Ajustamos la sentencia SQL para que coincida con la tabla 'clases' en nuestro esquema MySQL.
        // Las columnas en la BD son: id_clase, nombre_clase, horario, dia_semana, id_profesor, capacidad_maxima, activa, fecha_creacion, fecha_actualizacion
        // Notamos que 'descripcion' y 'cupo_maximo' de tu DAO original no coinciden exactamente.
        // En nuestro schema MySQL la columna es 'capacidad_maxima' y no hay 'descripcion'.
        // id_profesor es una FK que deberíamos considerar.
        String sql = "INSERT INTO clases (nombre_clase, horario, dia_semana, id_profesor, capacidad_maxima, activa) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Usamos Statement para claridad

            pstmt.setString(1, clase.getNombreClase());
            // Manejo del horario: si tu modelo Clase usa String para horario, asegúrate que sea un formato HH:MM:SS
            // Lo ideal es que el modelo use LocalTime y mapear a java.sql.Time
            if (clase.getHorario() instanceof LocalTime) { // Si el getter devuelve LocalTime
                pstmt.setTime(2, Time.valueOf((LocalTime) clase.getHorario()));
            } else if (clase.getHorario() != null) { // Si devuelve String
                // Asume que getHorario() devuelve un String en formato "HH:MM:SS" o "HH:MM"
                // MySQL puede parsear "HH:MM", pero "HH:MM:SS" es más robusto.
                pstmt.setString(2, clase.getHorario().toString()); // O directamente getString
            } else {
                pstmt.setNull(2, java.sql.Types.TIME); // Si el horario puede ser nulo
            }
            
            // Asumiendo que tu clase Clase tiene un getter para el día de la semana,
            // y un getter para el ID del profesor.
            pstmt.setString(3, clase.getDiaSemana()); // Asumiendo Clase.getDiaSemana()
            // Si el id_profesor puede ser nulo, ajusta. Nuestro schema permite NULL.
            if (clase.getIdProfesor() != null && clase.getIdProfesor() > 0) {
                pstmt.setInt(4, clase.getIdProfesor()); // Asumiendo Clase.getIdProfesor()
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(5, clase.getCapacidadMaxima()); // Columna 'capacidad_maxima' en DB, no 'cupo_maximo'
            pstmt.setBoolean(6, clase.isActiva());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación de la clase falló, no se insertaron filas.");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    clase.setIdClase(rs.getInt(1)); // Asignar el ID generado al objeto Clase
                    System.out.println("Clase insertada con ID: " + clase.getIdClase());
                } else {
                    throw new SQLException("La creación de la clase falló, no se obtuvo ID generado.");
                }
            }
        }
        return clase;
    }

    /**
     * Obtiene una clase por su ID.
     *
     * @param id El ID de la clase a buscar.
     * @return El objeto Clase si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Clase obtenerPorId(int id) throws SQLException {
        // Seleccionamos las columnas según el esquema MySQL
        // No hay columna 'descripcion' ni 'cupo_maximo' en nuestro esquema, se usa 'capacidad_maxima'.
        // Incluimos id_profesor y dia_semana.
        String sql = "SELECT id_clase, nombre_clase, horario, dia_semana, id_profesor, capacidad_maxima, activa FROM clases WHERE id_clase = ?";
        Clase clase = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Si el horario se guarda como TIME en la BD, recuperarlo como java.sql.Time
                    Time dbTime = rs.getTime("horario");
                    Object horario = (dbTime != null) ? dbTime.toLocalTime() : null; // Convierte a LocalTime
                    // Si tu modelo Clase usa String para horario, puedes usar rs.getString("horario")

                    clase = new Clase(
                        rs.getInt("id_clase"),
                        rs.getString("nombre_clase"),
                        // No hay 'descripcion' en nuestro schema DB, si tu modelo lo tiene, tendrás que manejarlo.
                        // Para este DAO, la omitimos.
                        horario, // Mapeado a LocalTime o String según tu modelo
                        rs.getString("dia_semana"), // Columna 'dia_semana' en DB
                        rs.getInt("id_profesor"), // Columna 'id_profesor' en DB
                        rs.getInt("capacidad_maxima"), // Columna 'capacidad_maxima' en DB
                        rs.getBoolean("activa")
                    );
                }
            }
        }
        return clase;
    }

    /**
     * Obtiene una lista de todas las clases.
     *
     * @return Una lista de objetos Clase. Puede estar vacía si no hay clases.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Clase> obtenerTodos() throws SQLException {
        List<Clase> clases = new ArrayList<>();
        String sql = "SELECT id_clase, nombre_clase, horario, dia_semana, id_profesor, capacidad_maxima, activa FROM clases";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Time dbTime = rs.getTime("horario");
                Object horario = (dbTime != null) ? dbTime.toLocalTime() : null; // Convierte a LocalTime

                Clase clase = new Clase(
                    rs.getInt("id_clase"),
                    rs.getString("nombre_clase"),
                    horario, // Mapeado a LocalTime o String
                    rs.getString("dia_semana"),
                    rs.getInt("id_profesor"),
                    rs.getInt("capacidad_maxima"),
                    rs.getBoolean("activa")
                );
                clases.add(clase);
            }
        }
        return clases;
    }

    /**
     * Actualiza la información de una clase existente.
     *
     * @param clase El objeto Clase con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizar(Clase clase) throws SQLException {
        // Ajustamos la sentencia SQL para que coincida con la tabla 'clases' en MySQL.
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
        }
        return filasAfectadas > 0;
    }

    /**
     * Elimina una clase por su ID.
     *
     * @param id El ID de la clase a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM clases WHERE id_clase = ?"; // Nombre de tabla en minúsculas
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
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
                    Time dbTime = rs.getTime("horario");
                    Object horario = (dbTime != null) ? dbTime.toLocalTime() : null;

                    Clase clase = new Clase(
                        rs.getInt("id_clase"),
                        rs.getString("nombre_clase"),
                        horario,
                        rs.getString("dia_semana"),
                        rs.getInt("id_profesor"),
                        rs.getInt("capacidad_maxima"),
                        rs.getBoolean("activa")
                    );
                    clasesPorDia.add(clase);
                }
            }
        }
        return clasesPorDia;
    }
}