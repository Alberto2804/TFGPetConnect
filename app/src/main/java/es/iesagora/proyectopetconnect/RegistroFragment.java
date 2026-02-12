package es.iesagora.proyectopetconnect;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import es.iesagora.proyectopetconnect.databinding.FragmentRegistroBinding;

public class RegistroFragment extends Fragment {

    private FragmentRegistroBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRegistroBinding.inflate(inflater, container, false);

        binding.button3.setOnClickListener(v -> {

            String nombre = binding.textInputLayoutNombre.getEditText().getText().toString().trim();
            String apellidos = binding.textInputLayoutApellidos.getEditText().getText().toString().trim();
            String correo = binding.textInputLayoutCorreo.getEditText().getText().toString().trim();
            String usuario = binding.textInputLayoutUsuario.getEditText().getText().toString().trim();
            String password = binding.textInputLayoutPassword.getEditText().getText().toString().trim();
            String password2 = binding.textInputLayoutPasswordConfirmar.getEditText().getText().toString().trim();

            // Validaciones dentro del OnClick
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

            if (password2.length() < 8) {
                binding.textInputLayoutPassword.setError("Mínimo 8 caracteres");
                return;
            }

            if (!password.equals(password2)) {
                binding.textInputLayoutPasswordConfirmar.setError("Las contraseñas no coinciden");
                return;
            }

            UsuarioDB.agregarUsuario(usuario, password);


            Toast.makeText(getContext(), "Registro completado", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigate(R.id.action_registroFragment_to_inicioFragment);

        });

        return binding.getRoot();
    }
}
