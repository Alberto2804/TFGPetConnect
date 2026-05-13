package comunidad;

public class Mensaje {
    private String user_id;
    private String usuario;
    private String mensaje;
    private String created_at;

    public Mensaje() {}

    public Mensaje(String user_id, String usuario, String mensaje) {
        this.user_id = user_id;
        this.usuario = usuario;
        this.mensaje = mensaje;
    }

    public String getUser_id() { return user_id; }
    public String getUsuario() { return usuario; }
    public String getMensaje() { return mensaje; }
}
