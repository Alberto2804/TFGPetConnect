package comunidad;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import api.Resource;
import comunidad.Mensaje;
import es.iesagora.proyectopetconnect.databinding.FragmentComunidadBinding;
import sharedpreferences.PreferencesRepository;
import comunidad.ComunidadViewModel;
import viewmodel.UserViewModel;

public class ComunidadFragment extends Fragment {

    private FragmentComunidadBinding binding;
    private ComunidadViewModel comunidadViewModel;
    private UserViewModel userViewModel;
    private PreferencesRepository prefs;

    private ChatAdapter chatAdapter;
    private String currentUserName = "Anónimo";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComunidadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = new PreferencesRepository(requireContext());
        comunidadViewModel = new ViewModelProvider(this).get(ComunidadViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Obtenemos el nombre del usuario actual
        userViewModel.getPerfilUsuario().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.data != null && resource.data.has("usuario")) {
                currentUserName = resource.data.get("usuario").getAsString();
            }
        });

        // Configuramos la lista
        chatAdapter = new ChatAdapter(prefs.getUserId());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        binding.recyclerViewChat.setLayoutManager(layoutManager);
        binding.recyclerViewChat.setAdapter(chatAdapter);

        // Activamos la carga en tiempo real (similar a Firestore snapshot)
        String authHeader = "Bearer " + prefs.getToken();
        comunidadViewModel.getMensajes(authHeader).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                int tamañoAnterior = chatAdapter.getItemCount();
                chatAdapter.setLista(resource.data);

                // Si entra un mensaje nuevo, scrollear abajo
                if (resource.data.size() > tamañoAnterior) {
                    binding.recyclerViewChat.scrollToPosition(resource.data.size() - 1);
                }
            }
        });

        // Botón de enviar (similar a tu DetalleFragment)
        binding.btnEnviar.setOnClickListener(v -> {
            String texto = binding.etMensaje.getText().toString().trim();
            if (!texto.isEmpty()) {
                Mensaje c = new Mensaje(prefs.getUserId(), currentUserName, texto);
                comunidadViewModel.agregarMensaje(authHeader, c);
                binding.etMensaje.setText("");
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        comunidadViewModel.pausarListener();
    }
}