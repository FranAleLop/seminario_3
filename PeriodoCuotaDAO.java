package taichi.dao;

import taichi.model.PeriodoCuota; // Importamos la clase PeriodoCuota
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PeriodoCuotaDAO {

    /**
     * Inserta un nuevo período de cuota en la base de datos.
     * @param periodo El objeto PeriodoCuota a insertar.
     * @return El objeto PeriodoCuota con el ID generado asignado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public PeriodoCuota crear(PeriodoCuota periodo) throws SQLException { // Cambiamos el nombre a 'crear' para consistencia
        // Ajustamos la sentencia SQL para que coincida con la tabla 'periodos_cuotas' en nuestro esquema MySQL.
        // Columnas en BD: id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo
        String sql = "INSERT INTO periodos_cuotas (nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Usamos Statement para claridad

            pstmt.setString(1, periodo.getNombrePeriodo());
            pstmt.setDate(2, java.sql.Date.valueOf(periodo.getFechaInicio())); // Convertir LocalDate a java.sql.Date
            pstmt.setDate(3, java.sql.Date.valueOf(periodo.getFechaFin()));
            pstmt.setDate(4, java.sql.Date.valueOf(periodo.getFechaVencimiento()));
            pstmt.setDouble(5, periodo.getMontoBase());
            pstmt.setDouble(6, periodo.getMontoRecargo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del período de cuota falló, no se insertaron filas.");
            }
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    periodo.setIdPeriodo(rs.getInt(1)); // Asignar el ID al objeto PeriodoCuota
                    System.out.println("Período de cuota insertado con ID: " + periodo.getIdPeriodo());
                } else {
                    throw new SQLException("La creación del período de cuota falló, no se obtuvo ID generado.");
                }
            }
        }
        return periodo;
    }

    /**
     * Obtiene un período de cuota por su ID.
     * @param id El ID del período a buscar.
     * @return El objeto PeriodoCuota si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public PeriodoCuota obtenerPorId(int id) throws SQLException {
        // Seleccionamos las columnas según el esquema MySQL
        // id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo
        String sql = "SELECT id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo FROM periodos_cuotas WHERE id_periodo_cuota = ?";
        PeriodoCuota periodo = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    periodo = new PeriodoCuota(
                        rs.getInt("id_periodo_cuota"), // En la BD es 'id_periodo_cuota', no 'id_periodo'
                        rs.getString("nombre_periodo"),
                        rs.getDate("fecha_inicio").toLocalDate(), // Convertir java.sql.Date a LocalDate
                        rs.getDate("fecha_fin").toLocalDate(),
                        rs.getDate("fecha_vencimiento").toLocalDate(),
                        rs.getDouble("monto_base"),
                        rs.getDouble("monto_recargo")
                    );
                }
            }
        }
        return periodo;
    }

    /**
     * Obtiene una lista de todos los períodos de cuota.
     * @return Una lista de objetos PeriodoCuota. Puede estar vacía si no hay períodos.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<PeriodoCuota> obtenerTodos() throws SQLException {
        List<PeriodoCuota> periodos = new ArrayList<>();
        // Seleccionamos las columnas según el esquema MySQL
        String sql = "SELECT id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo FROM periodos_cuotas";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                periodos.add(mapResultSetToPeriodoCuota(rs)); // Usamos el método auxiliar
            }
        }
        return periodos;
    }

    /**
     * Actualiza la información de un período de cuota existente.
     * @param periodo El objeto PeriodoCuota con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizar(PeriodoCuota periodo) throws SQLException {
        // Ajustamos la sentencia SQL para que coincida con la tabla 'periodos_cuotas' en MySQL.
        String sql = "UPDATE periodos_cuotas SET nombre_periodo = ?, fecha_inicio = ?, fecha_fin = ?, fecha_vencimiento = ?, monto_base = ?, monto_recargo = ? WHERE id_periodo_cuota = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, periodo.getNombrePeriodo());
            pstmt.setDate(2, java.sql.Date.valueOf(periodo.getFechaInicio()));
            pstmt.setDate(3, java.sql.Date.valueOf(periodo.getFechaFin()));
            pstmt.setDate(4, java.sql.Date.valueOf(periodo.getFechaVencimiento()));
            pstmt.setDouble(5, periodo.getMontoBase());
            pstmt.setDouble(6, periodo.getMontoRecargo());
            pstmt.setInt(7, periodo.getIdPeriodo()); // Cláusula WHERE, asumo getIdPeriodo() devuelve el ID correcto

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }

    /**
     * Elimina un período de cuota por su ID.
     * @param id El ID del período a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM periodos_cuotas WHERE id_periodo_cuota = ?"; // Nombre de tabla en minúsculas y ID
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
     * Obtiene una lista de períodos de cuota anteriores a un mes específico.
     *
     * @param mesActual El mes y año de referencia (YearMonth).
     * @return Una lista de objetos PeriodoCuota cuya `fecha_fin` es anterior al mes de referencia.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<PeriodoCuota> obtenerPeriodosAnterioresA(YearMonth mesActual) throws SQLException {
        List<PeriodoCuota> periodos = new ArrayList<>();
        // Para MySQL, comparamos la fecha_fin con el primer día del mes actual.
        // O si queremos meses completos anteriores, podemos comparar el año y el mes de fecha_fin.
        String sql = "SELECT id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo FROM periodos_cuotas WHERE YEAR(fecha_fin) < ? OR (YEAR(fecha_fin) = ? AND MONTH(fecha_fin) < ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, mesActual.getYear());
            pstmt.setInt(2, mesActual.getYear());
            pstmt.setInt(3, mesActual.getMonthValue());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    periodos.add(mapResultSetToPeriodoCuota(rs));
                }
            }
        }
        return periodos;
    }
    
    /**
     * Obtiene el período de cuota activo o el más reciente si no hay uno activo.
     * Un período "activo" podría definirse como aquel cuya fecha actual cae entre fecha_inicio y fecha_fin,
     * o simplemente el más reciente si solo hay uno.
     * Para esta implementación, buscaremos el período cuya fecha de inicio es la más reciente
     * y no ha finalizado (o finalizó más recientemente).
     *
     * @return El objeto PeriodoCuota activo/más reciente, o null si no se encuentra.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public PeriodoCuota obtenerPeriodoActivoOMasReciente() throws SQLException {
        // Esta consulta intenta encontrar un período que esté actualmente "activo"
        // (fecha actual entre fecha_inicio y fecha_fin).
        // Si no hay ninguno, obtiene el período con la fecha de fin más reciente.
        String sql = "SELECT id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo " +
                     "FROM periodos_cuotas " +
                     "WHERE ? BETWEEN fecha_inicio AND fecha_fin " + // Busca período activo
                     "ORDER BY fecha_inicio DESC, fecha_fin DESC " + // Si no hay activo, el más reciente
                     "LIMIT 1";
        
        PeriodoCuota periodo = null;
        LocalDate hoy = LocalDate.now(); // Fecha actual

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(hoy));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    periodo = mapResultSetToPeriodoCuota(rs);
                } else {
                    // Si no se encuentra un período activo para hoy, busca el más reciente finalizado o por iniciar
                    String fallbackSql = "SELECT id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo " +
                                         "FROM periodos_cuotas " +
                                         "ORDER BY fecha_fin DESC, fecha_inicio DESC " +
                                         "LIMIT 1";
                    try (PreparedStatement fallbackPstmt = conn.prepareStatement(fallbackSql);
                         ResultSet fallbackRs = fallbackPstmt.executeQuery()) {
                        if (fallbackRs.next()) {
                            periodo = mapResultSetToPeriodoCuota(fallbackRs);
                        }
                    }
                }
            }
        }
        return periodo;
    }


    /**
     * Método auxiliar para mapear un ResultSet a un objeto PeriodoCuota.
     * Extrae los datos de la fila actual del ResultSet y crea un objeto PeriodoCuota.
     * @param rs El ResultSet del que extraer los datos.
     * @return Un objeto PeriodoCuota con los datos de la fila actual.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private PeriodoCuota mapResultSetToPeriodoCuota(ResultSet rs) throws SQLException {
        // Asegúrate de que los nombres de las columnas coincidan con tu base de datos.
        // En tu esquema DB, la PK es 'id_periodo_cuota', no 'id_periodo'
        int idPeriodo = rs.getInt("id_periodo_cuota"); 
        String nombrePeriodo = rs.getString("nombre_periodo");
        LocalDate fechaInicio = rs.getDate("fecha_inicio").toLocalDate();
        LocalDate fechaFin = rs.getDate("fecha_fin").toLocalDate();
        LocalDate fechaVencimiento = rs.getDate("fecha_vencimiento").toLocalDate();
        double montoBase = rs.getDouble("monto_base");
        double montoRecargo = rs.getDouble("monto_recargo");

        // Asegúrate de que el constructor de PeriodoCuota sea compatible.
        return new PeriodoCuota(idPeriodo, nombrePeriodo, fechaInicio, fechaFin, fechaVencimiento, montoBase, montoRecargo);
    }
}