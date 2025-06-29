package taichi.util;

import org.mindrot.jbcrypt.BCrypt; // Importamos la clase BCrypt de la librería jBCrypt

public class PasswordHasher {

    /**
     * Hashea una contraseña usando el algoritmo BCrypt.
     * La "sal" (salt) se genera aleatoriamente en cada llamada.
     *
     * @param password La contraseña en texto plano.
     * @return La contraseña hasheada (un String que incluye la sal y el hash).
     */
    public static String hashPassword(String password) {
        // BCrypt.gensalt() genera una sal aleatoria con un factor de costo por defecto.
        // Un factor de costo más alto hace el hash más lento y seguro, pero consume más recursos.
        // El valor por defecto (10) suele ser un buen punto de partida.
        String salt = BCrypt.gensalt();
        
        // BCrypt.hashpw() combina la contraseña en texto plano con la sal para crear el hash.
        return BCrypt.hashpw(password, salt);
    }

    /**
     * Verifica si una contraseña en texto plano coincide con una contraseña hasheada.
     *
     * @param plainPassword La contraseña en texto plano introducida por el usuario (ej. durante el login).
     * @param hashedPassword La contraseña hasheada almacenada en la base de datos.
     * @return true si la contraseña en texto plano, cuando se hashea con la sal incrustada en hashedPassword,
     * coincide con el hash almacenado; false de lo contrario.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        // BCrypt.checkpw() se encarga automáticamente de extraer la sal del hashedPassword,
        // hashear plainPassword con esa sal y comparar el resultado con hashedPassword.
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}