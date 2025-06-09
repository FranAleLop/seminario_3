package taichi.dao;

import taichi.model.Pago;     // Importamos la clase Pago
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    /**
     * Inserta un nuevo pago en la base de datos.
     * @param pago El objeto Pago a insertar.
     * @return El ID generado para el nuevo pago, o -1 si falla.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int insertar(Pago pago) throws SQLException {
        String sql = "INSERT INTO Pagos (id_alumno, id_periodo, fecha_pago, monto_pagado, tipo_pago, es_pago_parcial, monto_recargo_aplicado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, pago.getIdAlumno());
            pstmt.setInt(2, pago.getIdPeriodo());
            pstmt.setDate(3, java.sql.Date.valueOf(pago.getFechaPago())); // Convertir LocalDate a java.sql.Date
            pstmt.setDouble(4, pago.getMontoPagado());
            pstmt.setString(5, pago.getTipoPago());
            pstmt.setBoolean(6, pago.isEsPagoParcial());
            pstmt.setDouble(7, pago.getMontoRecargoAplicado());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        pago.setIdPago(idGenerado); // Asignar el ID al objeto Pago
                        System.out.println("Pago insertado con ID: " + idGenerado);
                    }
                }
            }
        }
        return idGenerado;
    }

    /**
     * Obtiene un pago por su ID.
     * @param id El ID del pago a buscar.
     * @return El objeto Pago si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Pago obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_pago, id_alumno, id_periodo, fecha_pago, monto_pagado, tipo_pago, es_pago_parcial, monto_recargo_aplicado FROM Pagos WHERE id_pago = ?";
        Pago pago = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pago = new Pago(
                        rs.getInt("id_pago"),
                        rs.getInt("id_alumno"),
                        rs.getInt("id_periodo"),
                        rs.getDate("fecha_pago").toLocalDate(), // Convertir java.sql.Date a LocalDate
                        rs.getDouble("monto_pagado"),
                        rs.getString("tipo_pago"),
                        rs.getBoolean("es_pago_parcial"),
                        rs.getDouble("monto_recargo_aplicado")
                    );
                }
            }
        }
        return pago;
    }

    /**
     * Obtiene una lista de todos los pagos.
     * @return Una lista de objetos Pago. Puede estar vacía si no hay pagos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Pago> obtenerTodos() throws SQLException {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT id_pago, id_alumno, id_periodo, fecha_pago, monto_pagado, tipo_pago, es_pago_parcial, monto_recargo_aplicado FROM Pagos";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Pago pago = new Pago(
                    rs.getInt("id_pago"),
                    rs.getInt("id_alumno"),
                    rs.getInt("id_periodo"),
                    rs.getDate("fecha_pago").toLocalDate(),
                    rs.getDouble("monto_pagado"),
                    rs.getString("tipo_pago"),
                    rs.getBoolean("es_pago_parcial"),
                    rs.getDouble("monto_recargo_aplicado")
                );
                pagos.add(pago);
            }
        }
        return pagos;
    }

    /**
     * Actualiza la información de un pago existente.
     * @param pago El objeto Pago con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizar(Pago pago) throws SQLException {
        String sql = "UPDATE Pagos SET id_alumno = ?, id_periodo = ?, fecha_pago = ?, monto_pagado = ?, tipo_pago = ?, es_pago_parcial = ?, monto_recargo_aplicado = ? WHERE id_pago = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pago.getIdAlumno());
            pstmt.setInt(2, pago.getIdPeriodo());
            pstmt.setDate(3, java.sql.Date.valueOf(pago.getFechaPago()));
            pstmt.setDouble(4, pago.getMontoPagado());
            pstmt.setString(5, pago.getTipoPago());
            pstmt.setBoolean(6, pago.isEsPagoParcial());
            pstmt.setDouble(7, pago.getMontoRecargoAplicado());
            pstmt.setInt(8, pago.getIdPago()); // Cláusula WHERE

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }

    /**
     * Elimina un pago por su ID.
     * @param id El ID del pago a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Pagos WHERE id_pago = ?";
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
     * Cuenta el número de pagos que tuvieron un recargo aplicado.
     * @return El número total de pagos con recargo.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int contarPagosConRecargo() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Pagos WHERE monto_recargo_aplicado > 0";
        int totalPagosConRecargo = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                totalPagosConRecargo = rs.getInt(1); // O rs.getInt("COUNT(*)")
            }
        }
        return totalPagosConRecargo;
    }

    /**
     * Verifica si un alumno ha pagado una cuota completa para un periodo específico.
     * @param idAlumno El ID del alumno.
     * @param idPeriodo El ID del periodo.
     * @return true si el alumno ha realizado un pago COMPLETO para ese periodo, false en caso contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean haPagadoCuotaCompleta(int idAlumno, int idPeriodo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Pagos WHERE id_alumno = ? AND id_periodo = ? AND es_pago_parcial = FALSE";
        int pagosCompletos = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            pstmt.setInt(2, idPeriodo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pagosCompletos = rs.getInt(1);
                }
            }
        }
        return pagosCompletos > 0;
    }

    /**
     * Obtiene el monto total pagado por un alumno para un periodo específico.
     * Esto es útil para calcular si un pago parcial se convierte en completo.
     * @param idAlumno El ID del alumno.
     * @param idPeriodo El ID del periodo.
     * @return El monto total pagado por el alumno para ese periodo.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public double obtenerMontoTotalPagadoPorAlumnoYPeriodo(int idAlumno, int idPeriodo) throws SQLException {
        String sql = "SELECT SUM(monto_pagado) FROM Pagos WHERE id_alumno = ? AND id_periodo = ?";
        double montoTotal = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            pstmt.setInt(2, idPeriodo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    montoTotal = rs.getDouble(1); // SUM puede devolver null si no hay pagos, getDouble(1) lo convierte a 0.0
                }
            }
        }
        return montoTotal;
    }

        public List<Pago> obtenerPagosPorMes(YearMonth mes) throws SQLException {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE STRFTIME('%Y-%m', fecha_pago) = ?"; // SQLite
        // Para MySQL/PostgreSQL sería algo como: "SELECT * FROM pagos WHERE DATE_FORMAT(fecha_pago, '%Y-%m') = ?"
        // O: "SELECT * FROM pagos WHERE EXTRACT(YEAR FROM fecha_pago) = ? AND EXTRACT(MONTH FROM fecha_pago) = ?"
        // Ajusta la consulta según tu SGBD si no es SQLite.

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mes.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }
        }
        return pagos;
    }

    /**
     * Obtiene la suma total de pagos realizados en un mes específico.
     */
    public double obtenerSumaPagosPorMes(YearMonth mes) throws SQLException {
        double total = 0.0;
        String sql = "SELECT SUM(monto_pagado) FROM pagos WHERE STRFTIME('%Y-%m', fecha_pago) = ?"; // SQLite
        // Ajusta la consulta según tu SGBD

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mes.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        }
        return total;
    }

    /**
     * Obtiene los IDs de los alumnos que han realizado al menos un pago en un mes específico.
     */
    public static List<Integer> obtenerIdsAlumnosConPagoEnMes(YearMonth mes) throws SQLException {
        List<Integer> alumnoIds = new ArrayList<>();
        String sql = "SELECT DISTINCT id_alumno FROM pagos WHERE STRFTIME('%Y-%m', fecha_pago) = ?"; // SQLite
        // Ajusta la consulta según tu SGBD

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mes.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    alumnoIds.add(rs.getInt("id_alumno"));
                }
            }
        }
        return alumnoIds;
    }

    /**
     * Obtiene la suma de pagos de un alumno para un período de cuota específico.
     */
    public static double obtenerSumaPagosPorAlumnoYPeriodo(int idAlumno, int idPeriodo) throws SQLException {
        double totalPagado = 0.0;
        String sql = "SELECT SUM(monto_pagado) FROM pagos WHERE id_alumno = ? AND id_periodo = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idAlumno);
            pstmt.setInt(2, idPeriodo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalPagado = rs.getDouble(1);
                }
            }
        }
        return totalPagado;
    }

    // Método auxiliar (ya debería existir o ser similar)
    private Pago mapResultSetToPago(ResultSet rs) throws SQLException {
        int idPago = rs.getInt("id_pago");
        int idAlumno = rs.getInt("id_alumno");
        int idPeriodo = rs.getInt("id_periodo");
        double montoPagado = rs.getDouble("monto_pagado");
        LocalDate fechaPago = rs.getDate("fecha_pago").toLocalDate();
        String observaciones = rs.getString("observaciones");
        return new Pago(idPago, idAlumno, idPeriodo, montoPagado, fechaPago, observaciones);
    }
}