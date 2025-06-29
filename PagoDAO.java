package taichi.dao;

import taichi.model.Pago;     // Importamos la clase Pago
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Importar para PreparedStatement.RETURN_GENERATED_KEYS
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    /**
     * Inserta un nuevo pago en la base de datos.
     * @param pago El objeto Pago a insertar.
     * @return El objeto Pago con el ID generado asignado.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Pago crear(Pago pago) throws SQLException { // Cambiamos el nombre a 'crear' para consistencia
        // Ajustamos la sentencia SQL para que coincida con la tabla 'pagos' en nuestro esquema MySQL.
        // Columnas en BD: id_pago, id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo
        // Tu DAO usa 'id_periodo' -> debe ser 'id_cuota'
        // Tu DAO usa 'es_pago_parcial' -> en la BD es 'tiene_recargo' (que es distinto)
        // Tu DAO usa 'monto_recargo_aplicado' -> en la BD es 'monto_recargo'
        String sql = "INSERT INTO pagos (id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, pago.getIdAlumno());
            // CRÍTICO: id_periodo en tu Pago.java debe ser id_cuota para la DB
            pstmt.setInt(2, pago.getIdCuota()); // Asumiendo que Pago.getIdCuota() existe
            pstmt.setDate(3, java.sql.Date.valueOf(pago.getFechaPago())); // LocalDate a java.sql.Date
            pstmt.setDouble(4, pago.getMontoPagado());
            pstmt.setString(5, pago.getTipoPago());
            
            // Aquí hay una diferencia importante: 'es_pago_parcial' vs. 'tiene_recargo'
            // En tu modelo, 'es_pago_parcial' puede indicar que no se pagó el total esperado de la cuota.
            // En la BD, 'tiene_recargo' indica si se aplicó un recargo.
            // Necesitamos asegurarnos de que el modelo Pago refleje esto o decidir cómo se mapea.
            // Si 'monto_recargo_aplicado' > 0, entonces 'tiene_recargo' es TRUE.
            boolean tieneRecargo = pago.getMontoRecargoAplicado() > 0;
            pstmt.setBoolean(6, tieneRecargo); // Mapeamos a 'tiene_recargo'
            pstmt.setDouble(7, pago.getMontoRecargoAplicado());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La creación del pago falló, no se insertaron filas.");
            }
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pago.setIdPago(rs.getInt(1)); // Asignar el ID al objeto Pago
                    System.out.println("Pago insertado con ID: " + pago.getIdPago());
                } else {
                    throw new SQLException("La creación del pago falló, no se obtuvo ID generado.");
                }
            }
        }
        return pago;
    }

    /**
     * Obtiene un pago por su ID.
     * @param id El ID del pago a buscar.
     * @return El objeto Pago si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Pago obtenerPorId(int id) throws SQLException {
        // Seleccionamos las columnas según el esquema MySQL
        // id_pago, id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo
        String sql = "SELECT id_pago, id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo FROM pagos WHERE id_pago = ?";
        Pago pago = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Mapeamos 'tiene_recargo' de la BD a 'es_pago_parcial' o un nuevo campo en Pago
                    // Aquí asumo que tu modelo Pago todavía tiene 'es_pago_parcial'
                    // Pero semánticamente no son lo mismo. Sugiero que el modelo Pago tenga un campo 'tieneRecargo'.
                    boolean tieneRecargoDb = rs.getBoolean("tiene_recargo");
                    // Aquí, si monto_pagado es menor que el monto de la cuota, es pago parcial.
                    // Esto no se puede determinar solo con el Pago, necesitarías el monto de la Cuota.
                    // Por simplicidad, si tu modelo Pago aún tiene 'es_pago_parcial', podrías asumirlo de alguna forma
                    // o eliminarlo si la BD no lo soporta directamente.
                    // Para este DAO, vamos a mantener 'es_pago_parcial' si tu modelo Pago lo tiene,
                    // pero ten en cuenta la implicación de que no viene directamente de la DB.
                    // **Alternativa:** Elimina 'es_pago_parcial' del constructor de Pago si ya no lo usas.
                    
                    pago = new Pago(
                        rs.getInt("id_pago"),
                        rs.getInt("id_alumno"),
                        rs.getInt("id_cuota"), // Cambiado de id_periodo a id_cuota
                        rs.getDate("fecha_pago").toLocalDate(), // java.sql.Date a LocalDate
                        rs.getDouble("monto_pagado"),
                        rs.getString("tipo_pago"),
                        // El campo `es_pago_parcial` no está en la base de datos `pagos`.
                        // Su valor en el objeto `Pago` deberá derivarse de la lógica de negocio (ej. `montoPagado < montoTotalCuota`).
                        // Aquí lo inicializaremos a `false` o según una lógica que definas si realmente es necesario.
                        // Para evitar un error de constructor, si `Pago` tiene `boolean esPagoParcial`, ponle `false` por ahora.
                        false, // Asumiendo que tu constructor de Pago aún requiere esto. Revisa el modelo Pago.
                        rs.getDouble("monto_recargo") // Cambiado de monto_recargo_aplicado a monto_recargo
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
        // Seleccionamos las columnas según el esquema MySQL
        String sql = "SELECT id_pago, id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo FROM pagos";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                pagos.add(mapResultSetToPago(rs)); // Usamos el método auxiliar
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
        // Ajustamos la sentencia SQL para que coincida con la tabla 'pagos' en MySQL.
        String sql = "UPDATE pagos SET id_alumno = ?, id_cuota = ?, fecha_pago = ?, monto_pagado = ?, tipo_pago = ?, tiene_recargo = ?, monto_recargo = ? WHERE id_pago = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pago.getIdAlumno());
            pstmt.setInt(2, pago.getIdCuota()); // Cambiado de id_periodo a id_cuota
            pstmt.setDate(3, java.sql.Date.valueOf(pago.getFechaPago()));
            pstmt.setDouble(4, pago.getMontoPagado());
            pstmt.setString(5, pago.getTipoPago());
            
            boolean tieneRecargo = pago.getMontoRecargoAplicado() > 0;
            pstmt.setBoolean(6, tieneRecargo); // Mapeamos a 'tiene_recargo'
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
        String sql = "DELETE FROM pagos WHERE id_pago = ?"; // Nombre de tabla en minúsculas
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
        String sql = "SELECT COUNT(*) FROM pagos WHERE monto_recargo > 0"; // 'monto_recargo'
        int totalPagosConRecargo = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                totalPagosConRecargo = rs.getInt(1);
            }
        }
        return totalPagosConRecargo;
    }

    /**
     * Verifica si un alumno ha pagado una cuota completa para un periodo específico.
     * NOTA: Esta lógica es compleja ya que "cuota completa" no se registra directamente en 'pagos'.
     * Debes comparar el `monto_pagado` con el `monto_total` de la cuota correspondiente.
     * Este método solo verifica si **no** se aplicó un recargo, lo que NO es sinónimo de pago completo.
     * Para verificar un pago completo, necesitarías el DAO de Cuota.
     *
     * @param idAlumno El ID del alumno.
     * @param idCuota El ID de la cuota (no periodo).
     * @return true si no se aplicó recargo al pago de esa cuota por ese alumno.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean haPagadoCuotaSinRecargo(int idAlumno, int idCuota) throws SQLException {
        // Renombre para reflejar la realidad del esquema DB.
        // Para saber si la cuota es "completa", necesitas el monto de la cuota desde la tabla `cuotas_alumnos`
        // y compararlo con el `SUM(monto_pagado)` de la tabla `pagos`.
        String sql = "SELECT COUNT(*) FROM pagos WHERE id_alumno = ? AND id_cuota = ? AND tiene_recargo = FALSE";
        int pagosSinRecargo = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            pstmt.setInt(2, idCuota); // Cambiado de id_periodo a id_cuota

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pagosSinRecargo = rs.getInt(1);
                }
            }
        }
        return pagosSinRecargo > 0;
    }

    /**
     * Obtiene el monto total pagado por un alumno para una cuota específica.
     * @param idAlumno El ID del alumno.
     * @param idCuota El ID de la cuota.
     * @return El monto total pagado por el alumno para esa cuota.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public double obtenerMontoTotalPagadoPorAlumnoYCuota(int idAlumno, int idCuota) throws SQLException {
        // Cambiado de id_periodo a id_cuota en el nombre del método y SQL
        String sql = "SELECT SUM(monto_pagado) FROM pagos WHERE id_alumno = ? AND id_cuota = ?";
        double montoTotal = 0.0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idAlumno);
            pstmt.setInt(2, idCuota);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    montoTotal = rs.getDouble(1);
                }
            }
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
        // **CRÍTICO**: Usar la función de fecha de MySQL para comparar.
        // Aquí usamos MONTH() y YEAR().
        String sql = "SELECT id_pago, id_alumno, id_cuota, fecha_pago, monto_pagado, tipo_pago, tiene_recargo, monto_recargo FROM pagos WHERE MONTH(fecha_pago) = ? AND YEAR(fecha_pago) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mes.getMonthValue()); // Obtiene el número del mes (1-12)
            pstmt.setInt(2, mes.getYear());       // Obtiene el año
            
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
     * @param mes El mes y año a consultar (YearMonth).
     * @return La suma total de los montos pagados en el mes.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public double obtenerSumaPagosPorMes(YearMonth mes) throws SQLException {
        double total = 0.0;
        // **CRÍTICO**: Usar la función de fecha de MySQL.
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
        // **CRÍTICO**: Usar la función de fecha de MySQL.
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
        // Renombre para reflejar que es 'id_cuota'
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
        }
        return totalPagado;
    }

    /**
     * Método auxiliar para mapear un ResultSet a un objeto Pago.
     * Extrae los datos de la fila actual del ResultSet y crea un objeto Pago.
     * @param rs El ResultSet del que extraer los datos.
     * @return Un objeto Pago con los datos de la fila actual.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private Pago mapResultSetToPago(ResultSet rs) throws SQLException {
        int idPago = rs.getInt("id_pago");
        int idAlumno = rs.getInt("id_alumno");
        int idCuota = rs.getInt("id_cuota"); // Cambiado de id_periodo a id_cuota
        LocalDate fechaPago = rs.getDate("fecha_pago").toLocalDate();
        double montoPagado = rs.getDouble("monto_pagado");
        String tipoPago = rs.getString("tipo_pago");
        
        // El campo `es_pago_parcial` no está en la base de datos `pagos`.
        // Su valor en el objeto `Pago` deberá derivarse de la lógica de negocio (ej. `montoPagado < montoTotalCuota`).
        // Para evitar un error de constructor, si `Pago` tiene `boolean esPagoParcial`, ponle `false` por ahora.
        // O mejor aún, haz que el modelo Pago tenga `boolean tieneRecargo` y lo uses.
        boolean esPagoParcialDummy = false; // Valor temporal, debe derivarse de la lógica de la cuota
        
        // Nuevo campo `tieneRecargo` de la base de datos
        boolean tieneRecargo = rs.getBoolean("tiene_recargo"); 
        double montoRecargoAplicado = rs.getDouble("monto_recargo"); // Cambiado de monto_recargo_aplicado a monto_recargo

        // Asumo que tu constructor de Pago ha sido actualizado para reflejar estos cambios.
        // Si no tienes `es_pago_parcial` en tu modelo, quítalo de aquí.
        // Si tu modelo Pago tiene un constructor con `tieneRecargo` en lugar de `es_pago_parcial`, ajusta.
        return new Pago(idPago, idAlumno, idCuota, fechaPago, montoPagado, tipoPago, esPagoParcialDummy, montoRecargoAplicado);
    }
}