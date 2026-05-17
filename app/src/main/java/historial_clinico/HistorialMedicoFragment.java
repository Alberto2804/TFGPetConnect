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

public class HistorialMedicoFragment extends Fragment {

    private FragmentHistorialMedicoBinding binding;
    private HistorialViewModel viewModel;
    private HistorialAdapter adapter;
    private String mascotaId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistorialMedicoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreferencesRepository prefs = new PreferencesRepository(requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(HistorialViewModel.class);

        // Recuperamos el ID de la mascota
        if (getArguments() != null) {
            mascotaId = getArguments().getString("mascota_id");
        }
        if (mascotaId == null) {
            mascotaId = prefs.getMascotaActivaId();
        }


        // Inicializamos el adaptador pasándole la acción de borrar
        adapter = new HistorialAdapter(registroId -> {

            // OJO AQUÍ: Supabase siempre necesita el "Bearer " delante del token para autorizar el borrado
            String token = "Bearer " + prefs.getToken();

            // Llamamos al ViewModel para borrar el registro
            viewModel.borrarRegistro(token, registroId).observe(getViewLifecycleOwner(), resource -> {
                if (resource.status == api.Resource.Status.SUCCESS) {
                    android.widget.Toast.makeText(getContext(), "Registro borrado", android.widget.Toast.LENGTH_SHORT).show();

                    // ¡AQUÍ LLAMAMOS AL NUEVO MÉTODO PARA RECARGAR LA PANTALLA!
                    cargarHistorial(prefs.getToken());

                } else if (resource.status == api.Resource.Status.ERROR) {
                    android.widget.Toast.makeText(getContext(), "Error al borrar", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.recyclerHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerHistorial.setAdapter(adapter);

        // ¡AQUÍ LLAMAMOS AL MÉTODO POR PRIMERA VEZ AL ABRIR LA PANTALLA!
        cargarHistorial(prefs.getToken());

        binding.fabAddRegistro.setOnClickListener(v -> {
            android.os.Bundle bundle = new android.os.Bundle();
            bundle.putString("mascota_id", mascotaId);
            androidx.navigation.Navigation.findNavController(v)
                    .navigate(es.iesagora.proyectopetconnect.R.id.action_historialMedicoFragment_to_crearHistorialFragment, bundle);
        });
    }

    // =========================================================
    // ESTE ES EL MÉTODO NUEVO QUE HEMOS EXTRAÍDO
    // =========================================================
    private void cargarHistorial(String tokenPuro) {
        viewModel.getHistorial("Bearer " + tokenPuro, mascotaId).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        binding.progressBarHistorial.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        binding.progressBarHistorial.setVisibility(View.GONE);
                        if (resource.data != null) {
                            adapter.setLista(resource.data); // Pinta la lista nueva (sin el borrado)
                        }
                        break;
                    case ERROR:
                        binding.progressBarHistorial.setVisibility(View.GONE);
                        android.widget.Toast.makeText(getContext(), "Error cargando historial", android.widget.Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}