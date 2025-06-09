package taichi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // --- Configuración de la Base de Datos ---
    // Se Reemplaza estos valores de la configuración de MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/taichi_db"; // URL de la base de datos
    private static final String USER = "root";                                // Usuario de la base de datos
    private static final String PASSWORD = "admin";                           // Contraseña del usuario

    // --- Método para obtener una conexión a la BD ---
    public static Connection getConnection() throws SQLException {
        Connection connection = null;
        try {
            // Cargar el driver JDBC de MySQL (no siempre necesario en JDK 6+ pero es buena práctica)
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establecer la conexión
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a la base de datos."); // Solo para depuración
            return connection;
        } catch (ClassNotFoundException e) {
            // Manejo de excepción si el driver no se encuentra
            System.err.println("Error: Driver JDBC de MySQL no encontrado.");
            throw new SQLException("No se pudo cargar el driver JDBC de MySQL.", e);
        } catch (SQLException e) {
            // Manejo de excepción si hay un error en la conexión
            System.err.println("Error de conexión a la base de datos: " + e.getMessage());
            throw e; // Relanza la excepción para que sea manejada por la capa superior (DAO/Controller)
        }
    }

    // --- Método para cerrar una conexión a la BD ---
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexión cerrada."); // Solo para depuración
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión a la base de datos: " + e.getMessage());
            }
        }
    }
}