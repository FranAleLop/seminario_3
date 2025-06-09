package taichi.model;

public class Usuario {

    // Atributos (variables de instancia)
    // Corresponden a las columnas de la tabla 'Usuarios'
    private int idUsuario;               // id_usuario (clave primaria)
    private String nombreUsuario;        // nombre_usuario
    private String contrasena;           // contrasena (idealmente, almacenar hash de la contraseña)
    private String rol;                  // rol (ej. "Administrador", "Recepcionista")
    private boolean activo;              // activo (para habilitar/deshabilitar el usuario)

    // --- Constructores ---

    // Constructor vacío
    public Usuario() {
    }

    // Constructor sin idUsuario (para crear un NUEVO usuario antes de insertarlo en la BD)
    public Usuario(String nombreUsuario, String contrasena, String rol, boolean activo) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena; // Considerar hashing para seguridad en un sistema real
        this.rol = rol;
        this.activo = activo;
    }

    // Constructor con idUsuario (para cuando recuperas un usuario de la BD)
    public Usuario(int idUsuario, String nombreUsuario, String contrasena, String rol, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena; // Considerar hashing para seguridad en un sistema real
        this.rol = rol;
        this.activo = activo;
    }

    // --- Métodos Getters y Setters (Encapsulamiento) ---

    public Usuario(int idUsuario2, String text, Object object, boolean selected) {
        
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena; // En un sistema real, aquí iría el hashing
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Usuario{" +
               "idUsuario=" + idUsuario +
               ", nombreUsuario='" + nombreUsuario + '\'' +
               ", rol='" + rol + '\'' +
               ", activo=" + activo +
               '}';
        // NOTA: No se incluyo contraseña en el toString() por razones de seguridad
    }
}
