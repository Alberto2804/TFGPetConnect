package es.iesagora.proyectopetconnect;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import es.iesagora.proyectopetconnect.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);

        binding.button4.setOnClickListener(v -> {

            String usuario = binding.textInputLayoutUsuarioLogin.getEditText().getText().toString().trim();
            String password = binding.textInputLayoutPasswordLogin.getEditText().getText().toString().trim();


            if (usuario.isEmpty()) {
                binding.textInputLayoutUsuarioLogin.setError("Ingrese un usuario");
                return;
            }

            if (password.isEmpty()) {
                binding.textInputLayoutPasswordLogin.setError("Ingrese la contraseña");
                return;
            }

            if (!UsuarioDB.validarUsuario(usuario, password)) {
                Toast.makeText(getContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getContext(), "Inicio de sesión correcto", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_appFragment);

        });

        return binding.getRoot();
    }
}
