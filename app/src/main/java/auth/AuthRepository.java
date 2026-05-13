package auth;

import android.content.Context;
import com.google.gson.JsonObject;
import api.RetrofitClient;
import data.AuthResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sharedpreferences.PreferencesRepository;

public class AuthRepository {

    private final PreferencesRepository prefsRepo;

    public AuthRepository(Context context) {
        prefsRepo = new PreferencesRepository(context);
    }

    public interface AuthCallback {
        void onSuccess();
        void onError(String message);
    }

    public void login(String email, String password, AuthCallback callback) {
        JsonObject authData = new JsonObject();
        authData.addProperty("email", email);
        authData.addProperty("password", password);

        RetrofitClient.getApi().signIn(authData).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse datosAuth = response.body();
                    prefsRepo.guardarSesion(datosAuth.getAccessToken(), datosAuth.getUser().getId());
                    callback.onSuccess();
                } else {
                    callback.onError("Credenciales incorrectas");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Error de red: " + t.getMessage());
            }
        });
    }

    public void register(String nombre, String apellidos, String correo, String usuario, String password, AuthCallback callback) {
        JsonObject authData = new JsonObject();
        authData.addProperty("email", correo);
        authData.addProperty("password", password);

        RetrofitClient.getApi().signUp(authData).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse datosAuth = response.body();
                    String userId = datosAuth.getUser().getId();
                    String token = datosAuth.getAccessToken();

                    prefsRepo.guardarSesion(token, userId);

                    guardarDatosPerfil(userId, token, nombre, apellidos, usuario, correo, callback);
                } else {
                    callback.onError("Error en registro. ¿El correo ya existe?");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    // Añadir en AuthRepository.java
    public void loginWithGoogle(String idToken, AuthCallback callback) {
        JsonObject authData = new JsonObject();
        authData.addProperty("provider", "google");
        authData.addProperty("id_token", idToken);

        RetrofitClient.getApi().loginWithGoogleToken(authData).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse datosAuth = response.body();
                    // Guardamos la sesión igual que en el login normal
                    prefsRepo.guardarSesion(datosAuth.getAccessToken(), datosAuth.getUser().getId());
                    callback.onSuccess();
                } else {
                    callback.onError("Error de autenticación con Google");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Error de red: " + t.getMessage());
            }
        });
    }

    private void guardarDatosPerfil(String userId, String token, String nombre, String apellidos, String usuario, String correo, AuthCallback callback) {
        JsonObject userDbData = new JsonObject();
        userDbData.addProperty("id", userId);
        userDbData.addProperty("nombre", nombre);
        userDbData.addProperty("apellidos", apellidos);
        userDbData.addProperty("usuario", usuario);
        userDbData.addProperty("correo", correo);

        String authHeader = "Bearer " + token;

        RetrofitClient.getApi().crearUsuarioDB(authHeader, userDbData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Usuario creado, pero falló al guardar datos del perfil.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Error de red al guardar perfil");
            }
        });
    }

    public boolean isUserLoggedIn() {
        return !prefsRepo.getToken().isEmpty();
    }
}