package taichi.dao;

import taichi.model.Alumno; // Importamos la clase Alumno
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS
import java.time.LocalDate; // Para manejar fechas en Java
import java.util.ArrayList;
import java.util.List;
import java.sql.Date; // Necesario para java.sql.Date.valueOf()

// Implementamos la interfaz IDAO, especificando que trabajamos con Alumno y su ID es Integer
public class AlumnoDAO implements IDAO<Alumno, Integer> {

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public Alumno crear(Alumno alumno) throws SQLException {
        String sql = "INSERT INTO alumnos (nombre, apellido, dni, telefono, email, fecha_nacimiento, activo) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, alumno.getNombre());
            pstmt.setString(2, alumno.getApellido());
            pstmt.setString(3, alumno.getDni());
            pstmt.setString(4, alumno.getTelefono());
            pstmt.setString(5, alumno.getEmail());
            pstmt.setDate(6, alumno.getFechaNacimiento() != null ? java.sql.Date.valueOf(alumno.getFechaNacimiento()) : null);
            pstmt.setBoolean(7, alumno.isActivo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del alumno falló, no se insertaron filas en la base de datos.");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    alumno.setIdAlumno(rs.getInt(1)); // Asignar el ID generado al objeto Alumno
                } else {
                    throw new SQLException("La creación del alumno falló, no se obtuvo ID generado de la base de datos.");
                }
            }
        } catch (SQLException e) {
            // Manejo específico para violaciones de unicidad (ej. DNI duplicado, si la columna DNI es UNIQUE)
            if (e.getSQLState().startsWith("23") || e.getErrorCode() == 1062) {
                throw new SQLException("Error: El DNI '" + alumno.getDni() + "' ya está registrado para otro alumno.", e);
            }
            throw new SQLException("Error al crear el alumno en la base de datos: " + e.getMessage(), e);
        }
        return alumno;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public Alumno obtenerPorId(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "SELECT id_alumno, nombre, apellido, dni, telefono, email, fecha_nacimiento, activo FROM alumnos WHERE id_alumno = ?";
        Alumno alumno = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    alumno = mapResultSetToAlumno(rs); // Usamos el método auxiliar de mapeo
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener el alumno con ID " + id + " de la base de datos: " + e.getMessage(), e);
        }
        return alumno;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public List<Alumno> obtenerTodos() throws SQLException {
        List<Alumno> alumnos = new ArrayList<>();
        String sql = "SELECT id_alumno, nombre, apellido, dni, telefono, email, fecha_nacimiento, activo FROM alumnos";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                alumnos.add(mapResultSetToAlumno(rs)); // Usamos el método auxiliar de mapeo
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener todos los alumnos de la base de datos: " + e.getMessage(), e);
        }
        return alumnos;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean actualizar(Alumno alumno) throws SQLException {
        String sql = "UPDATE alumnos SET nombre = ?, apellido = ?, dni = ?, telefono = ?, email = ?, fecha_nacimiento = ?, activo = ? WHERE id_alumno = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alumno.getNombre());
            pstmt.setString(2, alumno.getApellido());
            pstmt.setString(3, alumno.getDni());
            pstmt.setString(4, alumno.getTelefono());
            pstmt.setString(5, alumno.getEmail());
            pstmt.setDate(6, alumno.getFechaNacimiento() != null ? java.sql.Date.valueOf(alumno.getFechaNacimiento()) : null);
            pstmt.setBoolean(7, alumno.isActivo());
            pstmt.setInt(8, alumno.getIdAlumno());

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            // Manejo específico para violaciones de unicidad (ej. DNI duplicado al actualizar, si la columna DNI es UNIQUE)
            if (e.getSQLState().startsWith("23") || e.getErrorCode() == 1062) {
                throw new SQLException("Error: El DNI '" + alumno.getDni() + "' ya está registrado para otro alumno al intentar actualizar.", e);
            }
            throw new SQLException("Error al actualizar el alumno con ID " + alumno.getIdAlumno() + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean eliminar(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "DELETE FROM alumnos WHERE id_alumno = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el alumno con ID " + id + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

    /**
     * Obtiene una lista de alumnos que tienen alguna cuota pendiente (deudores)
     * para un período específico.
     *
     * @param idPeriodo El ID del período a verificar si el alumno pagó completamente.
     * @return Una lista de alumnos que son deudores para el período dado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Alumno> obtenerDeudoresPorPeriodo(int idPeriodo) throws SQLException {
        List<Alumno> deudores = new ArrayList<>();
        // Asumimos que un alumno es deudor si NO tiene un pago registrado para el idPeriodo O
        // si tiene un pago registrado pero el monto pagado es MENOR al monto esperado del periodo
        // (Esto requiere un campo 'monto_esperado' en PeriodosCuota y 'monto_pagado' en Pagos, lo cual está en nuestro esquema).
        String sql = "SELECT A.id_alumno, A.nombre, A.apellido, A.dni, A.telefono, A.email, A.fecha_nacimiento, A.activo " +
                     "FROM alumnos A " +
                     "LEFT JOIN pagos P ON A.id_alumno = P.id_alumno AND P.id_periodo = ? " +
                     "LEFT JOIN periodos_cuota PC ON PC.id_periodo = ? " + // Unimos al PeriodoCuota específico
                     "WHERE P.id_pago IS NULL " + // No hay ningún pago registrado para este periodo
                     "OR (P.monto_pagado < PC.monto_esperado)"; // O el monto pagado es menor al esperado (pago parcial/insuficiente)

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idPeriodo); // Para el LEFT JOIN en Pagos
            pstmt.setInt(2, idPeriodo); // Para el LEFT JOIN en PeriodosCuota

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    deudores.add(mapResultSetToAlumno(rs)); // Usamos el método auxiliar de mapeo
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener alumnos deudores para el período " + idPeriodo + ": " + e.getMessage(), e);
        }
        return deudores;
    }

    /**
     * Obtiene un alumno por su DNI.
     * Útil para validaciones y búsqueda.
     * @param dni El DNI del alumno a buscar.
     * @return El objeto Alumno si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Alumno obtenerPorDni(String dni) throws SQLException {
        String sql = "SELECT id_alumno, nombre, apellido, dni, telefono, email, fecha_nacimiento, activo FROM alumnos WHERE dni = ?";
        Alumno alumno = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dni);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    alumno = mapResultSetToAlumno(rs); // Usamos el método auxiliar de mapeo
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener el alumno por DNI '" + dni + "': " + e.getMessage(), e);
        }
        return alumno;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Alumno.
     * Centraliza la lógica de conversión de datos de la base de datos a objetos Java.
     * @param rs El ResultSet que contiene los datos del alumno.
     * @return Un objeto Alumno con los datos del ResultSet.
     * @throws SQLException Si ocurre un error al leer del ResultSet.
     */
    private Alumno mapResultSetToAlumno(ResultSet rs) throws SQLException {
        LocalDate fechaNacimiento = rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null;

        return new Alumno(
            rs.getInt("id_alumno"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("dni"),
            rs.getString("telefono"),
            rs.getString("email"),
            fechaNacimiento,
            rs.getBoolean("activo")
            
        );
    }
}