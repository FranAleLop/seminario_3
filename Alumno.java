package taichi.model;

import java.time.LocalDate; // Necesario para manejar fechas

public class Alumno {

    // Atributos (variables de instancia)
    // Corresponden a las columnas de la tabla 'Alumnos'
    private int idAlumno;                 // id_alumno (clave primaria)
    private String nombreCompleto;        // nombre_completo
    private String dni;                   // dni
    private LocalDate fechaNacimiento;    // fecha_nacimiento
    private String direccion;             // direccion
    private String telefono;              // telefono
    private String email;                 // email
    private LocalDate fechaInscripcion;   // fecha_inscripcion
    private boolean activo;               // activo (para saber si el alumno está activo o dado de baja)

    // --- Constructores ---

    // Constructor vacío 
    public Alumno() {
    }

    // Constructor con todos los parámetros (excepto idAlumno, si es autoincremental en la BD)
    // Este constructor se usaría al crear un NUEVO alumno antes de insertarlo en la BD
    public Alumno(String nombreCompleto, String dni, LocalDate fechaNacimiento,
                  String direccion, String telefono, String email,
                  LocalDate fechaInscripcion, boolean activo) {
        this.nombreCompleto = nombreCompleto;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.fechaInscripcion = fechaInscripcion;
        this.activo = activo;
    }

    // Constructor con idAlumno (útil cuando recuperas un alumno de la BD)
    public Alumno(int idAlumno, String nombreCompleto, String dni, LocalDate fechaNacimiento,
                  String direccion, String telefono, String email,
                  LocalDate fechaInscripcion, boolean activo) {
        this.idAlumno = idAlumno;
        this.nombreCompleto = nombreCompleto;
        this.dni = dni;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.fechaInscripcion = fechaInscripcion;
        this.activo = activo;
    }

    // --- Métodos Getters y Setters (Encapsulamiento) ---

    public int getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(int idAlumno) {
        this.idAlumno = idAlumno;
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

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public boolean isActivo() { // Para booleanos, el getter suele ser 'isNombre'
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Alumno{" +
               "idAlumno=" + idAlumno +
               ", nombreCompleto='" + nombreCompleto + '\'' +
               ", dni='" + dni + '\'' +
               ", fechaNacimiento=" + fechaNacimiento +
               ", direccion='" + direccion + '\'' +
               ", telefono='" + telefono + '\'' +
               ", email='" + email + '\'' +
               ", fechaInscripcion=" + fechaInscripcion +
               ", activo=" + activo +
               '}';
    }
}