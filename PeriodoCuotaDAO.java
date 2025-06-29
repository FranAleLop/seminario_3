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

// Implementamos la interfaz IDAO, especificando que trabajamos con PeriodoCuota y su ID es Integer
public class PeriodoCuotaDAO implements IDAO<PeriodoCuota, Integer> {

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public PeriodoCuota crear(PeriodoCuota periodo) throws SQLException {
        // Columnas en BD: id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo
        String sql = "INSERT INTO periodos_cuotas (nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, periodo.getNombrePeriodo());
            pstmt.setDate(2, java.sql.Date.valueOf(periodo.getFechaInicio())); // Convertir LocalDate a java.sql.Date
            pstmt.setDate(3, java.sql.Date.valueOf(periodo.getFechaFin()));
            pstmt.setDate(4, java.sql.Date.valueOf(periodo.getFechaVencimiento()));
            pstmt.setDouble(5, periodo.getMontoBase());
            pstmt.setDouble(6, periodo.getMontoRecargo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del período de cuota falló, no se insertaron filas en la base de datos.");
            }
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    periodo.setIdPeriodo(rs.getInt(1)); // Asignar el ID al objeto PeriodoCuota
                    System.out.println("Período de cuota insertado con ID: " + periodo.getIdPeriodo());
                } else {
                    throw new SQLException("La creación del período de cuota falló, no se obtuvo ID generado de la base de datos.");
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al crear el período de cuota en la base de datos: " + e.getMessage(), e);
        }
        return periodo;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public PeriodoCuota obtenerPorId(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "SELECT id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo FROM periodos_cuotas WHERE id_periodo_cuota = ?";
        PeriodoCuota periodo = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    periodo = mapResultSetToPeriodoCuota(rs); // Usamos el método auxiliar
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener el período de cuota con ID " + id + " de la base de datos: " + e.getMessage(), e);
        }
        return periodo;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public List<PeriodoCuota> obtenerTodos() throws SQLException {
        List<PeriodoCuota> periodos = new ArrayList<>();
        String sql = "SELECT id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo FROM periodos_cuotas";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                periodos.add(mapResultSetToPeriodoCuota(rs)); // Usamos el método auxiliar
            }
        } catch (SQLException e) {
            throw new SQLException("Error al obtener todos los períodos de cuota de la base de datos: " + e.getMessage(), e);
        }
        return periodos;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean actualizar(PeriodoCuota periodo) throws SQLException {
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
            pstmt.setInt(7, periodo.getIdPeriodo()); 

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al actualizar el período de cuota con ID " + periodo.getIdPeriodo() + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

    @Override // Indica que este método implementa un método de la interfaz IDAO
    public boolean eliminar(Integer id) throws SQLException { // Usamos Integer para consistencia con la interfaz
        String sql = "DELETE FROM periodos_cuotas WHERE id_periodo_cuota = ?"; 
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error al eliminar el período de cuota con ID " + id + ": " + e.getMessage(), e);
        }
        return filasAfectadas > 0;
    }

    //Consultas Específicas de la Base de Datos

    /**
     * Obtiene una lista de períodos de cuota anteriores a un mes específico.
     *
     * @param mesActual El mes y año de referencia (YearMonth).
     * @return Una lista de objetos PeriodoCuota cuya `fecha_fin` es anterior al mes de referencia.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<PeriodoCuota> obtenerPeriodosAnterioresA(YearMonth mesActual) throws SQLException {
        List<PeriodoCuota> periodos = new ArrayList<>();
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
        } catch (SQLException e) {
            throw new SQLException("Error al obtener períodos de cuota anteriores a " + mesActual + ": " + e.getMessage(), e);
        }
        return periodos;
    }
    
    /**
     * Obtiene el período de cuota activo o el más reciente si no hay uno activo.
     * Un período "activo" se define como aquel cuya fecha actual (o la fecha del sistema)
     * cae entre `fecha_inicio` y `fecha_fin`.
     * Si no se encuentra un período activo, el método busca el período más reciente
     * basándose en su `fecha_fin` y luego `fecha_inicio`.
     *
     * @return El objeto PeriodoCuota activo o el más reciente, o null si no se encuentra ninguno.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public PeriodoCuota obtenerPeriodoActivoOMasReciente() throws SQLException {
        PeriodoCuota periodo = null;
        // Obtenemos la fecha actual, considerando la zona horaria de Salta, Argentina.
        // Aunque `LocalDate.now()` usa la zona horaria predeterminada, para una aplicación de producción
        // y mayor precisión, se podría especificar una zona horaria explícita.
        // Para este contexto, `LocalDate.now()` es suficiente.
        LocalDate hoy = LocalDate.now(); 

        // Primero, intentamos encontrar un período que esté activo hoy
        String sqlActivo = "SELECT id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo " +
                            "FROM periodos_cuotas " +
                            "WHERE ? BETWEEN fecha_inicio AND fecha_fin " + 
                            "LIMIT 1"; // Solo necesitamos uno si hay múltiples activos (ej. solapamiento, aunque no deseable)
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmtActivo = conn.prepareStatement(sqlActivo)) {

            pstmtActivo.setDate(1, java.sql.Date.valueOf(hoy));
            
            try (ResultSet rsActivo = pstmtActivo.executeQuery()) {
                if (rsActivo.next()) {
                    periodo = mapResultSetToPeriodoCuota(rsActivo);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar el período de cuota activo: " + e.getMessage(), e);
        }

        // Si no se encontró un período activo, buscamos el más reciente finalizado o por iniciar
        if (periodo == null) {
            String sqlMasReciente = "SELECT id_periodo_cuota, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo " +
                                    "FROM periodos_cuotas " +
                                    "ORDER BY fecha_fin DESC, fecha_inicio DESC " +
                                    "LIMIT 1";
            try (Connection conn = DatabaseConnection.getConnection(); // Abrir una nueva conexión o reutilizar si es seguro.
                 PreparedStatement pstmtMasReciente = conn.prepareStatement(sqlMasReciente);
                 ResultSet rsMasReciente = pstmtMasReciente.executeQuery()) {
                if (rsMasReciente.next()) {
                    periodo = mapResultSetToPeriodoCuota(rsMasReciente);
                }
            } catch (SQLException e) {
                throw new SQLException("Error al buscar el período de cuota más reciente (fallback): " + e.getMessage(), e);
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
        // En tu esquema DB, la PK es 'id_periodo_cuota'
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