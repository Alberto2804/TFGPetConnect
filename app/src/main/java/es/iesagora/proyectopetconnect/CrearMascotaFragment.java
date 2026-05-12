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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import api.Resource;
import es.iesagora.proyectopetconnect.databinding.FragmentCrearMascotaBinding;
import viewmodel.UserViewModel;

public class CrearMascotaFragment extends Fragment {

    private FragmentCrearMascotaBinding binding;
    private UserViewModel userViewModel;

    private ActivityResultLauncher<String> selectorImagenLauncher;
    private Uri fotoMascotaUri = null;

    private boolean esModoEdicion = false;
    private String urlFotoActual = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectorImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        fotoMascotaUri = uri;
                        Glide.with(this).load(uri).circleCrop().into(binding.ivFotoMascota);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCrearMascotaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        if (getArguments() != null && getArguments().containsKey("nombre")) {
            esModoEdicion = true;
            binding.tvTitulo.setText("Editar Mascota");
            binding.btnGuardarMascota.setText("Guardar Cambios");

            if (binding.tilNombreMascota.getEditText() != null) {
                binding.tilNombreMascota.getEditText().setText(getArguments().getString("nombre"));
            }
            if (binding.tilRazaMascota.getEditText() != null) {
                binding.tilRazaMascota.getEditText().setText(getArguments().getString("raza"));
            }
            if (binding.tilEdadMascota.getEditText() != null) {
                binding.tilEdadMascota.getEditText().setText(getArguments().getString("edad"));
            }

            urlFotoActual = getArguments().getString("urlFoto");
            if (urlFotoActual != null && !urlFotoActual.isEmpty()) {
                Glide.with(this)
                        .load(urlFotoActual)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.ivFotoMascota);
            }
        }

        binding.ivFotoMascota.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));
        binding.btnSeleccionarFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));
        binding.btnGuardarMascota.setOnClickListener(v -> procesarGuardado());
    }

    private void procesarGuardado() {
        String nombre = "";
        if (binding.tilNombreMascota.getEditText() != null) {
            nombre = binding.tilNombreMascota.getEditText().getText().toString().trim();
        }

        String raza = "";
        if (binding.tilRazaMascota.getEditText() != null) {
            raza = binding.tilRazaMascota.getEditText().getText().toString().trim();
        }

        String edad = "";
        if (binding.tilEdadMascota.getEditText() != null) {
            edad = binding.tilEdadMascota.getEditText().getText().toString().trim();
        }

        if (nombre.isEmpty()) {
            binding.tilNombreMascota.setError("El nombre es obligatorio");
            return;
        }

        if (!esModoEdicion && fotoMascotaUri == null) {
            Toast.makeText(getContext(), "Selecciona una foto para la mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnGuardarMascota.setEnabled(false);
        binding.btnGuardarMascota.setText("Guardando...");

        final String nombreFinal = nombre;
        final String razaFinal = raza;
        final String edadFinal = edad;

        if (fotoMascotaUri != null) {
            File archivoFoto = uriToFile(fotoMascotaUri);
            if (archivoFoto == null) {
                Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                restaurarBoton();
                return;
            }
            subirFotoYGuardarDatos(archivoFoto, nombreFinal, razaFinal, edadFinal);
        } else {

            actualizarSoloTextos(nombreFinal, razaFinal, edadFinal);
        }
    }

    private void subirFotoYGuardarDatos(File archivo, String nombre, String raza, String edad) {
        userViewModel.guardarFotoMascota(archivo).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        if (esModoEdicion) {
                            actualizarSoloTextos(nombre, raza, edad);
                        } else {
                            crearRegistroNuevo(nombre, raza, edad);
                        }
                        break;
                    case ERROR:
                        restaurarBoton();
                        Toast.makeText(getContext(), "Error al subir foto: " + resource.message, Toast.LENGTH_LONG).show();
                        break;
                    default: break;
                }
            }
        });
    }

    private void actualizarSoloTextos(String nombre, String raza, String edad) {
        userViewModel.editarMascota(nombre, raza, edad).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        Toast.makeText(getContext(), "¡Cambios guardados!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                        break;
                    case ERROR:
                        restaurarBoton();
                        Toast.makeText(getContext(), "Error al actualizar datos: " + resource.message, Toast.LENGTH_LONG).show();
                        break;
                    default: break;
                }
            }
        });
    }

    private void crearRegistroNuevo(String nombre, String raza, String edad) {
        userViewModel.guardarMascota(nombre, raza, edad).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        Toast.makeText(getContext(), "¡Mascota creada con éxito!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                        break;
                    case ERROR:
                        restaurarBoton();
                        Toast.makeText(getContext(), "Error al crear mascota: " + resource.message, Toast.LENGTH_LONG).show();
                        break;
                    default: break;
                }
            }
        });
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("mascota_tmp", ".jpg", requireContext().getCacheDir());
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
        binding.btnGuardarMascota.setEnabled(true);
        binding.btnGuardarMascota.setText(esModoEdicion ? "Guardar Cambios" : "Guardar");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}