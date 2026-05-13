package auth;

import android.app.Application;
import android.util.Patterns;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repo;
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repo = new AuthRepository(application.getApplicationContext());
    }

    public MutableLiveData<AuthState> getAuthState() { return authState; }

    public void register(String nombre, String apellidos, String correo, String usuario, String password, String passwordConfirmar) {
        if (nombre.isEmpty() || apellidos.isEmpty() || correo.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            authState.setValue(AuthState.error("Todos los campos son obligatorios"));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            authState.setValue(AuthState.error("Correo inválido"));
            return;
        }
        if (password.length() < 6) {
            authState.setValue(AuthState.error("Mínimo 6 caracteres en la contraseña"));
            return;
        }
        if (!password.equals(passwordConfirmar)) {
            authState.setValue(AuthState.error("Las contraseñas no coinciden"));
            return;
        }

        authState.setValue(AuthState.loading());
        repo.register(nombre, apellidos, correo, usuario, password, new AuthRepository.AuthCallback() {
            @Override public void onSuccess() { authState.postValue(AuthState.success()); }
            @Override public void onError(String message) { authState.postValue(AuthState.error(message)); }
        });
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            authState.setValue(AuthState.error("El correo y la contraseña son obligatorios"));
            return;
        }

        authState.setValue(AuthState.loading());

        repo.login(email.trim(), password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                authState.postValue(AuthState.success());
            }
            @Override
            public void onError(String message) {
                authState.postValue(AuthState.error(message));
            }
        });
    }

    // Añadir en AuthViewModel.java
    // En AuthViewModel.java
    public void loginWithGoogle(String idToken, String nombre, String email) {
        authState.setValue(AuthState.loading());
        repo.loginWithGoogle(idToken, nombre, email, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() { authState.postValue(AuthState.success()); }
            @Override
            public void onError(String message) { authState.postValue(AuthState.error(message)); }
        });
    }
}