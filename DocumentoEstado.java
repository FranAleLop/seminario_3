package taichi.model;

import java.time.LocalDate;

public class DocumentoEstado {

    // Atributos (variables de instancia)
    // Corresponden a las columnas de la tabla 'DocumentosEstado'
    private int idDocumentoEstado;       // id_documento_estado (clave primaria)
    private int idAlumno;               // id_alumno (clave foránea a Alumnos)
    private String tipoDocumento;        // tipo_documento (ej. "Ficha médica", "DNI")
    private String estado;               // estado (ej. "Pendiente", "Entregado", "Vencido")
    private String observaciones;        // observaciones

    // --- Constructores ---

    // Constructor vacío
    public DocumentoEstado(int idDocumentoEstado2, int i, String string, LocalDate fechaPresentacion, boolean b, String string2) {
    }

    // Constructor sin idDocumentoEstado (para crear un NUEVO registro de estado de documento)
    public DocumentoEstado(int idAlumno, String tipoDocumento, String estado, String observaciones) {
        this.idAlumno = idAlumno;
        this.tipoDocumento = tipoDocumento;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    // Constructor con idDocumentoEstado (para cuando recuperas un registro de la BD)
    public DocumentoEstado(int idDocumentoEstado, int idAlumno, String tipoDocumento, String estado, String observaciones) {
        this.idDocumentoEstado = idDocumentoEstado;
        this.idAlumno = idAlumno;
        this.tipoDocumento = tipoDocumento;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    // --- Métodos Getters y Setters (Encapsulamiento) ---

    public int getIdDocumentoEstado() {
        return idDocumentoEstado;
    }

    public void setIdDocumentoEstado(int idDocumentoEstado) {
        this.idDocumentoEstado = idDocumentoEstado;
    }

    public int getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(int idAlumno) {
        this.idAlumno = idAlumno;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "DocumentoEstado{" +
               "idDocumentoEstado=" + idDocumentoEstado +
               ", idAlumno=" + idAlumno +
               ", tipoDocumento='" + tipoDocumento + '\'' +
               ", estado='" + estado + '\'' +
               ", observaciones='" + observaciones + '\'' +
               '}';
    }
    public Object getFechaPresentacion() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFechaPresentacion'");
    }
    public boolean isEntregado() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isEntregado'");
    }
    public Object getNotas() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getNotas'");
    }
}