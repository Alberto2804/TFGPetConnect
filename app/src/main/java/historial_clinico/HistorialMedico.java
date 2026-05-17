package historial_clinico;

public class HistorialMedico {
    private Long id;
    private String mascota_id;
    private String tipo;
    private String fecha;
    private String descripcion;

    // Constructor vacío necesario para Retrofit/Gson
    public HistorialMedico() {}

    public HistorialMedico(String mascota_id, String tipo, String fecha, String descripcion) {
        this.mascota_id = mascota_id;
        this.tipo = tipo;
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public String getMascota_id() { return mascota_id; }
    public String getTipo() { return tipo; }
    public String getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
}
