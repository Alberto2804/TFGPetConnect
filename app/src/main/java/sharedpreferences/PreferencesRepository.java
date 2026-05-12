package sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesRepository {
    private static final String PREF_NAME = "PreferenciasPetConnect";
    private static final String KEY_TOKEN = "supabase_token";
    private static final String KEY_USER_ID = "supabase_userId";

    private static final String KEY_DARK_MODE = "tema_oscuro";

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

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }
}