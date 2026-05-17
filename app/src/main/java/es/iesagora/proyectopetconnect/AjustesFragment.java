package es.iesagora.proyectopetconnect;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import api.Resource;
import auth.AuthActivity;
import es.iesagora.proyectopetconnect.databinding.FragmentAjustesBinding;
import viewmodel.UserViewModel;
import androidx.appcompat.app.AppCompatDelegate;

public class AjustesFragment extends Fragment {

    private FragmentAjustesBinding binding;
    private UserViewModel userViewModel;
    private sharedpreferences.PreferencesRepository prefs;

    private ActivityResultLauncher<String> selectorImagenLauncher;
    private Uri nuevaFotoUri = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectorImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        nuevaFotoUri = uri;
                        binding.ivFotoPerfil.setImageURI(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAjustesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ ESTE ES EL CAMBIO QUE ARREGLA TODO EL PARPADEO
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        prefs = new sharedpreferences.PreferencesRepository(requireContext());
        String userId = prefs.getUserId();

        // Construimos la URL estática limpia sin timestamps destructores de caché
        String urlFoto = "https://evrsywohqxoehdnbhpkg.supabase.co/storage/v1/object/public/avatares/" + userId + ".jpg";

        com.bumptech.glide.Glide.with(this)
                .load(urlFoto)
                .circleCrop()
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL) // 🚀 Activamos la caché nativa
                .into(binding.ivFotoPerfil);

        // Como el Repositorio guarda el perfil en RAM, este observador responde al instante (0ms)
        userViewModel.getPerfilUsuario().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                if (resource.data.has("usuario") && !resource.data.get("usuario").isJsonNull()) {
                    binding.etNombreUsuario.setText(resource.data.get("usuario").getAsString());
                }
            }
        });

        boolean modoOscuroActivado = userViewModel.isModoOscuro();
        binding.switchModoOscuro.setChecked(modoOscuroActivado);

        binding.switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userViewModel.setModoOscuro(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.btnCambiarFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));
        binding.btnGuardarPerfil.setOnClickListener(v -> guardarCambios());
        binding.btnLogout.setOnClickListener(v -> hacerLogout());
    }

    private void guardarCambios() {
        String nuevoNombre = binding.etNombreUsuario.getText().toString().trim();

        if (nuevoNombre.isEmpty()) {
            Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnGuardarPerfil.setEnabled(false);
        binding.btnGuardarPerfil.setText("Guardando...");

        File archivoFoto = (nuevaFotoUri != null) ? uriToFile(nuevaFotoUri) : null;

        if (archivoFoto != null) {
            userViewModel.subirFotoPerfil(archivoFoto).observe(getViewLifecycleOwner(), resourceUrl -> {
                switch (resourceUrl.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        String urlPublica = resourceUrl.data;
                        actualizarDatos(nuevoNombre, urlPublica);
                        nuevaFotoUri = null;
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), "Error subiendo la foto", Toast.LENGTH_SHORT).show();
                        restaurarBoton();
                        break;
                }
            });
        } else {
            actualizarDatos(nuevoNombre, null);
        }
    }

    private void actualizarDatos(String nombre, String urlFoto) {
        userViewModel.actualizarPerfilDB(nombre, urlFoto).observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == api.Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                restaurarBoton();
            } else if (resource.status == api.Resource.Status.ERROR) {
                Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                restaurarBoton();
            }
        });
    }

    private void hacerLogout() {
        userViewModel.hacerLogout();
        prefs.cerrarSesion();
        android.content.Intent intent = new android.content.Intent(requireContext(), AuthActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("avatar_tmp", ".jpg", requireContext().getCacheDir());
            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) { out.write(buffer, 0, read); }
            out.close();
            in.close();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    private void restaurarBoton() {
        binding.btnGuardarPerfil.setEnabled(true);
        binding.btnGuardarPerfil.setText("Guardar Cambios");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}