package taichi.model;

public class Clase {

    // Atributos (variables de instancia)
    // Corresponden a las columnas de la tabla 'Clases'
    private int idClase;                 // id_clase (clave primaria)
    private String nombreClase;          // nombre_clase
    private String descripcion;          // descripcion
    private String horario;              // horario (ej. "Lunes y Miércoles 18:00-19:30")
    private int cupoMaximo;              // cupo_maximo
    private boolean activa;              // activa (para saber si la clase está actualmente en curso)

    // --- Constructores ---

    // Constructor vacío
    public Clase() {
    }

    // Constructor sin idClase (para crear una NUEVA clase antes de insertarla en la BD)
    public Clase(String nombreClase, String descripcion, String horario, int cupoMaximo, boolean activa) {
        this.nombreClase = nombreClase;
        this.descripcion = descripcion;
        this.horario = horario;
        this.cupoMaximo = cupoMaximo;
        this.activa = activa;
    }

    // Constructor con idClase (para cuando recuperas una clase de la BD)
    public Clase(int idClase, String nombreClase, String descripcion, String horario, int cupoMaximo, boolean activa) {
        this.idClase = idClase;
        this.nombreClase = nombreClase;
        this.descripcion = descripcion;
        this.horario = horario;
        this.cupoMaximo = cupoMaximo;
        this.activa = activa;
    }

    // --- Métodos Getters y Setters (Encapsulamiento) ---

    public int getIdClase() {
        return idClase;
    }

    public void setIdClase(int idClase) {
        this.idClase = idClase;
    }

    public String getNombreClase() {
        return nombreClase;
    }

    public void setNombreClase(String nombreClase) {
        this.nombreClase = nombreClase;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public String toString() {
        return "Clase{" +
               "idClase=" + idClase +
               ", nombreClase='" + nombreClase + '\'' +
               ", descripcion='" + descripcion + '\'' +
               ", horario='" + horario + '\'' +
               ", cupoMaximo=" + cupoMaximo +
               ", activa=" + activa +
               '}';
    }
}