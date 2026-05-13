package es.iesagora.proyectopetconnect;

import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;

import api.RetrofitClient;
import data.AuthResponse;
import es.iesagora.proyectopetconnect.databinding.FragmentRegistroBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroFragment extends Fragment {

    private sharedpreferences.PreferencesRepository preferencesRepository;
    private FragmentRegistroBinding binding;
    private Uri fotoSeleccionadaUri = null;

    private final ActivityResultLauncher<String> selectorImagenLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    fotoSeleccionadaUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(binding.ivFotoPerfil);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegistroBinding.inflate(inflater, container, false);
        preferencesRepository = new sharedpreferences.PreferencesRepository(requireContext());

        // Navegación
        binding.loginTextView.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.loginFragment));
        binding.btnSeleccionarFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));

        binding.registerButton.setOnClickListener(v -> {
            String usuario = binding.usernameEditText.getText().toString().trim();
            String correo = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String password2 = binding.confirmPasswordEditText.getText().toString().trim();

            // Validaciones básicas
            if (usuario.isEmpty()) { Toast.makeText(getContext(), "Escriba un nombre de usuario", Toast.LENGTH_SHORT).show(); return; }
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { Toast.makeText(getContext(), "Correo inválido", Toast.LENGTH_SHORT).show(); return; }
            if (password.length() < 6) { Toast.makeText(getContext(), "Contraseña muy corta", Toast.LENGTH_SHORT).show(); return; }
            if (!password.equals(password2)) { Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show(); return; }

            registrarEnAuth(usuario, correo, password);
        });

        return binding.getRoot();
    }

    private void registrarEnAuth(String usuario, String correo, String password) {
        binding.registerButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        JsonObject authData = new JsonObject();
        authData.addProperty("email", correo);
        authData.addProperty("password", password);

        RetrofitClient.getApi().signUp(authData).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String userId = response.body().getUser().getId();
                    String token = response.body().getAccessToken();

                    preferencesRepository.guardarSesion(token, userId);

                    // Guardamos solo Usuario e ID en la tabla de perfiles
                    guardarEnBaseDeDatos(userId, token, usuario, correo);
                } else {
                    restaurarUI();
                    Toast.makeText(getContext(), "Error en el registro de autenticación", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                restaurarUI();
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarEnBaseDeDatos(String userId, String token, String usuario, String correo) {
        JsonObject userData = new JsonObject();
        userData.addProperty("id", userId);
        userData.addProperty("usuario", usuario);
        userData.addProperty("correo", correo);

        RetrofitClient.getApi().crearUsuarioDB("Bearer " + token, userData).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                restaurarUI();
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Bienvenido!", Toast.LENGTH_SHORT).show();

                    // ¡NUEVO!: Viajamos a la MainActivity usando un Intent
                    android.content.Intent intent = new android.content.Intent(requireContext(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish(); // Cerramos el Registro
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                restaurarUI();
            }
        });
    }

    private void restaurarUI() {
        binding.registerButton.setEnabled(true);
        binding.progressBar.setVisibility(View.GONE);
    }
}