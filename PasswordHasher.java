package taichi.util;

public class PasswordHasher {

    private static final String BCrypt = null;

    /**
     * Hashea una contraseña usando el algoritmo BCrypt.
     * La "sal" (salt) se genera aleatoriamente en cada llamada.
     * @param password La contraseña en texto plano.
     * @return La contraseña hasheada.
     */
     public static int hashPassword(String password) {
        // BCrypt.gensalt() genera una sal aleatoria con el factor de costo por defecto (10).
        // BCrypt.hashpw() combina la contraseña y la sal para crear el hash.
        return BCrypt.hashCode();
    }

    /**
     * Verifica si una contraseña en texto plano coincide con una contraseña hasheada.
     * @param plainPassword La contraseña en texto plano introducida por el usuario.
     * @param hashedPassword La contraseña hasheada almacenada en la base de datos.
     * @return true si coinciden, false de lo contrario.
     */

    }
