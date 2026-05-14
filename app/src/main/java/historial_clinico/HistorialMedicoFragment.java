package historial_clinico;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;


import es.iesagora.proyectopetconnect.databinding.FragmentHistorialMedicoBinding;
import sharedpreferences.PreferencesRepository;
import historial_clinico.HistorialViewModel;


public class HistorialMedicoFragment extends Fragment {

    private FragmentHistorialMedicoBinding binding;
    private HistorialViewModel viewModel;
    private HistorialAdapter adapter;
    private String mascotaId; // Deberás pasarlo por Bundle

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistorialMedicoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recuperamos el ID de la mascota que se seleccionó
        if (getArguments() != null) {
            mascotaId = getArguments().getString("mascota_id");
        }

        PreferencesRepository prefs = new PreferencesRepository(requireContext());
        mascotaId = prefs.getMascotaActivaId();

        viewModel = new ViewModelProvider(this).get(HistorialViewModel.class);


        adapter = new HistorialAdapter();
        binding.recyclerHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerHistorial.setAdapter(adapter);

        viewModel.getHistorial("Bearer " + prefs.getToken(), mascotaId).observe(getViewLifecycleOwner(), resource -> {
            if (resource.data != null) {
                adapter.setLista(resource.data);
            }
        });

        binding.fabAddRegistro.setOnClickListener(v -> {
            // 1. Creamos la "mochila" de datos
            android.os.Bundle bundle = new android.os.Bundle();

            // 2. Metemos el ID de la mascota actual para que el formulario sepa a quién guardarle el registro
            bundle.putString("mascota_id", mascotaId);

            // 3. Viajamos a la pantalla de Crear Registro usando la acción de tu nav_graph
            androidx.navigation.Navigation.findNavController(v)
                    .navigate(es.iesagora.proyectopetconnect.R.id.action_historialMedicoFragment_to_crearHistorialFragment, bundle);
        });
    }
}