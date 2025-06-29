package taichi.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

       // --- Configuración para MySQL ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/taichi_academia"; // Se cambia 'localhost' y '3306' si el MySQL está en otro lugar/puerto
    private static final String DB_USER = "root"; // Tu usuario de MySQL
    private static final String DB_PASSWORD = "password"; // Tu contraseña de MySQL

    // Nombre del driver JDBC para MySQL
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Establece y retorna una conexión a la base de datos MySQL.
     *
     * @return Una instancia de Connection.
     * @throws SQLException Si ocurre un error al conectar con la base de datos.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Cargar el driver JDBC de MySQL
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver JDBC de MySQL no encontrado. Asegúrate de que el JAR del driver esté en el classpath.");
            throw new SQLException("Error al cargar el driver de la base de datos.", e);
        }
        // Establecer la conexión
        // Se añaden parámetros para la zona horaria y el uso de SSL (común en MySQL)
        return DriverManager.getConnection(DB_URL + "?useSSL=false&serverTimezone=UTC", DB_USER, DB_PASSWORD);
    }

    /**
     * Cierra una conexión a la base de datos.
     *
     * @param connection La conexión a cerrar.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión a la base de datos: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}