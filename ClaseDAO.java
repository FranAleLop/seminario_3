package taichi.dao;

import taichi.model.Clase; // Importamos la clase Clase
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClaseDAO {

    /**
     * Inserta una nueva clase en la base de datos.
     * @param clase El objeto Clase a insertar.
     * @return El ID generado para la nueva clase, o -1 si falla.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public int insertar(Clase clase) throws SQLException {
        String sql = "INSERT INTO Clases (nombre_clase, descripcion, horario, cupo_maximo, activa) VALUES (?, ?, ?, ?, ?)";
        int idGenerado = -1;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, clase.getNombreClase());
            pstmt.setString(2, clase.getDescripcion());
            pstmt.setString(3, clase.getHorario());
            pstmt.setInt(4, clase.getCupoMaximo());
            pstmt.setBoolean(5, clase.isActiva());

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                        clase.setIdClase(idGenerado); // Asignar el ID al objeto Clase
                        System.out.println("Clase insertada con ID: " + idGenerado);
                    }
                }
            }
        }
        return idGenerado;
    }

    /**
     * Obtiene una clase por su ID.
     * @param id El ID de la clase a buscar.
     * @return El objeto Clase si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public Clase obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id_clase, nombre_clase, descripcion, horario, cupo_maximo, activa FROM Clases WHERE id_clase = ?";
        Clase clase = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    clase = new Clase(
                        rs.getInt("id_clase"),
                        rs.getString("nombre_clase"),
                        rs.getString("descripcion"),
                        rs.getString("horario"),
                        rs.getInt("cupo_maximo"),
                        rs.getBoolean("activa")
                    );
                }
            }
        }
        return clase;
    }

    /**
     * Obtiene una lista de todas las clases.
     * @return Una lista de objetos Clase. Puede estar vacía si no hay clases.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<Clase> obtenerTodos() throws SQLException {
        List<Clase> clases = new ArrayList<>();
        String sql = "SELECT id_clase, nombre_clase, descripcion, horario, cupo_maximo, activa FROM Clases";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Clase clase = new Clase(
                    rs.getInt("id_clase"),
                    rs.getString("nombre_clase"),
                    rs.getString("descripcion"),
                    rs.getString("horario"),
                    rs.getInt("cupo_maximo"),
                    rs.getBoolean("activa")
                );
                clases.add(clase);
            }
        }
        return clases;
    }

    /**
     * Actualiza la información de una clase existente.
     * @param clase El objeto Clase con la información actualizada.
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizar(Clase clase) throws SQLException {
        String sql = "UPDATE Clases SET nombre_clase = ?, descripcion = ?, horario = ?, cupo_maximo = ?, activa = ? WHERE id_clase = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, clase.getNombreClase());
            pstmt.setString(2, clase.getDescripcion());
            pstmt.setString(3, clase.getHorario());
            pstmt.setInt(4, clase.getCupoMaximo());
            pstmt.setBoolean(5, clase.isActiva());
            pstmt.setInt(6, clase.getIdClase());

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }

    /**
     * Elimina una clase por su ID.
     * @param id El ID de la clase a eliminar.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Clases WHERE id_clase = ?";
        int filasAfectadas = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            filasAfectadas = pstmt.executeUpdate();
        }
        return filasAfectadas > 0;
    }
}