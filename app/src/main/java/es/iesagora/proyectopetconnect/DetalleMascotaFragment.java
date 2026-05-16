package es.iesagora.proyectopetconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import es.iesagora.proyectopetconnect.databinding.FragmentDetalleMascotaBinding;
import viewmodel.UserViewModel;

public class DetalleMascotaFragment extends Fragment {
    private FragmentDetalleMascotaBinding binding;
    private UserViewModel userViewModel;
    private JsonObject mascotaActual;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetalleMascotaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        cargarDatosMascota();

        binding.fabEditarMascota.setOnClickListener(v -> {
            if (mascotaActual != null) {
                Bundle b = new Bundle();
                b.putString("mascota_id", mascotaActual.get("id").getAsString());
                b.putString("nombre", mascotaActual.get("nombre").getAsString());
                b.putString("animal", mascotaActual.get("animal").getAsString());
                b.putString("raza", mascotaActual.get("raza").getAsString());
                b.putString("fecha_nacimiento", mascotaActual.get("fecha_nacimiento").getAsString());
                b.putString("sexo", mascotaActual.get("sexo").getAsString());
                b.putString("peso", mascotaActual.has("peso") && !mascotaActual.get("peso").isJsonNull() ? mascotaActual.get("peso").getAsString() : "");
                b.putString("urlFoto", mascotaActual.has("foto_url") && !mascotaActual.get("foto_url").isJsonNull() ? mascotaActual.get("foto_url").getAsString() : "");

                Navigation.findNavController(view).navigate(R.id.action_detalleMascotaFragment_to_crearMascotaFragment, b);
            }
        });
    }

    private void cargarDatosMascota() {
        String idActiva = userViewModel.getMascotaActivaId();
        userViewModel.getMascotas().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null) {
                for (JsonObject m : resource.data) {
                    if (m.get("id").getAsString().equals(idActiva)) {
                        mascotaActual = m;
                        pintarUI(m);
                        break;
                    }
                }
            }
        });
    }

    private void pintarUI(JsonObject m) {
        binding.tvDetalleNombre.setText(m.get("nombre").getAsString());
        binding.tvDetalleRazaSuperior.setText(m.get("raza").getAsString());
        binding.tvInfoEspecie.setText(m.get("animal").getAsString());
        binding.tvInfoSexo.setText(m.get("sexo").getAsString());
        binding.tvInfoPeso.setText(m.has("peso") && !m.get("peso").isJsonNull() ? m.get("peso").getAsString() + " kg" : "No definido");

        String edad = userViewModel.calcularEdad(m.get("fecha_nacimiento").getAsString());
        binding.tvInfoEdad.setText(edad);

        if (m.has("foto_url") && !m.get("foto_url").isJsonNull()) {
            Glide.with(this).load(m.get("foto_url").getAsString()).into(binding.ivDetalleFoto);
        }
    }
}