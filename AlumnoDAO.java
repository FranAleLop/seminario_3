package taichi.dao;

import taichi.model.Alumno; // Importamos la clase Alumno
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlumnoDAO {

    /**
     * Inserta un nuevo alumno en la base de datos.
     * @param alumno El objeto Alumno a insertar.
     * @return El ID generado para el nuevo alumno, o -1 si falla.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int insertar(Alumno alumno) throws SQLException {
        String sql = "INSERT INTO Alumnos (nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_inscripcion, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;
        
        // Uso de try-with-resources para asegurar el cierre automático de Connection y PreparedStatement
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) { // Importante para obtener el ID generado

            pstmt.setString(1, alumno.getNombreCompleto());
            pstmt.setString(2, alumno.getDni());
            pstmt.setDate(3, java.sql.Date.valueOf(alumno.getFechaNacimiento())); // Convertir LocalDate a java.sql.Date
            pstmt.setString(4, alumno.getDireccion());
            pstmt.setString(5, alumno.getTelefono());
            pstmt.setString(6, alumno.getEmail());
            pstmt.setDate(7, java.sql.Date.valueOf(alumno.getFechaInscripcion()));
            pstmt.setBoolean(8, alumno.isActivo());

            int filasAfectadas = pstmt.executeUpdate(); // Ejecuta la inserción

            if (filasAfectadas > 0) {
                // Si la inserción fue exitosa, obtenemos el ID autogenerado
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        alumno.setIdAlumno(idGenerado); // Asignar el ID al objeto Alumno
                        System.out.println("Alumno insertado con ID: " + idGenerado);
                    }
                }
            }
        } // Connection y PreparedStatement se cierran automáticamente aquí
        return idGenerado;
    }

    /**
     * Obtiene un alumno por su ID.
     * @param id El ID del alumno a buscar.
     * @return El objeto Alumno si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Alumno obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_alumno, nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_inscripcion, activo FROM Alumnos WHERE id_alumno = ?";
        Alumno alumno = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id); // Establece el parámetro del ID
            
            try (ResultSet rs = pstmt.executeQuery()) { // Ejecuta la consulta
                if (rs.next()) {
                    // Si se encuentra un resultado, se crea el objeto Alumno
                    alumno = new Alumno(
                        rs.getInt("id_alumno"),
                        rs.getString("nombre_completo"),
                        rs.getString("dni"),
                        rs.getDate("fecha_nacimiento").toLocalDate(), // Convertir java.sql.Date a LocalDate
                        rs.getString("direccion"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getDate("fecha_inscripcion").toLocalDate(),
                        rs.getBoolean("activo")
                    );
                }
            }
        }
        return alumno;
    }

    /**
     * Obtiene una lista de todos los alumnos.
     * @return Una lista de objetos Alumno. Puede estar vacía si no hay alumnos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Alumno> obtenerTodos() throws SQLException {
        List<Alumno> alumnos = new ArrayList<>();
        String sql = "SELECT id_alumno, nombre_completo, dni, fecha_nacimiento, direccion, telefono, email, fecha_inscripcion, activo FROM Alumnos";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) { // Ejecuta la consulta

            while (rs.next()) { // Itera sobre cada fila del resultado
                Alumno alumno = new Alumno(
                    rs.getInt("id_alumno"),
                    rs.getString("nombre_completo"),
                    rs.getString("dni"),
                    rs.getDate("fecha_nacimiento").toLocalDate(),
                    rs.getString("direccion"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getDate("fecha_inscripcion").toLocalDate(),
                    rs.getBoolean("activo")
                );
                alumnos.add(alumno); // Agrega el alumno a la lista
            }
        }
        return alumnos;
    }

    /**
     * Actualiza la información de un alumno existente.
     * @param alumno El objeto Alumno con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizar(Alumno alumno) throws SQLException {
        String sql = "UPDATE Alumnos SET nombre_completo = ?, dni = ?, fecha_nacimiento = ?, direccion = ?, telefono = ?, email = ?, fecha_inscripcion = ?, activo = ? WHERE id_alumno = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alumno.getNombreCompleto());
            pstmt.setString(2, alumno.getDni());
            pstmt.setDate(3, java.sql.Date.valueOf(alumno.getFechaNacimiento()));
            pstmt.setString(4, alumno.getDireccion());
            pstmt.setString(5, alumno.getTelefono());
            pstmt.setString(6, alumno.getEmail());
            pstmt.setDate(7, java.sql.Date.valueOf(alumno.getFechaInscripcion()));
            pstmt.setBoolean(8, alumno.isActivo());
            pstmt.setInt(9, alumno.getIdAlumno()); // Cláusula WHERE

            filasAfectadas = pstmt.executeUpdate(); // Ejecuta la actualización
        }
        return filasAfectadas > 0;
    }

    /**
     * Elimina un alumno por su ID.
     * @param id El ID del alumno a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Alumnos WHERE id_alumno = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id); // Establece el parámetro del ID

            filasAfectadas = pstmt.executeUpdate(); // Ejecuta la eliminación
        }
        return filasAfectadas > 0;
    }

    // --- Consultas específicas de la BD ---

    /**
     * Obtiene una lista de alumnos que tienen alguna cuota pendiente (deudores).
     * Esto requiere una JOIN con la tabla Pagos y PeriodosCuota.
     * Para este ejemplo, simplificaremos a alumnos que no tienen un pago *completo* para un período en particular.
     * La consulta original de deudores era más compleja, haremos una versión que ilustre la idea.
     *
     * Este método es una implementación de la consulta de "deudores" que habíamos planteado.
     * Simplificado para mostrar el concepto de una "consulta específica".
     * Necesitarías pasar un ID de Periodo para una verificación más precisa.
     *
     * @param idPeriodo El ID del período a verificar si el alumno pagó completamente.
     * @return Una lista de alumnos que son deudores para el período dado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Alumno> obtenerDeudoresPorPeriodo(int idPeriodo) throws SQLException {
        List<Alumno> deudores = new ArrayList<>();
        // Esta consulta busca alumnos que no tienen un pago COMPLETO para un periodo dado.
        // Se considera deudor si no hay un pago, o si el pago existente es parcial para ese periodo.
        String sql = "SELECT A.id_alumno, A.nombre_completo, A.dni, A.fecha_nacimiento, " +
                     "A.direccion, A.telefono, A.email, A.fecha_inscripcion, A.activo " +
                     "FROM Alumnos A " +
                     "LEFT JOIN Pagos P ON A.id_alumno = P.id_alumno AND P.id_periodo = ? " +
                     "LEFT JOIN PeriodosCuota PC ON P.id_periodo = PC.id_periodo " +
                     "WHERE PC.id_periodo IS NULL OR P.es_pago_parcial = TRUE"; // OJO: PC.id_periodo IS NULL significa que no hay pago para ese periodo

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idPeriodo); // Establece el parámetro del ID del período
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Alumno alumno = new Alumno(
                        rs.getInt("id_alumno"),
                        rs.getString("nombre_completo"),
                        rs.getString("dni"),
                        rs.getDate("fecha_nacimiento").toLocalDate(),
                        rs.getString("direccion"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getDate("fecha_inscripcion").toLocalDate(),
                        rs.getBoolean("activo")
                    );
                    deudores.add(alumno);
                }
            }
        }
        return deudores;
    }
}