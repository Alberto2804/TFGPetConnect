package es.iesagora.proyectopetconnect;

import android.os.Bundle;
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
import es.iesagora.proyectopetconnect.databinding.FragmentLoginBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);

        binding.button4.setOnClickListener(v -> {

            String email = binding.textInputLayoutUsuarioLogin.getEditText().getText().toString().trim();
            String password = binding.textInputLayoutPasswordLogin.getEditText().getText().toString().trim();

            if (email.isEmpty()) {
                binding.textInputLayoutUsuarioLogin.setError("Ingrese su correo");
                return;
            }

            if (password.isEmpty()) {
                binding.textInputLayoutPasswordLogin.setError("Ingrese su contraseña");
                return;
            }

            JsonObject loginData = new JsonObject();
            loginData.addProperty("email", email);
            loginData.addProperty("password", password);

            binding.button4.setEnabled(false);

            RetrofitClient.getApi().signIn(loginData).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    binding.button4.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(getContext(), "¡Bienvenido!", Toast.LENGTH_SHORT).show();


                        Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_appFragment);
                    } else {
                        Toast.makeText(getContext(), "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    binding.button4.setEnabled(true);
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });

        });

        return binding.getRoot();
    }
}