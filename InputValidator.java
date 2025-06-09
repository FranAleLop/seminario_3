package taichi.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase utilitaria para validar diferentes tipos de entradas de usuario.
 * Proporciona métodos estáticos para validaciones comunes como cadenas no vacías,
 * números, fechas, emails, etc.
 */
public class InputValidator {

    private static final String EMAIL_REGEX =
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Valida que una cadena de texto no sea nula ni esté vacía.
     *
     * @param text El texto a validar.
     * @param fieldName El nombre del campo para el mensaje de error.
     * @return true si el texto es válido, false en caso contrario.
     * @throws IllegalArgumentException si el texto es nulo o vacío.
     */
    public static boolean isNotNullOrEmpty(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " no puede estar vacío.");
        }
        return true;
    }

    /**
     * Valida que una cadena de texto no sea nula, no esté vacía y tenga una longitud mínima.
     *
     * @param text El texto a validar.
     * @param fieldName El nombre del campo para el mensaje de error.
     * @param minLength La longitud mínima requerida.
     * @return true si el texto es válido, false en caso contrario.
     * @throws IllegalArgumentException si el texto es nulo, vacío o no cumple la longitud mínima.
     */
    public static boolean isValidStringLength(String text, String fieldName, int minLength) {
        isNotNullOrEmpty(text, fieldName); // Primero validar que no esté vacío
        if (text.trim().length() < minLength) {
            throw new IllegalArgumentException(fieldName + " debe tener al menos " + minLength + " caracteres.");
        }
        return true;
    }

    /**
     * Valida que una cadena de texto sea un número entero válido.
     *
     * @param text El texto a validar.
     * @param fieldName El nombre del campo para el mensaje de error.
     * @return El valor entero parseado.
     * @throws IllegalArgumentException si el texto no es un entero válido.
     */
    public static int parseInteger(String text, String fieldName) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " debe ser un número entero válido.");
        }
    }

    /**
     * Valida que una cadena de texto sea un número de punto flotante válido.
     *
     * @param text El texto a validar.
     * @param fieldName El nombre del campo para el mensaje de error.
     * @return El valor double parseado.
     * @throws IllegalArgumentException si el texto no es un double válido.
     */
    public static double parseDouble(String text, String fieldName) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " debe ser un número válido.");
        }
    }

    /**
     * Valida que una cadena de texto sea una fecha válida en formato YYYY-MM-DD.
     * Permite que la fecha sea opcional (nula o vacía).
     *
     * @param text El texto de la fecha a validar.
     * @param fieldName El nombre del campo para el mensaje de error.
     * @param optional Si la fecha puede ser nula/vacía.
     * @return La fecha parseada como LocalDate, o null si es opcional y vacía.
     * @throws IllegalArgumentException si la fecha no es válida y no es opcional o el formato es incorrecto.
     */
    public static LocalDate parseDate(String text, String fieldName, boolean optional) {
        if (text == null || text.trim().isEmpty()) {
            if (optional) {
                return null; // Es opcional y está vacío
            } else {
                throw new IllegalArgumentException(fieldName + " no puede estar vacío.");
            }
        }
        try {
            return LocalDate.parse(text.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " debe tener el formato YYYY-MM-DD.");
        }
    }

    /**
     * Valida que una cadena de texto sea una dirección de correo electrónico válida.
     *
     * @param email El email a validar.
     * @param fieldName El nombre del campo para el mensaje de error.
     * @param optional Si el email puede ser nulo/vacío.
     * @return true si el email es válido o nulo/vacío si es opcional, false en caso contrario.
     * @throws IllegalArgumentException si el email no es válido y no es opcional.
     */
    public static boolean isValidEmail(String email, String fieldName, boolean optional) {
        if (email == null || email.trim().isEmpty()) {
            if (optional) {
                return true; // Es opcional y está vacío
            } else {
                throw new IllegalArgumentException(fieldName + " no puede estar vacío.");
            }
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(fieldName + " no es una dirección de correo electrónico válida.");
        }
        return true;
    }

    public static boolean isNullOrEmpty(String nombreCompleto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isNullOrEmpty'");
    }

    public static boolean isValidDni(String dni) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isValidDni'");
    }

    public static boolean isPositive(int idClase) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isPositive'");
    }

    public static boolean isNullOrFutureDate(LocalDate fechaNacimiento) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isNullOrFutureDate'");
    }
}