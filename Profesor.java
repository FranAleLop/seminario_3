package taichi.model;

import java.time.LocalDate; // Necesario para manejar fechas

public class Profesor {

    // Atributos (variables de instancia)
    // Corresponden a las columnas de la tabla 'Profesores'
    private int idProfesor;               // id_profesor (clave primaria)
    private String nombreCompleto;        // nombre_completo
    private String dni;                   // dni
    private LocalDate fechaNacimiento;    // fecha_nacimiento
    private String direccion;             // direccion
    private String telefono;              // telefono
    private String email;                 // email
    private LocalDate fechaContratacion;  // fecha_contratacion
    private boolean activo;               // activo (para saber si el profesor está activo o dado de baja)

    // --- Constructores ---

    // Constructor vacío
    public Profesor() {
    }

    // Constructor sin idProfesor (para crear un NUEVO profesor antes de insertarlo en la BD)
    public Profesor(String nombreCompleto, String dni, LocalDate fechaNacimiento,
                    String direccion, String telefono, String email,
                    LocalDate fechaContratacion, boolean activo) {
        this.nombreCompleto = nombreCompleto;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
    }

    // Constructor con idProfesor (para cuando recuperas un profesor de la BD)
    public Profesor(int idProfesor, String nombreCompleto, String dni, LocalDate fechaNacimiento,
                    String direccion, String telefono, String email,
                    LocalDate fechaContratacion, boolean activo) {
        this.idProfesor = idProfesor;
        this.nombreCompleto = nombreCompleto;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
    }

    // --- Métodos Getters y Setters (Encapsulamiento) ---

    public int getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(int idProfesor) {
        this.idProfesor = idProfesor;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public boolean isActivo() { // Para booleanos, el getter suele ser 'isNombre'
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Profesor{" +
               "idProfesor=" + idProfesor +
               ", nombreCompleto='" + nombreCompleto + '\'' +
               ", dni='" + dni + '\'' +
               ", fechaNacimiento=" + fechaNacimiento +
               ", direccion='" + direccion + '\'' +
               ", telefono='" + telefono + '\'' +
               ", email='" + email + '\'' +
               ", fechaContratacion=" + fechaContratacion +
               ", activo=" + activo +
               '}';
    }
}