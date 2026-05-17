package sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesRepository {
    private static final String PREF_NAME = "PreferenciasPetConnect";
    private static final String KEY_TOKEN = "supabase_token";
    private static final String KEY_USER_ID = "supabase_userId";

    private static final String KEY_DARK_MODE = "tema_oscuro";

    private static final String KEY_MASCOTA_ACTIVA = "mascota_activa_id";

    private final SharedPreferences prefs;

    public PreferencesRepository(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void guardarSesion(String token, String userId) {
        prefs.edit().putString(KEY_TOKEN, token).putString(KEY_USER_ID, userId).apply();
    }

    public void guardarModoOscuro(boolean isOscuro) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isOscuro).apply();
    }

    public boolean isModoOscuro() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public void guardarMascotaActivaId(String mascotaId) {
        prefs.edit().putString(KEY_MASCOTA_ACTIVA, mascotaId).apply();
    }

    public String getMascotaActivaId() {
        return prefs.getString(KEY_MASCOTA_ACTIVA, null); // Devuelve null si no hay ninguna
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }

    public void guardarDatosPerfil(String nombre, String fotoUrl) {
        prefs.edit().putString("user_name", nombre).apply();
        prefs.edit().putString("user_photo", fotoUrl).apply();
    }


    public String getNombrePerfil() { return prefs.getString("user_name", "Usuario"); }
    public String getFotoPerfil() { return prefs.getString("user_photo", ""); }
}