package es.iesagora.proyectopetconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import auth.AuthState;
import auth.AuthViewModel;
import es.iesagora.proyectopetconnect.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupClickListeners();
    }

    private void setupObservers() {
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            switch (authState.status) {
                case LOADING:
                    binding.button4.setEnabled(false);
                    binding.button4.setText("Iniciando sesión...");
                    break;
                case SUCCESS:
                    binding.button4.setEnabled(true);
                    Toast.makeText(getContext(), "¡Login exitoso!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_appFragment);
                    break;
                case ERROR:
                    binding.button4.setEnabled(true);
                    binding.button4.setText("Iniciar Sesion");
                    Toast.makeText(getContext(), authState.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setupClickListeners() {
        binding.button4.setOnClickListener(v -> {
            String email = "";
            if(binding.textInputLayoutUsuarioLogin.getEditText() != null) {
                email = binding.textInputLayoutUsuarioLogin.getEditText().getText().toString().trim();
            }

            String password = "";
            if(binding.textInputLayoutPasswordLogin.getEditText() != null) {
                password = binding.textInputLayoutPasswordLogin.getEditText().getText().toString().trim();
            }

            authViewModel.login(email, password);
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}