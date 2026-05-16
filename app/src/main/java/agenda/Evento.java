package agenda;

public class Evento {
    public String id;
    public String titulo;
    public String fecha;
    public String hora;
    public String userId;

    public Evento(String titulo, String fecha, String hora, String userId) {
        this.titulo = titulo;
        this.fecha = fecha;
        this.hora = hora;
        this.userId = userId;
    }
}