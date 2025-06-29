package taichi.dao;

import taichi.model.Alumno; // Importamos la clase Alumno
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate; 

public class AlumnoDAO {

    /**
     * Inserta un nuevo alumno en la base de datos.
     *
     * @param alumno El objeto Alumno a insertar.
     * @return El objeto Alumno con el ID generado asignado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Alumno crear(Alumno alumno) throws SQLException { // Cambiamos el nombre a 'crear' para consistencia con otros DAOs
        // MySQL maneja 'fecha_creacion' y 'fecha_actualizacion' automáticamente,
        // así que no las incluimos en el INSERT.
        String sql = "INSERT INTO alumnos (nombre, apellido, dni, telefono, email, fecha_nacimiento, activo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Nota: Si el modelo Alumno tiene 'direccion' y 'fecha_inscripcion' y las tablas no, habría que decidir si se añaden a la BD o se eliminan del modelo.
        // Asumiendo el script MySQL proporcionado: 'nombre_completo' en el script de la entrega 2 se convirtió a 'nombre' y 'apellido'.
        // Aquí asumimos que el objeto Alumno tiene 'getNombre()' y 'getApellido()'
        // Si Alumno aún tiene 'getNombreCompleto()', deberías dividirlo en nombre y apellido antes de insertar,
        // o cambiar el schema de la BD a 'nombre_completo'
        // EN NUESTRO SCHEMA MYSQL (última versión), TENEMOS 'nombre' y 'apellido'.
        // POR ESO, EL ALUMNO MODEL DEBE TENER GETNOMBRE() Y GETAPELLIDO()
        // O MODIFICAR EL SCHEMA DE LA BD DE ALUMNOS PARA QUE VUELVA A TENER 'nombre_completo'
        // AJUSTO LA SQL PARA EL SCHEMA MAS RECIENTE CON 'nombre' y 'apellido'
        // Y ASUMO QUE EL OBJETO ALUMNO tiene esos getters.

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, alumno.getNombre()); // Asumiendo Alumno.getNombre()
            pstmt.setString(2, alumno.getApellido()); // Asumiendo Alumno.getApellido()
            pstmt.setString(3, alumno.getDni());
            pstmt.setString(4, alumno.getTelefono()); // Antes era telefono_contacto
            pstmt.setString(5, alumno.getEmail());
            pstmt.setDate(6, alumno.getFechaNacimiento() != null ? java.sql.Date.valueOf(alumno.getFechaNacimiento()) : null);
            pstmt.setBoolean(7, alumno.isActivo()); // Asumiendo que el campo 'activo' existe en Alumno y BD

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del alumno falló, no se insertaron filas.");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    alumno.setIdAlumno(rs.getInt(1)); // Asignar el ID generado al objeto Alumno
                } else {
                    throw new SQLException("La creación del alumno falló, no se obtuvo ID generado.");
                }
            }
        }
        return alumno;
    }

    /**
     * Obtiene un alumno por su ID.
     *
     * @param id El ID del alumno a buscar.
     * @return El objeto Alumno si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Alumno obtenerPorId(int id) throws SQLException {
        // Seleccionamos las columnas según el script MySQL.
        // No seleccionamos fecha_creacion y fecha_actualizacion si no las necesitas en el objeto Alumno.
        // Si las necesitas, Alumno deberá tener campos para ellas.
        String sql = "SELECT id_alumno, nombre, apellido, dni, telefono, email, fecha_nacimiento, activo FROM alumnos WHERE id_alumno = ?";
        Alumno alumno = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Mapeo según las columnas de la tabla 'alumnos' en MySQL
                    // Y asumiendo que el constructor de Alumno fue actualizado.
                    LocalDate fechaNacimiento = rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null;

                    alumno = new Alumno(
                        rs.getInt("id_alumno"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        fechaNacimiento,
                        rs.getBoolean("activo")
                        // NOTA: 'direccion' y 'fecha_inscripcion' no están en el schema de la BD 'alumnos' que te di.
                        // Si tu modelo Alumno las tiene, debes decidir si las eliminas del modelo o las añades al esquema de la BD.
                        // Por simplicidad en esta corrección, las he omitido en el mapeo, asumiendo que no existen en el schema de la BD.
                        // También, ten en cuenta que el constructor de Alumno podría necesitar ajuste a menos parámetros.
                    );
                }
            }
        }
        return alumno;
    }

    /**
     * Obtiene una lista de todos los alumnos.
     *
     * @return Una lista de objetos Alumno. Puede estar vacía si no hay alumnos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Alumno> obtenerTodos() throws SQLException {
        List<Alumno> alumnos = new ArrayList<>();
        String sql = "SELECT id_alumno, nombre, apellido, dni, telefono, email, fecha_nacimiento, activo FROM alumnos";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                LocalDate fechaNacimiento = rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null;

                Alumno alumno = new Alumno(
                    rs.getInt("id_alumno"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("dni"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    fechaNacimiento,
                    rs.getBoolean("activo")
                );
                alumnos.add(alumno);
            }
        }
        return alumnos;
    }

    /**
     * Actualiza la información de un alumno existente.
     *
     * @param alumno El objeto Alumno con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizar(Alumno alumno) throws SQLException {
        // NOTA: 'fecha_actualizacion' en la BD se actualiza automáticamente.
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
        }
        return filasAfectadas > 0;
    }

    /**
     * Elimina un alumno por su ID.
     *
     * @param id El ID del alumno a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM alumnos WHERE id_alumno = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
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
                    LocalDate fechaNacimiento = rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null;

                    Alumno alumno = new Alumno(
                        rs.getInt("id_alumno"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("dni"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        fechaNacimiento,
                        rs.getBoolean("activo")
                    );
                    deudores.add(alumno);
                }
            }
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
                    LocalDate fechaNacimiento = rs.getDate("fecha_nacimiento") != null ? rs.getDate("fecha_nacimiento").toLocalDate() : null;

                    alumno = new Alumno(
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
        }
        return alumno;
    }
}