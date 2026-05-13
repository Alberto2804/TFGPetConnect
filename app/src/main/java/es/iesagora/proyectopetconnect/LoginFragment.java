package es.iesagora.proyectopetconnect;

import android.app.Activity;
import android.content.Intent;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import auth.AuthViewModel;
import es.iesagora.proyectopetconnect.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;

    // Variables de Google Sign-In
    private GoogleSignInClient googleClient;
    private ActivityResultLauncher<Intent> googleLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 1. Inicializamos el receptor del resultado de Google
        inicializarLauncherGoogleSignIn();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. Configuramos el cliente de Google
        configurarGoogleSignIn();

        setupObservers();
        setupClickListeners();
    }

    private void configurarGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Coge el ID que pusimos en strings.xml
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
                            // Hemos iniciado sesión en Google correctamente en el móvil
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            String idToken = account.getIdToken();

                            // Ahora mandamos ese Token a Supabase
                            authViewModel.loginWithGoogle(idToken);
                        } catch (ApiException e) {
                            Toast.makeText(getContext(), "Error Google: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }

    private void setupObservers() {
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), authState -> {
            switch (authState.status) {
                case LOADING:
                    binding.loginButton.setEnabled(false);
                    binding.googleSignInButton.setEnabled(false);
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.loginButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "¡Login exitoso!", Toast.LENGTH_SHORT).show();

                    // ¡NUEVO!: Viajamos a la MainActivity usando un Intent
                    android.content.Intent intent = new android.content.Intent(requireContext(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish(); // Cerramos el Login para que no pueda volver atrás
                    break;
                case ERROR:
                    binding.loginButton.setEnabled(true);
                    binding.googleSignInButton.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), authState.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setupClickListeners() {
        // Clic en Iniciar Sesión normal
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText() != null ? binding.emailEditText.getText().toString().trim() : "";
            String password = binding.passwordEditText.getText() != null ? binding.passwordEditText.getText().toString().trim() : "";
            authViewModel.login(email, password);
        });

        // Clic en Google
        binding.googleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = googleClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });

        // Ir a Registro
        binding.registerTextView.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.registroFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}