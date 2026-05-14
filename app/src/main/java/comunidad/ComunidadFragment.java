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

import java.util.List;

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

        binding.recyclerViewChat.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                // dy < 0 significa que el usuario está deslizando hacia arriba
                // !recyclerView.canScrollVertically(-1) comprueba si se ha llegado al inicio de la lista
                if (dy < 0 && !recyclerView.canScrollVertically(-1)) {
                    // Si llegamos al tope superior, pedimos más mensajes
                    comunidadViewModel.cargarMasMensajes();
                }
            }
        });

        // Activamos la carga en tiempo real (similar a Firestore snapshot)
        String authHeader = "Bearer " + prefs.getToken();
        comunidadViewModel.getMensajes(authHeader).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {

                List<Mensaje> nuevaLista = resource.data;
                int cantidadAnterior = chatAdapter.getItemCount();
                int mensajesAñadidos = nuevaLista.size() - cantidadAnterior;

                LinearLayoutManager lm = (LinearLayoutManager) binding.recyclerViewChat.getLayoutManager();

                // 1. Guardamos la posición exacta al milímetro antes de actualizar
                int posicionActual = lm != null ? lm.findFirstVisibleItemPosition() : 0;
                View vistaSuperior = lm != null ? lm.findViewByPosition(posicionActual) : null;
                int offsetPíxeles = vistaSuperior != null ? vistaSuperior.getTop() : 0;

                // Comprobamos si estabas leyendo el final de la conversación
                boolean estabaAlFinal = lm != null && lm.findLastVisibleItemPosition() >= cantidadAnterior - 2;

                // 2. Metemos los mensajes (esto descoloca la lista internamente)
                chatAdapter.setLista(nuevaLista);

                // 3. Restauramos la posición
                if (cantidadAnterior == 0 || estabaAlFinal) {
                    // Si acabas de entrar o estabas abajo del todo, te llevamos al último mensaje
                    binding.recyclerViewChat.scrollToPosition(nuevaLista.size() - 1);

                } else if (mensajesAñadidos > 0 && lm != null) {
                    // EL ANTI-SALTO: Le sumamos a tu posición los 50 mensajes que acaban de entrar por arriba.
                    // Visulamente la pantalla se quedará congelada en el texto que estabas leyendo.
                    lm.scrollToPositionWithOffset(posicionActual + mensajesAñadidos, offsetPíxeles);
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