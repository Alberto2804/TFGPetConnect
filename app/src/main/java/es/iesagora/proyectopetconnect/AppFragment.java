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

        // 1. Configurar botones básicos
        binding.btnSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_appFragment_to_ajustesFragment)
        );

        binding.cardLugares.setOnClickListener(v -> abrirGoogleMaps());

        // 2. Observar el perfil del usuario (Nombre)
        userViewModel.getPerfilUsuario().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                if (resource.data.has("usuario") && !resource.data.get("usuario").isJsonNull()) {
                    binding.tvNombre.setText(resource.data.get("usuario").getAsString());
                }
            }
        });

        // 3. Lógica Multimascota: Observar la lista completa
        cargarMascotas();
    }

    private void cargarMascotas() {
        userViewModel.getMascotas().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                List<JsonObject> listaMascotas = resource.data;

                if (listaMascotas.isEmpty()) {
                    // Si no hay mascotas, mostramos el botón de añadir
                    binding.cardPerfilMascota.setVisibility(View.GONE);
                    binding.btnCrearMascota.setVisibility(View.VISIBLE);
                    binding.btnCrearMascota.setOnClickListener(v ->
                            Navigation.findNavController(v).navigate(R.id.action_appFragment_to_crearMascotaFragment)
                    );
                } else {
                    // Si hay mascotas, gestionamos la "Activa"
                    binding.btnCrearMascota.setVisibility(View.GONE);
                    binding.cardPerfilMascota.setVisibility(View.VISIBLE);

                    String activaId = prefs.getMascotaActivaId();
                    JsonObject mascotaAMostrar = listaMascotas.get(0); // Por defecto la primera

                    // Buscamos si la mascota activa está en la lista descargada
                    if (activaId != null) {
                        for (JsonObject m : listaMascotas) {
                            if (m.get("id").getAsString().equals(activaId)) {
                                mascotaAMostrar = m;
                                break;
                            }
                        }
                    }

                    // Guardamos/Actualizamos el ID en preferencias por si acaso
                    prefs.guardarMascotaActivaId(mascotaAMostrar.get("id").getAsString());

                    // Pintamos los datos en la UI
                    pintarMascotaEnTarjeta(mascotaAMostrar);

                    // Al hacer clic, abrimos el selector de mascotas
                    binding.cardPerfilMascota.setOnClickListener(v -> mostrarSelectorMascotas(listaMascotas));
                }
            }
        });
    }

    private void pintarMascotaEnTarjeta(JsonObject mascota) {
        String nombre = mascota.has("nombre") ? mascota.get("nombre").getAsString() : "Sin nombre";
        String raza = mascota.has("raza") ? mascota.get("raza").getAsString() : "";
        String edad = mascota.has("edad") ? mascota.get("edad").getAsString() : "";
        String userId = mascota.get("user_id").getAsString();

        binding.tvNombreMascota.setText(nombre);
        binding.tvRazaEdad.setText(raza + " • " + edad + " años");

        String urlFoto = "https://evrsywohqxoehdnbhpkg.supabase.co/storage/v1/object/public/mascotas/" + userId + ".jpg?t=" + System.currentTimeMillis();

        Glide.with(this)
                .load(urlFoto)
                .centerCrop()
                .error(R.drawable.goldenretriever)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.imgMascota);
    }

    private void mostrarSelectorMascotas(List<JsonObject> listaMascotas) {
        String[] opciones = new String[listaMascotas.size() + 1];
        for (int i = 0; i < listaMascotas.size(); i++) {
            opciones[i] = listaMascotas.get(i).get("nombre").getAsString();
        }
        opciones[listaMascotas.size()] = "+ Añadir nueva mascota";

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Cambiar mascota")
                .setItems(opciones, (dialog, which) -> {
                    if (which == listaMascotas.size()) {
                        // Navegar a crear nueva
                        Navigation.findNavController(requireView()).navigate(R.id.action_appFragment_to_crearMascotaFragment);
                    } else {
                        // Cambiar activa y refrescar
                        String nuevoId = listaMascotas.get(which).get("id").getAsString();
                        prefs.guardarMascotaActivaId(nuevoId);

                        // Refresco visual rápido de la tarjeta
                        pintarMascotaEnTarjeta(listaMascotas.get(which));
                        Toast.makeText(getContext(), "Mascota cambiada a " + opciones[which], Toast.LENGTH_SHORT).show();
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