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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.JsonObject;

import java.util.List;

import api.Resource;
import es.iesagora.proyectopetconnect.databinding.FragmentAppBinding;
import sharedpreferences.PreferencesRepository;
import viewmodel.UserViewModel;

public class AppFragment extends Fragment {

    private FragmentAppBinding binding;
    private UserViewModel userViewModel;
    private PreferencesRepository prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        prefs = new PreferencesRepository(requireContext());

        binding.btnSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_appFragment_to_ajustesFragment)
        );
        binding.cardLugares.setOnClickListener(v -> abrirGoogleMaps());

        // Al pulsar en Agenda, navegamos al fragmento de la agenda
        binding.cardAgenda.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_appFragment_to_agendaFragment)
        );

        userViewModel.getPerfilUsuario().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    if (resource.data.has("usuario") && !resource.data.get("usuario").isJsonNull()) {
                        binding.tvNombre.setText(resource.data.get("usuario").getAsString());
                    }
                }
            }
        });

        cargarMascotas();

        // Busca el cardMascota que es el que dice "Mi Mascota - Ver Perfil"
        binding.cardMascota.setOnClickListener(v -> {
            // Solo navegamos si hay una mascota seleccionada
            if (prefs.getMascotaActivaId() != null) {
                Navigation.findNavController(v).navigate(R.id.action_appFragment_to_detalleMascotaFragment);
            } else {
                Toast.makeText(getContext(), "Primero añade una mascota", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarMascotas() {
        userViewModel.getMascotas().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                if (resource.status == Resource.Status.LOADING) {
                    if (binding.progressBarApp != null) binding.progressBarApp.setVisibility(View.VISIBLE);
                } else if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    if (binding.progressBarApp != null) binding.progressBarApp.setVisibility(View.GONE);
                    List<JsonObject> listaMascotas = resource.data;

                    if (listaMascotas.isEmpty()) {
                        binding.cardPerfilMascota.setVisibility(View.GONE);
                        binding.btnCrearMascota.setVisibility(View.VISIBLE);
                        binding.btnCrearMascota.setOnClickListener(v ->
                                Navigation.findNavController(v).navigate(R.id.action_appFragment_to_crearMascotaFragment)
                        );
                    } else {
                        binding.btnCrearMascota.setVisibility(View.GONE);
                        binding.cardPerfilMascota.setVisibility(View.VISIBLE);

                        String activaId = prefs.getMascotaActivaId();
                        JsonObject mascotaAMostrar = listaMascotas.get(0);

                        if (activaId != null) {
                            for (JsonObject m : listaMascotas) {
                                if (m.get("id").getAsString().equals(activaId)) {
                                    mascotaAMostrar = m;
                                    break;
                                }
                            }
                        }

                        prefs.guardarMascotaActivaId(mascotaAMostrar.get("id").getAsString());
                        pintarMascotaEnTarjeta(mascotaAMostrar);

                        binding.cardPerfilMascota.setOnClickListener(v -> mostrarSelectorMascotas(listaMascotas));
                    }
                } else if (resource.status == Resource.Status.ERROR) {
                    if (binding.progressBarApp != null) binding.progressBarApp.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error cargando mascotas", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void pintarMascotaEnTarjeta(JsonObject mascota) {
        String idMascota = mascota.get("id").getAsString();
        String nombre = mascota.has("nombre") && !mascota.get("nombre").isJsonNull() ? mascota.get("nombre").getAsString() : "Sin nombre";
        String raza = mascota.has("raza") && !mascota.get("raza").isJsonNull() ? mascota.get("raza").getAsString() : "";
        String animal = mascota.has("animal") && !mascota.get("animal").isJsonNull() ? mascota.get("animal").getAsString() : "";
        String fechaNacimiento = mascota.has("fecha_nacimiento") && !mascota.get("fecha_nacimiento").isJsonNull() ? mascota.get("fecha_nacimiento").getAsString() : "";
        String sexo = mascota.has("sexo") && !mascota.get("sexo").isJsonNull() ? mascota.get("sexo").getAsString() : "";
        String peso = mascota.has("peso") && !mascota.get("peso").isJsonNull() ? mascota.get("peso").getAsString() : "";
        String urlFoto = mascota.has("foto_url") && !mascota.get("foto_url").isJsonNull() ? mascota.get("foto_url").getAsString() : "";

        // Calculamos la edad usando el ViewModel
        String edadCalculada = userViewModel.calcularEdad(fechaNacimiento);

        binding.tvNombreMascota.setText(nombre);
        binding.tvRazaEdad.setText(raza + " • " + edadCalculada);

        Glide.with(this)
                .load(urlFoto)
                .centerCrop()
                .placeholder(R.drawable.goldenretriever)
                .error(R.drawable.goldenretriever)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgMascota);

        // Preparamos el bundle para cuando se pulse en editar dentro del menú
        Bundle bundleEdicion = new Bundle();
        bundleEdicion.putString("mascota_id", idMascota);
        bundleEdicion.putString("nombre", nombre);
        bundleEdicion.putString("animal", animal);
        bundleEdicion.putString("raza", raza);
        bundleEdicion.putString("fecha_nacimiento", fechaNacimiento);
        bundleEdicion.putString("sexo", sexo);
        bundleEdicion.putString("peso", peso);
        bundleEdicion.putString("urlFoto", urlFoto);

        // Si quieres añadir un botón "Editar" en el selector en el futuro, le pasas este bundleEdicion.
    }

    private void mostrarSelectorMascotas(List<JsonObject> listaMascotas) {
        String[] opciones = new String[listaMascotas.size() + 1];
        for (int i = 0; i < listaMascotas.size(); i++) {
            String nom = listaMascotas.get(i).has("nombre") && !listaMascotas.get(i).get("nombre").isJsonNull() ? listaMascotas.get(i).get("nombre").getAsString() : "Sin nombre";
            opciones[i] = nom;
        }
        opciones[listaMascotas.size()] = "+ Añadir nueva mascota";

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Cambiar mascota")
                .setItems(opciones, (dialog, which) -> {
                    if (which == listaMascotas.size()) {
                        Navigation.findNavController(requireView()).navigate(R.id.action_appFragment_to_crearMascotaFragment);
                    } else {
                        String nuevoId = listaMascotas.get(which).get("id").getAsString();
                        prefs.guardarMascotaActivaId(nuevoId);
                        pintarMascotaEnTarjeta(listaMascotas.get(which));
                        Toast.makeText(getContext(), "Cambiado a " + opciones[which], Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void abrirGoogleMaps() {
        android.net.Uri gmmIntentUri = android.net.Uri.parse("geo:0,0?q=veterinarios, parques de perros");
        android.content.Intent mapIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(mapIntent);
        } catch (android.content.ActivityNotFoundException e) {
            android.net.Uri webUri = android.net.Uri.parse("http://maps.google.com/maps?q=veterinarios,parques");
            startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, webUri));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}