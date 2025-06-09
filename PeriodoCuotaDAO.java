package taichi.dao;

import taichi.model.PeriodoCuota; // Importamos la clase PeriodoCuota
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PeriodoCuotaDAO {

    /**
     * Inserta un nuevo período de cuota en la base de datos.
     * @param periodo El objeto PeriodoCuota a insertar.
     * @return El ID generado para el nuevo período, o -1 si falla.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int insertar(PeriodoCuota periodo) throws SQLException {
        String sql = "INSERT INTO PeriodosCuota (nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo) VALUES (?, ?, ?, ?, ?, ?)";
        int idGenerado = -1;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, periodo.getNombrePeriodo());
            pstmt.setDate(2, java.sql.Date.valueOf(periodo.getFechaInicio())); // Convertir LocalDate a java.sql.Date
            pstmt.setDate(3, java.sql.Date.valueOf(periodo.getFechaFin()));
            pstmt.setDate(4, java.sql.Date.valueOf(periodo.getFechaVencimiento()));
            pstmt.setDouble(5, periodo.getMontoBase());
            pstmt.setDouble(6, periodo.getMontoRecargo());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        periodo.setIdPeriodo(idGenerado); // Asignar el ID al objeto PeriodoCuota
                        System.out.println("Período de cuota insertado con ID: " + idGenerado);
                    }
                }
            }
        }
        return idGenerado;
    }

    /**
     * Obtiene un período de cuota por su ID.
     * @param id El ID del período a buscar.
     * @return El objeto PeriodoCuota si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public PeriodoCuota obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_periodo, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo FROM PeriodosCuota WHERE id_periodo = ?";
        PeriodoCuota periodo = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    periodo = new PeriodoCuota(
                        rs.getInt("id_periodo"),
                        rs.getString("nombre_periodo"),
                        rs.getDate("fecha_inicio").toLocalDate(),
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
        String sql = "SELECT id_periodo, nombre_periodo, fecha_inicio, fecha_fin, fecha_vencimiento, monto_base, monto_recargo FROM PeriodosCuota";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                PeriodoCuota periodo = new PeriodoCuota(
                    rs.getInt("id_periodo"),
                    rs.getString("nombre_periodo"),
                    rs.getDate("fecha_inicio").toLocalDate(),
                    rs.getDate("fecha_fin").toLocalDate(),
                    rs.getDate("fecha_vencimiento").toLocalDate(),
                    rs.getDouble("monto_base"),
                    rs.getDouble("monto_recargo")
                );
                periodos.add(periodo);
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
        String sql = "UPDATE PeriodosCuota SET nombre_periodo = ?, fecha_inicio = ?, fecha_fin = ?, fecha_vencimiento = ?, monto_base = ?, monto_recargo = ? WHERE id_periodo = ?";
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
        String sql = "DELETE FROM PeriodosCuota WHERE id_periodo = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }
    public List<PeriodoCuota> obtenerPeriodosAnterioresA(YearMonth mesActual) throws SQLException {
        List<PeriodoCuota> periodos = new ArrayList<>();
        // Esto es una suposición. Si tu tabla 'periodos_cuota' tiene columnas 'mes' y 'anio' (INT), sería más directo.
        // Si 'nombre_periodo' es como "Cuota Enero 2024", la lógica de SQL sería más compleja o se haría en Java.
        // Para simplicidad, vamos a asumir que la tabla tiene una columna `fecha_inicio_periodo` de tipo DATE.
        // Si tu modelo PeriodoCuota no tiene fecha_inicio_periodo, deberás ajustarlo o adaptar la consulta.
        String sql = "SELECT * FROM periodos_cuota WHERE strftime('%Y-%m', fecha_inicio_periodo) < ?"; // SQLite
        // O: "SELECT * FROM periodos_cuota WHERE (anio_periodo < ? OR (anio_periodo = ? AND mes_periodo < ?))"
        // Si tienes columnas `mes_periodo` y `anio_periodo` (INT) en tu tabla periodos_cuota, usa esa.
        // Si solo tienes `nombre_periodo` como "Cuota Enero 2024", será muy difícil hacer esto en SQL directamente.
        // Te recomiendo añadir campos `mes_num` INT y `anio_num` INT a tu tabla `periodos_cuota`.

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, mesActual.format(DateTimeFormatter.ofPattern("yyyy-MM"))); // Compara con el mes/año actual
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    periodos.add(mapResultSetToPeriodoCuota(rs));
                }
            }
        }
        return periodos;
    }
    
    // Método auxiliar (ya debería existir o ser similar)
    private PeriodoCuota mapResultSetToPeriodoCuota(ResultSet rs) throws SQLException {
        int idPeriodo = rs.getInt("id_periodo");
        String nombrePeriodo = rs.getString("nombre_periodo");
        // Asegúrate de que tu modelo PeriodoCuota tenga un campo para el monto si lo usas en el reporte de deudas
        double monto = rs.getDouble("monto"); // Suponiendo que tienes un campo 'monto' en tu tabla
        // Si tienes una fecha de inicio en la BD, la mapeas aquí:
        LocalDate fechaInicio = rs.getDate("fecha_inicio_periodo") != null ? rs.getDate("fecha_inicio_periodo").toLocalDate() : null;

        return new PeriodoCuota(idPeriodo, nombrePeriodo, monto, fechaInicio, false); // Ajusta el constructor si tu modelo es diferente
    }
}