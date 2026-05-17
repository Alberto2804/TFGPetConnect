package es.iesagora.proyectopetconnect;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import auth.AuthActivity;
import es.iesagora.proyectopetconnect.databinding.ActivityMainBinding;
import sharedpreferences.PreferencesRepository;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Instanciamos el repositorio de preferencias locales
        PreferencesRepository prefs = new PreferencesRepository(this);

        // 🚀 EL PORTERO: Comprobamos si el usuario no está logeado (no hay token)
        // Hacemos la comprobación antes de inflar vistas para evitar el parpadeo visual
        if (prefs.getToken() == null || prefs.getToken().trim().isEmpty()) {
            Intent intent = new Intent(this, AuthActivity.class);
            // Estas flags limpian la pila de pantallas para que no pueda volver atrás
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Destruimos la MainActivity inmediatamente
            return;   // Cortamos el flujo de ejecución por completo
        }

        // =====================================================================
        // Si hay token, la app continúa con su ejecución normal de forma segura
        // =====================================================================

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "agenda_channel", "Avisos de Agenda",
                    android.app.NotificationManager.IMPORTANCE_HIGH);
            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // Configuración del Modo Oscuro
        if (prefs.isModoOscuro()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar la Toolbar superior
        setSupportActionBar(binding.toolbar);

        // Obtener el Controlador de Navegación
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        appBarConfiguration = new AppBarConfiguration.Builder(binding.bottomNavView.getMenu()).build();

        // Vinculamos la barra superior (Toolbar) para que el título cambie automáticamente
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);
    }

    // Gestiona la flecha de ir hacia atrás en pantallas que no son las principales
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}