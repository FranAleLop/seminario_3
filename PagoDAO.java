package taichi.dao;

import taichi.model.Pago;     // Importamos la clase Pago
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Importar para Statement.RETURN_GENERATED_KEYS
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter; // No se usa directamente en este DAO, pero es útil.
import java.util.ArrayList;
import java.util.List;

// Implementamos la interfaz IDAO, especificando que trabajamos con Pago y su ID es Integer
public class PagoDAO implements IDAO<Pago, Integer> {

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public Pago crear(Pago pago) throws SQLException {
        // Columnas en BD: id_pago, id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo
        String sql = "INSERT INTO pagos (id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, pago.getIdAlumno());
            pstmt.setInt(2, pago.getIdCuota()); 
            pstmt.setDate(3, java.sql.Date.valueOf(pago.getFechaPago())); // LocalDate a java.sql.Date
            pstmt.setDouble(4, pago.getMontoPagado());
            pstmt.setString(5, pago.getTipoPago());
            
            // Mapeo directo: si el monto de recargo es > 0, tiene_recargo es TRUE
            boolean tieneRecargo = pago.getMontoRecargoAplicado() > 0;
            pstmt.setBoolean(6, tieneRecargo); 
            pstmt.setDouble(7, pago.getMontoRecargoAplicado());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del pago falló, no se insertaron filas en la base de datos.");
            }
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pago.setIdPago(rs.getInt(1)); // Asignar el ID al objeto Pago
                    System.out.println("Pago insertado con ID: " + pago.getIdPago());
                } else {
                    throw new SQLException("La creación del pago falló, no se obtuvo ID generado de la base de datos.");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al crear el pago en la base de datos: " + e.getMessage(), e);
        }
        return pago;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public Pago obtenerPorId(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "SELECT id_pago, id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo FROM pagos WHERE id_pago = ?";
        Pago pago = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pago = mapResultSetToPago(rs); // Usamos el método auxiliar
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener el pago con ID " + id + " de la base de datos: " + e.getMessage(), e);
        }
        return pago;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public List<Pago> obtenerTodos() throws SQLException {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT id_pago, id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo FROM pagos";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                pagos.add(mapResultSetToPago(rs)); // Usamos el método auxiliar
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener todos los pagos de la base de datos: " + e.getMessage(), e);
        }
        return pagos;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean actualizar(Pago pago) throws SQLException {
        String sql = "UPDATE pagos SET id_alumno = ?, id_cuota = ?, fecha_pago = ?, monto_pagado = ?, tipo_pago = ?, tiene_recargo = ?, monto_recargo = ? WHERE id_pago = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pago.getIdAlumno());
            pstmt.setInt(2, pago.getIdCuota()); 
            pstmt.setDate(3, java.sql.Date.valueOf(pago.getFechaPago()));
            pstmt.setDouble(4, pago.getMontoPagado());
            pstmt.setString(5, pago.getTipoPago());
            
            boolean tieneRecargo = pago.getMontoRecargoAplicado() > 0;
            pstmt.setBoolean(6, tieneRecargo); 
            pstmt.setDouble(7, pago.getMontoRecargoAplicado());
            pstmt.setInt(8, pago.getIdPago()); 

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el pago con ID " + pago.getIdPago() + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean eliminar(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "DELETE FROM pagos WHERE id_pago = ?"; 
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el pago con ID " + id + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }



    // Consultas Específicas de la Base de Datos

    /**
     * Cuenta el número de pagos que tuvieron un recargo aplicado.
     * @return El número total de pagos con recargo.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int contarPagosConRecargo() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pagos WHERE monto_recargo > 0"; 
        int totalPagosConRecargo = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                totalPagosConRecargo = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al contar pagos con recargo: " + e.getMessage(), e);
        }
        return totalPagosConRecargo;
    }

    /**
     * Verifica si un alumno ha realizado un pago para una cuota específica sin recargo.
     * IMPORTANTE: Para determinar si una cuota está "completa", se necesita comparar el `monto_pagado`
     * con el `monto_total` de la cuota, lo cual requeriría acceder a la tabla `cuotas_alumnos`.
     * Este método solo verifica si existe un pago para esa cuota que **no** tuvo recargo.
     *
     * @param idAlumno El ID del alumno.
     * @param idCuota El ID de la cuota.
     * @return true si existe al menos un pago para esa cuota sin recargo, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean haPagadoCuotaSinRecargo(int idAlumno, int idCuota) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pagos WHERE id_alumno = ? AND id_cuota = ? AND tiene_recargo = FALSE";
        int pagosSinRecargo = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            pstmt.setInt(2, idCuota); 

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pagosSinRecargo = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al verificar si la cuota " + idCuota + " para el alumno " + idAlumno + " fue pagada sin recargo: " + e.getMessage(), e);
        }
        return pagosSinRecargo > 0;
    }

    /**
     * Obtiene el monto total pagado por un alumno para una cuota específica.
     * Esto sumaría múltiples pagos si una cuota se paga en varias transacciones.
     * @param idAlumno El ID del alumno.
     * @param idCuota El ID de la cuota.
     * @return El monto total pagado por el alumno para esa cuota.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public double obtenerMontoTotalPagadoPorAlumnoYCuota(int idAlumno, int idCuota) throws SQLException {
        double montoTotal = 0.0;
        String sql = "SELECT SUM(monto_pagado) FROM pagos WHERE id_alumno = ? AND id_cuota = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            pstmt.setInt(2, idCuota);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    montoTotal = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener el monto total pagado por el alumno " + idAlumno + " para la cuota " + idCuota + ": " + e.getMessage(), e);
        }
        return montoTotal;
    }

    /**
     * Obtiene una lista de pagos realizados en un mes específico.
     * @param mes El mes y año a consultar (YearMonth).
     * @return Una lista de objetos Pago correspondientes al mes.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Pago> obtenerPagosPorMes(YearMonth mes) throws SQLException {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT id_pago, id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo FROM pagos WHERE MONTH(fecha_pago) = ? AND YEAR(fecha_pago) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mes.getMonthValue()); 
            pstmt.setInt(2, mes.getYear());       
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener pagos para el mes " + mes + ": " + e.getMessage(), e);
        }
        return pagos;
    }

    /**
     * Obtiene la suma total de pagos realizados en un mes específico.
     * @param mes El mes y año a consultar (YearMonth).
     * @return La suma total de los montos pagados en el mes.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public double obtenerSumaPagosPorMes(YearMonth mes) throws SQLException {
        double total = 0.0;
        String sql = "SELECT SUM(monto_pagado) FROM pagos WHERE MONTH(fecha_pago) = ? AND YEAR(fecha_pago) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mes.getMonthValue());
            pstmt.setInt(2, mes.getYear());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener la suma de pagos para el mes " + mes + ": " + e.getMessage(), e);
        }
        return total;
    }

    /**
     * Obtiene los IDs de los alumnos que han realizado al menos un pago en un mes específico.
     * @param mes El mes y año a consultar (YearMonth).
     * @return Una lista de IDs de alumnos únicos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Integer> obtenerIdsAlumnosConPagoEnMes(YearMonth mes) throws SQLException {
        List<Integer> alumnoIds = new ArrayList<>();
        String sql = "SELECT DISTINCT id_alumno FROM pagos WHERE MONTH(fecha_pago) = ? AND YEAR(fecha_pago) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mes.getMonthValue());
            pstmt.setInt(2, mes.getYear());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    alumnoIds.add(rs.getInt("id_alumno"));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener IDs de alumnos con pagos en el mes " + mes + ": " + e.getMessage(), e);
        }
        return alumnoIds;
    }

    /**
     * Obtiene la suma de pagos de un alumno para una cuota específica.
     * @param idAlumno El ID del alumno.
     * @param idCuota El ID de la cuota.
     * @return El monto total pagado por el alumno para esa cuota.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public double obtenerSumaPagosPorAlumnoYCuota(int idAlumno, int idCuota) throws SQLException {
        double totalPagado = 0.0;
        String sql = "SELECT SUM(monto_pagado) FROM pagos WHERE id_alumno = ? AND id_cuota = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idAlumno);
            pstmt.setInt(2, idCuota);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalPagado = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener la suma de pagos del alumno " + idAlumno + " para la cuota " + idCuota + ": " + e.getMessage(), e);
        }
        return totalPagado;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Pago.
     * Extrae los datos de la fila actual del ResultSet y crea un objeto Pago.
     * Se alinea con los campos de la tabla `pagos` en la base de datos.
     * @param rs El ResultSet del que extraer los datos.
     * @return Un objeto Pago con los datos de la fila actual.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private Pago mapResultSetToPago(ResultSet rs) throws SQLException {
        // Asegúrate de que el constructor de Pago tenga estos campos en este orden o ajusta la llamada.
        // Se asume que el modelo Pago ahora tiene un campo `boolean tieneRecargo` en lugar de `esPagoParcial`.
        // Si tu modelo Pago aún usa `esPagoParcial`, deberás decidir cómo se mapea desde `tiene_recargo` y `monto_recargo`.
        // Para este ejemplo, si `Pago` tiene `boolean esPagoParcial`, se le pasa `rs.getBoolean("tiene_recargo")`
        // o `rs.getDouble("monto_recargo") > 0`, dependiendo de tu lógica de negocio para `esPagoParcial`.
        
        // Asumiendo que el constructor de Pago es similar a:
        // Pago(int idPago, int idAlumno, int idCuota, LocalDate fechaPago, double montoPagado, String tipoPago, boolean tieneRecargo, double montoRecargoAplicado)
        return new Pago(
            rs.getInt("id_pago"),
            rs.getInt("id_alumno"),
            rs.getInt("id_cuota"), 
            rs.getDate("fecha_pago").toLocalDate(),
            rs.getDouble("monto_pagado"),
            rs.getString("tipo_pago"),
            rs.getBoolean("tiene_recargo"), // Directamente desde la columna de la DB
            rs.getDouble("monto_recargo") 
        );
    }
}