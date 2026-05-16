package es.iesagora.proyectopetconnect;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.util.Calendar;

import api.Resource;
import es.iesagora.proyectopetconnect.databinding.FragmentCrearMascotaBinding;
import viewmodel.UserViewModel;

public class CrearMascotaFragment extends Fragment {

    private FragmentCrearMascotaBinding binding;
    private UserViewModel userViewModel;
    private Uri fotoMascotaUri = null;
    private boolean esModoEdicion = false;
    private String urlFotoActual = null;
    private String idMascotaEditar = null;

    private final ActivityResultLauncher<String> selectorImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoMascotaUri = uri;
                    binding.ivFotoMascota.setImageURI(uri);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCrearMascotaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        configurarDesplegables();
        configurarCalendario();

        if (getArguments() != null && getArguments().containsKey("mascota_id")) {
            esModoEdicion = true;
            binding.tvTitulo.setText("Editar Mascota");
            binding.btnGuardarMascota.setText("Guardar Cambios");

            idMascotaEditar = getArguments().getString("mascota_id");

            binding.etNombre.setText(getArguments().getString("nombre", ""));
            binding.etAnimal.setText(getArguments().getString("animal", ""), false);
            binding.etRaza.setText(getArguments().getString("raza", ""), false);
            binding.etRaza.setEnabled(true);
            binding.etFechaNacimiento.setText(getArguments().getString("fecha_nacimiento", ""));
            binding.etSexo.setText(getArguments().getString("sexo", ""), false);
            binding.etPeso.setText(getArguments().getString("peso", ""));

            urlFotoActual = getArguments().getString("urlFoto");
            if (urlFotoActual != null && !urlFotoActual.isEmpty()) {
                Glide.with(this).load(urlFotoActual)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(binding.ivFotoMascota);
            }
        }

        binding.ivFotoMascota.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));
        binding.btnSeleccionarFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));
        binding.btnGuardarMascota.setOnClickListener(v -> procesarGuardado());
    }

    private void configurarDesplegables() {
        // 1. Bloqueamos el campo de raza por defecto para que no se pueda pulsar
        binding.etRaza.setEnabled(false);

        ArrayAdapter<String> adapterAnimal = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, userViewModel.getListaAnimales());
        binding.etAnimal.setAdapter(adapterAnimal);

        ArrayAdapter<String> adapterSexo = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, userViewModel.getListaSexo());
        binding.etSexo.setAdapter(adapterSexo);

        binding.etAnimal.setOnItemClickListener((parent, view, position, id) -> {
            String animalSeleccionado = userViewModel.getListaAnimales()[position];
            String[] razas = userViewModel.getRazasPorAnimal(animalSeleccionado);

            ArrayAdapter<String> adapterRaza = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, razas);
            binding.etRaza.setAdapter(adapterRaza);
            binding.etRaza.setText("", false);

            // 2. ¡Desbloqueamos el campo de raza porque ya hay un animal seleccionado!
            binding.etRaza.setEnabled(true);
        });
    }

    private void configurarCalendario() {
        binding.etFechaNacimiento.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(requireContext(),
                    (view1, anio, mes, dia) -> {
                        String mesFormateado = String.format("%02d", mes + 1);
                        String diaFormateado = String.format("%02d", dia);
                        binding.etFechaNacimiento.setText(anio + "-" + mesFormateado + "-" + diaFormateado);
                    },
                    calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void procesarGuardado() {
        String nombre = binding.etNombre.getText().toString().trim();
        String animal = binding.etAnimal.getText().toString().trim();
        String raza = binding.etRaza.getText().toString().trim();
        String fechaNacimiento = binding.etFechaNacimiento.getText().toString().trim();
        String sexo = binding.etSexo.getText().toString().trim();
        String peso = binding.etPeso.getText().toString().trim();

        if (nombre.isEmpty()) {
            binding.etNombre.setError("Obligatorio");
            return;
        }

        binding.btnGuardarMascota.setEnabled(false);
        binding.btnGuardarMascota.setText("Guardando...");

        if (fotoMascotaUri != null) {
            File archivoFoto = uriToFile(fotoMascotaUri);
            if (archivoFoto != null) subirFotoYGuardarDatos(archivoFoto, nombre, animal, raza, fechaNacimiento, sexo, peso);
        } else {
            actualizarSoloTextos(nombre, animal, raza, fechaNacimiento, sexo, peso);
        }
    }

    private void subirFotoYGuardarDatos(File archivo, String nombre, String animal, String raza, String fechaNacimiento, String sexo, String peso) {
        String nombreUnicoArchivo = java.util.UUID.randomUUID().toString() + ".jpg";

        userViewModel.guardarFotoMascota(nombreUnicoArchivo, archivo).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {
                String urlFotoGenerada = resource.data;
                if (esModoEdicion) {
                    userViewModel.editarMascota(idMascotaEditar, nombre, animal, raza, fechaNacimiento, sexo, peso, urlFotoGenerada).observe(getViewLifecycleOwner(), this::gestionarRespuesta);
                } else {
                    userViewModel.guardarMascota(nombre, animal, raza, fechaNacimiento, sexo, peso, urlFotoGenerada).observe(getViewLifecycleOwner(), this::gestionarRespuesta);
                }
            } else if (resource != null && resource.status == Resource.Status.ERROR) {
                binding.btnGuardarMascota.setEnabled(true);
                binding.btnGuardarMascota.setText("Guardar Mascota");
                Toast.makeText(getContext(), "Error subiendo foto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarSoloTextos(String nombre, String animal, String raza, String fechaNacimiento, String sexo, String peso) {
        if (esModoEdicion) {
            userViewModel.editarMascota(idMascotaEditar, nombre, animal, raza, fechaNacimiento, sexo, peso, urlFotoActual).observe(getViewLifecycleOwner(), this::gestionarRespuesta);
        } else {
            // Si es nueva y no tiene foto, guardamos sin fotoUrl
            userViewModel.guardarMascota(nombre, animal, raza, fechaNacimiento, sexo, peso, null).observe(getViewLifecycleOwner(), this::gestionarRespuesta);
        }
    }

    private void gestionarRespuesta(Resource<Void> resource) {
        if (resource != null && resource.status == Resource.Status.SUCCESS) {
            Toast.makeText(getContext(), "Guardado con éxito", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
        } else if (resource != null && resource.status == Resource.Status.ERROR) {
            binding.btnGuardarMascota.setEnabled(true);
            binding.btnGuardarMascota.setText("Guardar Mascota");
            Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_LONG).show();
        }
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload", ".jpg", requireContext().getCacheDir());
            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) out.write(buffer, 0, read);
            out.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}