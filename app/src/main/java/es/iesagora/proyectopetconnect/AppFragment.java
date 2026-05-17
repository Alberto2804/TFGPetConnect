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
    private JsonObject mascotaActualActiva;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        prefs = new PreferencesRepository(requireContext());

        binding.btnSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_appFragment_to_ajustesFragment)
        );
        binding.cardLugares.setOnClickListener(v -> abrirGoogleMaps());

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
                else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), "Sesión inválida o expirada", Toast.LENGTH_SHORT).show();
                    prefs.cerrarSesion();
                    android.content.Intent intent = new android.content.Intent(requireActivity(), auth.AuthActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                }
            }
        });

        cargarMascotas();
        obtenerUbicacionYClima();

        // 🚀 CARGA CERO: Pasamos los datos calientes en el Bundle al pulsar ver perfil
        binding.cardMascota.setOnClickListener(v -> {
            // 🚀 CARGA LIMPIA: No pasamos ningún bundle gigante. Solo navegamos.
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

                        mascotaActualActiva = mascotaAMostrar; // Sincronizamos
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
        String nombre = mascota.has("nombre") && !mascota.get("nombre").isJsonNull() ? mascota.get("nombre").getAsString() : "Sin nombre";
        String raza = mascota.has("raza") && !mascota.get("raza").isJsonNull() ? mascota.get("raza").getAsString() : "";
        String fechaNacimiento = mascota.has("fecha_nacimiento") && !mascota.get("fecha_nacimiento").isJsonNull() ? mascota.get("fecha_nacimiento").getAsString() : "";
        String urlFoto = mascota.has("foto_url") && !mascota.get("foto_url").isJsonNull() ? mascota.get("foto_url").getAsString() : "";

        String edadCalculada = userViewModel.calcularEdad(fechaNacimiento);

        binding.tvNombreMascota.setText(nombre);
        binding.tvRazaEdad.setText(raza + " • " + edadCalculada);

        Glide.with(this)
                .load(urlFoto)
                .centerCrop()
                .placeholder(R.drawable.ic_foto)
                .error(R.drawable.ic_foto)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.imgMascota);
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
                        JsonObject seleccionada = listaMascotas.get(which);
                        mascotaActualActiva = seleccionada; // Actualizamos la activa al cambiar
                        prefs.guardarMascotaActivaId(seleccionada.get("id").getAsString());
                        pintarMascotaEnTarjeta(seleccionada);
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

    private void obtenerUbicacionYClima() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 102);
            return;
        }

        android.location.LocationManager locationManager = (android.location.LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);
        if (locationManager != null) {
            android.location.Location location = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            }

            if (location != null) {
                cargarConsejoDelClima(location.getLatitude(), location.getLongitude());
            } else {
                cargarConsejoDelClima(39.47, -6.37);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionYClima();
        }
    }

    private void cargarConsejoDelClima(double latitud, double longitud) {
        api.WeatherClient.getApi().getCurrentWeather(latitud, longitud, true)
                .enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.google.gson.JsonObject> call, retrofit2.Response<com.google.gson.JsonObject> response) {
                        if (binding == null || !isAdded() || getContext() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                com.google.gson.JsonObject current = response.body().getAsJsonObject("current_weather");
                                double temp = current.get("temperature").getAsDouble();
                                int weatherCode = current.get("weathercode").getAsInt();

                                binding.tvClimaTemp.setText(temp + " ºC en tu zona");

                                if (weatherCode >= 51 && weatherCode <= 99) {
                                    binding.tvClimaConsejo.setText("Día lluvioso. Si sales, seca bien sus patas y orejas al volver para evitar hongos.");
                                    binding.ivClimaIcono.setImageResource(R.drawable.ic_lluvia);
                                } else if (temp >= 30) {
                                    binding.tvClimaConsejo.setText("¡Alerta por calor! Riesgo de golpe de calor. Renueva el agua constantemente.");
                                    binding.ivClimaIcono.setImageResource(R.drawable.icono_calor);
                                } else if (temp <= 8) {
                                    binding.tvClimaConsejo.setText("Bajas temperaturas. Protege los hábitats de tus mascotas de corrientes de aire.");
                                    binding.ivClimaIcono.setImageResource(R.drawable.icono_frio);
                                } else {
                                    binding.tvClimaConsejo.setText("Clima excelente. Aprovecha para pasear o jugar al aire libre sin preocupaciones.");
                                    binding.ivClimaIcono.setImageResource(R.drawable.icono_ideal);
                                }
                            } catch (Exception e) {
                                binding.tvClimaTemp.setText("Clima no disponible");
                            }
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<com.google.gson.JsonObject> call, Throwable t) {}
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}