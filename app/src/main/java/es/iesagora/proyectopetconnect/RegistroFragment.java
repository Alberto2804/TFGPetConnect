package es.iesagora.proyectopetconnect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import auth.AuthViewModel;
import es.iesagora.proyectopetconnect.databinding.FragmentRegistroBinding;

public class RegistroFragment extends Fragment {

    private FragmentRegistroBinding binding;
    private AuthViewModel authViewModel;


    // --- Variables para Google Sign In ---
    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googleLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 1. Inicializamos el launcher de Google
        inicializarLauncherGoogleSignIn();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegistroBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. Configuramos el cliente de Google
        configurarGoogleSignIn();

        // 3. Observamos los estados (Cargando, Éxito, Error)
        setupObservers();

        // --- CLICKS EN BOTONES ---

        // Volver al Login
        binding.loginTextView.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.loginFragment));

        // Registro Normal por Correo
        binding.registerButton.setOnClickListener(v -> {
            String usuario = binding.usernameEditText.getText() != null ? binding.usernameEditText.getText().toString().trim() : "";
            String correo = binding.emailEditText.getText() != null ? binding.emailEditText.getText().toString().trim() : "";
            String password = binding.passwordEditText.getText() != null ? binding.passwordEditText.getText().toString().trim() : "";
            String passwordConfirmar = binding.confirmPasswordEditText.getText() != null ? binding.confirmPasswordEditText.getText().toString().trim() : "";

            // NOTA: Como quitamos nombre y apellidos del XML, le pasamos "N/A" para que tu AuthViewModel no de error de validación
            authViewModel.register("N/A", "N/A", correo, usuario, password, passwordConfirmar);
        });

        // ¡EL BOTÓN DE GOOGLE AHORA SÍ FUNCIONA!
        binding.googleSignInButton.setOnClickListener(v -> {
            // 1. Forzamos a Google a "cerrar sesión" internamente
            googleClient.signOut().addOnCompleteListener(requireActivity(), task -> {
                // 2. Una vez limpia la caché, abrimos el selector de cuentas
                Intent signInIntent = googleClient.getSignInIntent();
                googleLauncher.launch(signInIntent);
            });
        });
    }

    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void inicializarLauncherGoogleSignIn() {
        googleLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            String idToken = account.getIdToken();
                            String nombre = account.getDisplayName(); // Nombre del usuario en Google
                            String email = account.getEmail();       // Correo del usuario en Google

                            // Le pasamos el token y los datos para que cree el perfil
                            authViewModel.loginWithGoogle(idToken, nombre, email);
                        } catch (ApiException e) {
                            Toast.makeText(getContext(), "Error Google", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void setupObservers() {
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            switch (authState.status) {
                case LOADING:
                    binding.registerButton.setEnabled(false);
                    binding.googleSignInButton.setEnabled(false);
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    binding.registerButton.setEnabled(true);
                    binding.googleSignInButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show();

                    // Viajamos a la App Principal y cerramos la pantalla de Auth
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                    break;

                case ERROR:
                    binding.registerButton.setEnabled(true);
                    binding.googleSignInButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), authState.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}