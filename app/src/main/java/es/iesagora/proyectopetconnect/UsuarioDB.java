package es.iesagora.proyectopetconnect;

import java.util.ArrayList;

public class UsuarioDB {

    private static ArrayList<String> usuarios = new ArrayList<>();
    private static ArrayList<String> contrasenas = new ArrayList<>();

    public static void agregarUsuario(String usuario, String password) {
        usuarios.add(usuario);
        contrasenas.add(password);
    }

    public static boolean validarUsuario(String usuario, String password) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).equals(usuario) && contrasenas.get(i).equals(password)) {
                return true;
            }
        }
        return false;
    }
}

