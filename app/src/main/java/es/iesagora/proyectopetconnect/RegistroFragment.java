package es.iesagora.proyectopetconnect;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.gson.JsonObject;

import api.RetrofitClient;
import data.AuthResponse;
import es.iesagora.proyectopetconnect.databinding.FragmentRegistroBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroFragment extends Fragment {

    private FragmentRegistroBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRegistroBinding.inflate(inflater, container, false);

        limpiarErrorAlEscribir(binding.textInputLayoutNombre);
        limpiarErrorAlEscribir(binding.textInputLayoutApellidos);
        limpiarErrorAlEscribir(binding.textInputLayoutCorreo);
        limpiarErrorAlEscribir(binding.textInputLayoutUsuario);
        limpiarErrorAlEscribir(binding.textInputLayoutPassword);
        limpiarErrorAlEscribir(binding.textInputLayoutPasswordConfirmar);

        binding.button3.setOnClickListener(v -> {


            String nombre = binding.textInputLayoutNombre.getEditText().getText().toString().trim();
            String apellidos = binding.textInputLayoutApellidos.getEditText().getText().toString().trim();
            String correo = binding.textInputLayoutCorreo.getEditText().getText().toString().trim();
            String usuario = binding.textInputLayoutUsuario.getEditText().getText().toString().trim();
            String password = binding.textInputLayoutPassword.getEditText().getText().toString().trim();
            String password2 = binding.textInputLayoutPasswordConfirmar.getEditText().getText().toString().trim();

            if (nombre.isEmpty()) {
                binding.textInputLayoutNombre.setError("Ingrese su nombre");
                return;
            }
            if (apellidos.isEmpty()) {
                binding.textInputLayoutApellidos.setError("Ingrese sus apellidos");
                return;
            }
            if (correo.isEmpty()) {
                binding.textInputLayoutCorreo.setError("Ingrese un correo");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                binding.textInputLayoutCorreo.setError("Correo inválido");
                return;
            }
            if (usuario.isEmpty()) {
                binding.textInputLayoutUsuario.setError("Ingrese un usuario");
                return;
            }
            if (password.isEmpty()) {
                binding.textInputLayoutPassword.setError("Ingrese una contraseña");
                return;
            }
            if (password.length() < 6) {
                binding.textInputLayoutPassword.setError("Mínimo 6 caracteres");
                return;
            }
            if (!password.equals(password2)) {
                binding.textInputLayoutPasswordConfirmar.setError("Las contraseñas no coinciden");
                return;
            }

            JsonObject authData = new JsonObject();
            authData.addProperty("email", correo);
            authData.addProperty("password", password);


            binding.button3.setEnabled(false);

            RetrofitClient.getApi().signUp(authData).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {


                        AuthResponse datosAuth = response.body();
                        String userId = datosAuth.getUser().getId();       // Sacamos el ID
                        String accessToken = datosAuth.getAccessToken();   // Sacamos el Token


                        guardarDatosEnBaseDeDatos(userId, accessToken, nombre, apellidos, usuario, correo, v);

                    } else {
                        binding.button3.setEnabled(true);
                        Toast.makeText(getContext(), "Error en registro. ¿El correo ya existe?", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    binding.button3.setEnabled(true);
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return binding.getRoot();
    }

    private void guardarDatosEnBaseDeDatos(String userId, String token, String nombre, String apellidos, String usuario, String correo, View view) {

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
                binding.button3.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_registroFragment_to_appFragment);
                } else {
                    Toast.makeText(getContext(), "Usuario creado, pero falló al guardar datos del perfil.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                binding.button3.setEnabled(true);
                Toast.makeText(getContext(), "Error de red al guardar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limpiarErrorAlEscribir(com.google.android.material.textfield.TextInputLayout layout) {
        if (layout.getEditText() != null) {
            layout.getEditText().addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (layout.getError() != null) {
                        layout.setError(null);
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) { }
            });
        }
    }
}