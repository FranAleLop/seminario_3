package taichi.controller;

import taichi.model.Usuario;
import taichi.dao.UsuarioDAO;
import taichi.util.InputValidator;
import taichi.util.PasswordHasher;

import java.awt.GraphicsConfiguration;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioController {

    private UsuarioDAO usuarioDAO;
    private static final Logger LOGGER = Logger.getLogger(UsuarioController.class.getName());

    public UsuarioController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * La contraseña se hasheará antes de ser almacenada.
     * @param nombreUsuario Nombre de usuario único.
     * @param contrasena Contraseña del usuario (se hasheará).
     * @param rol Rol del usuario (ej. "Administrador", "Secretaria", "Profesor").
     * @param activo Estado activo del usuario.
     * @return El objeto Usuario recién creado y con su ID asignado.
     * @throws Exception Si ocurre un error lógico (validación, usuario ya existe) o de base de datos.
     */
    public Usuario registrarNuevoUsuario(String nombreUsuario, String contrasena, boolean activo) throws Exception {
        // --- 1. Validaciones de entrada ---
        if (InputValidator.isNullOrEmpty(nombreUsuario) || InputValidator.isNullOrEmpty(contrasena)) {
            throw new IllegalArgumentException("Nombre de usuario, contraseña y rol son campos obligatorios.");
        }
        // Validar formato de nombre de usuario si es necesario (ej. longitud mínima)
        if (contrasena.length() < 8) { // Ejemplo de política de contraseña
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        // Validar roles permitidos si es una lista cerrada
        // if (!List.of("Administrador", "Secretaria", "Profesor").contains(rol)) {
        //     throw new IllegalArgumentException("El rol especificado no es válido.");
        // }

        // --- 2. Verificar si el nombre de usuario ya existe ---
        try {
            if (usuarioDAO.obtenerPorNombreUsuario(nombreUsuario) != null) {
                throw new Exception("El nombre de usuario '" + nombreUsuario + "' ya está en uso. Por favor, elija otro.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar existencia de usuario: " + e.getMessage(), e);
            throw new Exception("Error al procesar registro de usuario: " + e.getMessage());
        }

        // --- 3. Hashear la contraseña ---
        int hashedPassword = PasswordHasher.hashPassword(contrasena); // Usaremos un algoritmo seguro como BCrypt

        // --- 4. Crear el objeto Usuario ---
        Usuario nuevoUsuario = new Usuario(0, nombreUsuario, hashedPassword, activo);

        // --- 5. Llamar al DAO para insertar en la BD ---
        try {
            int idGenerado = usuarioDAO.insertar(nuevoUsuario);
            if (idGenerado != -1) {
                nuevoUsuario.setIdUsuario(idGenerado);
                System.out.println("Usuario registrado con éxito: " + nombreUsuario);
                return nuevoUsuario;
            } else {
                throw new Exception("No se pudo insertar el usuario en la base de datos.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar nuevo usuario en la BD: " + e.getMessage(), e);
            throw new Exception("Error al registrar usuario: " + e.getMessage());
        }
    }

    /**
     * Autentica a un usuario. Verifica el nombre de usuario y la contraseña.
     * @param nombreUsuario Nombre de usuario.
     * @param contrasena Contraseña proporcionada por el usuario.
     * @return El objeto Usuario si la autenticación es exitosa, o null si falla.
     * @throws Exception Si ocurre un error de base de datos o lógica de autenticación.
     */
    public Usuario autenticarUsuario(String nombreUsuario, String contrasena) throws Exception {
        if (InputValidator.isNullOrEmpty(nombreUsuario) || InputValidator.isNullOrEmpty(contrasena)) {
            throw new IllegalArgumentException("Nombre de usuario y contraseña son obligatorios para la autenticación.");
        }

        try {
            Usuario usuario = usuarioDAO.obtenerPorNombreUsuario(nombreUsuario);

            if (usuario == null) {
                // No se encontró el usuario. Por seguridad, no debemos indicar si el usuario
                // no existe o si la contraseña es incorrecta para evitar la enumeración de usuarios.
                throw new Exception("Credenciales incorrectas."); 
            }

            if (!usuario.isActivo()) {
                throw new Exception("El usuario está inactivo. Contacte al administrador.");
            }

            // Verificar la contraseña hasheada
            if (PasswordHasher(contrasena, usuario.getContrasena())) {
                System.out.println("Autenticación exitosa para el usuario: " + nombreUsuario);
                return usuario;
            } else {
                throw new Exception("Credenciales incorrectas.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error de base de datos durante la autenticación de usuario " + nombreUsuario + ": " + e.getMessage(), e);
            throw new Exception("Error interno durante la autenticación.");
        }
    }

    private boolean PasswordHasher(String contrasena, String contrasena2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'PasswordHasher'");
    }

    /**
     * Obtiene la información de un usuario por su ID.
     * @param idUsuario ID del usuario a buscar.
     * @return El objeto Usuario si se encuentra, o null.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public Usuario obtenerUsuarioPorId(int idUsuario) throws Exception {
        if (!InputValidator.isPositive(idUsuario)) {
            throw new IllegalArgumentException("El ID del usuario debe ser un número positivo.");
        }
        try {
            return usuarioDAO.obtenerPorId(idUsuario);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener usuario por ID " + idUsuario + " de la BD: " + e.getMessage(), e);
            throw new Exception("Error al consultar usuario por ID: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de todos los usuarios.
     * @return Lista de objetos Usuario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public List<Usuario> obtenerTodosLosUsuarios() throws Exception {
        try {
            return usuarioDAO.obtenerTodos();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener todos los usuarios de la BD: " + e.getMessage(), e);
            throw new Exception("Error al obtener listado de usuarios: " + e.getMessage());
        }
    }

    /**
     * Actualiza la información de un usuario existente.
     * Si la contraseña se proporciona, se hashea. Si no, se mantiene la existente.
     * @param usuario El objeto Usuario con la información actualizada.
     * @param nuevaContrasena La nueva contraseña (puede ser null o vacío si no se cambia).
     * @return true si la actualización fue exitosa, false de lo contrario.
     * @throws Exception Si ocurre un error lógico o de base de datos.
     */
    public boolean actualizarInformacionUsuario(Usuario usuario, String nuevaContrasena) throws Exception {
        // --- 1. Validaciones de entrada ---
        if (usuario == null) {
            throw new IllegalArgumentException("El objeto Usuario no puede ser nulo.");
        }
        if (!InputValidator.isPositive(usuario.getIdUsuario())) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio para la actualización.");
        }
        if (InputValidator.isNullOrEmpty(usuario.getNombreUsuario()) || InputValidator.isNullOrEmpty(usuario.getRol())) {
            throw new IllegalArgumentException("Nombre de usuario y rol son campos obligatorios.");
        }

        // Verificar si el nombre de usuario ya existe para otro ID
        try {
            Usuario usuarioExistente = usuarioDAO.obtenerPorNombreUsuario(usuario.getNombreUsuario());
            if (usuarioExistente != null && usuarioExistente.getIdUsuario() != usuario.getIdUsuario()) {
                throw new Exception("El nombre de usuario '" + usuario.getNombreUsuario() + "' ya está en uso por otro usuario.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar existencia de usuario para actualización: " + e.getMessage(), e);
            throw new Exception("Error al procesar actualización de usuario: " + e.getMessage());
        }


        // --- 3. Llamar al DAO para actualizar en la BD ---
        try {
            return usuarioDAO.actualizar(usuario);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar usuario con ID " + usuario.getIdUsuario() + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al actualizar usuario: " + e.getMessage());
        }
    }

    /**
     * Desactiva un usuario por su ID.
     * @param idUsuario ID del usuario a desactivar.
     * @return true si el usuario fue desactivado exitosamente, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean desactivarUsuario(int idUsuario) throws Exception {
        if (!InputValidator.isPositive(idUsuario)) {
            throw new IllegalArgumentException("El ID del usuario debe ser un número positivo.");
        }
        try {
            Usuario usuario = usuarioDAO.obtenerPorId(idUsuario);
            if (usuario == null) {
                throw new Exception("Usuario no encontrado con ID: " + idUsuario);
            }
            if (!usuario.isActivo()) {
                System.out.println("El usuario con ID " + idUsuario + " ya está inactivo.");
                return true; 
            }
            usuario.setActivo(false); 
            return usuarioDAO.actualizar(usuario);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al desactivar al usuario con ID " + idUsuario + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al desactivar usuario: " + e.getMessage());
        }
    }

    /**
     * Activa un usuario por su ID.
     * @param idUsuario ID del usuario a activar.
     * @return true si el usuario fue activado exitosamente, false de lo contrario.
     * @throws Exception Si ocurre un error de base de datos.
     */
    public boolean activarUsuario(int idUsuario) throws Exception {
        if (!InputValidator.isPositive(idUsuario)) {
            throw new IllegalArgumentException("El ID del usuario debe ser un número positivo.");
        }
        try {
            Usuario usuario = usuarioDAO.obtenerPorId(idUsuario);
            if (usuario == null) {
                throw new Exception("Usuario no encontrado con ID: " + idUsuario);
            }
            if (usuario.isActivo()) {
                System.out.println("El usuario con ID " + idUsuario + " ya está activo.");
                return true; 
            }
            usuario.setActivo(true); 
            return usuarioDAO.actualizar(usuario);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al activar al usuario con ID " + idUsuario + " en la BD: " + e.getMessage(), e);
            throw new Exception("Error al activar usuario: " + e.getMessage());
        }
    }

	public Object getDocumentoEstadoController() {
		
		throw new UnsupportedOperationException("Unimplemented method 'getDocumentoEstadoController'");
	}

    public Object getPagoController() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPagoController'");
    }

    public Object getPeriodoCuotaController() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPeriodoCuotaController'");
    }

    public String getAlumnoController() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getAlumnoController'");
    }

    public GraphicsConfiguration getProfesorController() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getProfesorController'");
    }

    public Object getClaseController() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getClaseController'");
    }
}