package auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import es.iesagora.proyectopetconnect.MainActivity;
import es.iesagora.proyectopetconnect.R;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. El portero comprueba si ya tienes sesión guardada
        sharedpreferences.PreferencesRepository prefs = new sharedpreferences.PreferencesRepository(this);
        if (prefs.getToken() != null && !prefs.getToken().isEmpty()) {
            // Tienes sesión: Te mandamos directo al MainActivity a la velocidad de la luz
            android.content.Intent intent = new android.content.Intent(this, es.iesagora.proyectopetconnect.MainActivity.class);
            startActivity(intent);
            // Matamos esta pantalla de login para que no puedas volver atrás
            finish();
            // IMPORTANTE: El return corta la ejecución para que no dibuje el login
            return;
        }

        // 2. Si NO tienes sesión, el portero te deja en la puerta y dibuja el diseño del login
        setContentView(R.layout.activity_auth);
    }
}