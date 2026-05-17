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
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import api.Resource;
import auth.AuthActivity;
import es.iesagora.proyectopetconnect.databinding.FragmentAjustesBinding;
import viewmodel.UserViewModel;
import androidx.appcompat.app.AppCompatDelegate;

public class AjustesFragment extends Fragment {

    private FragmentAjustesBinding binding;
    private UserViewModel userViewModel;
    private sharedpreferences.PreferencesRepository prefs;

    private ActivityResultLauncher<String> selectorImagenLauncher;
    private Uri nuevaFotoUri = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectorImagenLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        nuevaFotoUri = uri;
                        binding.ivFotoPerfil.setImageURI(uri);
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAjustesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        prefs = new sharedpreferences.PreferencesRepository(requireContext());
        String userId = prefs.getUserId();

        // Carga estática inicial de reserva
        String urlFotoInicial = "https://evrsywohqxoehdnbhpkg.supabase.co/storage/v1/object/public/avatares/" + userId + ".jpg";

        Glide.with(this)
                .load(urlFotoInicial)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.ivFotoPerfil);

        // 🚀 CONTROL DINÁMICO: Escuchamos el perfil y gestionamos los permisos en tiempo real
        userViewModel.getPerfilUsuario().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS && resource.data != null) {
                // Pintamos el nombre
                if (resource.data.has("usuario") && !resource.data.get("usuario").isJsonNull()) {
                    binding.etNombreUsuario.setText(resource.data.get("usuario").getAsString());
                }

                // 🔥 CORREGIDO: Leemos la URL real con el timestamp (?t=...) persistido en tu BD de Supabase
                if (resource.data.has("foto_url") && !resource.data.get("foto_url").isJsonNull()) {
                    String urlFotoServidor = resource.data.get("foto_url").getAsString();
                    if (!urlFotoServidor.isEmpty()) {
                        Glide.with(this)
                                .load(urlFotoServidor)
                                .circleCrop()
                                .placeholder(R.drawable.ic_foto)
                                .error(R.drawable.ic_foto)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(binding.ivFotoPerfil);
                    } else {
                    // 🔥 Solución: Si el String de la BD está vacío, forzamos ic_foto
                    Glide.with(this).load(R.drawable.ic_foto).circleCrop().into(binding.ivFotoPerfil);
                    }
                }else {
                    // 🔥 Solución: Si ni siquiera existe el campo foto_url, forzamos ic_foto
                    Glide.with(this).load(R.drawable.ic_foto).circleCrop().into(binding.ivFotoPerfil);
                }

                // EXTRAEMOS Y GUARDAMOS EL ROL DEL SERVIDOR
                if (resource.data.has("rol") && !resource.data.get("rol").isJsonNull()) {
                    String rolServidor = resource.data.get("rol").getAsString();
                    prefs.guardarRol(rolServidor);
                }

                // EVALUAMOS VISIBILIDAD: Ahora que el dato está listo, mostramos u ocultamos el botón
                if (prefs.isAdmin()) {
                    binding.btnVerUsuarios.setVisibility(View.VISIBLE);
                    binding.btnVerUsuarios.setOnClickListener(v -> abrirPanelAdministrador());
                } else {
                    binding.btnVerUsuarios.setVisibility(View.GONE);
                }
            }
        });

        boolean modoOscuroActivado = userViewModel.isModoOscuro();
        binding.switchModoOscuro.setChecked(modoOscuroActivado);

        binding.switchModoOscuro.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userViewModel.setModoOscuro(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        binding.btnCambiarFoto.setOnClickListener(v -> selectorImagenLauncher.launch("image/*"));
        binding.btnGuardarPerfil.setOnClickListener(v -> guardarCambios());
        binding.btnLogout.setOnClickListener(v -> hacerLogout());
    }

    private void abrirPanelAdministrador() {
        String token = prefs.getToken();
        userViewModel.obtenerTodosLosUsuarios(token).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                    List<JsonObject> usuarios = resource.data;

                    String[] opcionesDialogo = new String[usuarios.size()];
                    for (int i = 0; i < usuarios.size(); i++) {
                        JsonObject u = usuarios.get(i);
                        String nombre = u.has("usuario") && !u.get("usuario").isJsonNull() ? u.get("usuario").getAsString() : "Sin nombre";
                        String rol = u.has("rol") && !u.get("rol").isJsonNull() ? u.get("rol").getAsString() : "user";
                        opcionesDialogo[i] = nombre + " [" + rol.toUpperCase() + "]";
                    }

                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("👥 Gestión de Usuarios (Admin)")
                            .setItems(opcionesDialogo, (dialog, which) -> {
                                JsonObject usuarioSeleccionado = usuarios.get(which);
                                String targetId = usuarioSeleccionado.get("id").getAsString();
                                String targetNombre = usuarioSeleccionado.has("usuario") && !usuarioSeleccionado.get("usuario").isJsonNull() ? usuarioSeleccionado.get("usuario").getAsString() : "Sin nombre";
                                String targetRol = usuarioSeleccionado.has("rol") && !usuarioSeleccionado.get("rol").isJsonNull() ? usuarioSeleccionado.get("rol").getAsString() : "user";

                                if (targetId.equals(prefs.getUserId())) {
                                    Toast.makeText(getContext(), "No puedes eliminar tu propia cuenta de administrador", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if ("admin".equals(targetRol)) {
                                    Toast.makeText(getContext(), "No tienes rango suficiente para eliminar a otro administrador", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                new android.app.AlertDialog.Builder(requireContext())
                                        .setTitle("⚠️ Eliminar Cuenta")
                                        .setMessage("¿Estás completamente seguro de que deseas fulminar el perfil de " + targetNombre + "? Se perderán sus mascotas, historiales y citas de forma permanente.")
                                        .setPositiveButton("Sí, eliminar", (dialog1, which1) -> solicitarBorradoServidor(targetId))
                                        .setNegativeButton("Cancelar", null)
                                        .show();
                            })
                            .setNegativeButton("Cerrar", null)
                            .show();
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), "Error en el servidor al cargar perfiles", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void solicitarBorradoServidor(String idUsuarioABorrar) {
        String token = prefs.getToken();
        userViewModel.eliminarUsuarioAdmin(token, idUsuarioABorrar).observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                if (resource.status == Resource.Status.SUCCESS) {
                    Toast.makeText(getContext(), "Perfil eliminado con éxito del sistema", Toast.LENGTH_SHORT).show();
                    abrirPanelAdministrador();
                } else if (resource.status == Resource.Status.ERROR) {
                    Toast.makeText(getContext(), "Error de permisos: " + resource.message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void guardarCambios() {
        String nuevoNombre = binding.etNombreUsuario.getText().toString().trim();

        if (nuevoNombre.isEmpty()) {
            Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnGuardarPerfil.setEnabled(false);
        binding.btnGuardarPerfil.setText("Guardando...");

        File archivoFoto = (nuevaFotoUri != null) ? uriToFile(nuevaFotoUri) : null;

        if (archivoFoto != null) {
            userViewModel.subirFotoPerfil(archivoFoto).observe(getViewLifecycleOwner(), resourceUrl -> {
                switch (resourceUrl.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        String urlPublica = resourceUrl.data;
                        actualizarDatos(nuevoNombre, urlPublica);
                        nuevaFotoUri = null;
                        break;
                    case ERROR:
                        Toast.makeText(getContext(), "Error subiendo la foto", Toast.LENGTH_SHORT).show();
                        restaurarBoton();
                        break;
                }
            });
        } else {
            actualizarDatos(nuevoNombre, null);
        }
    }

    private void actualizarDatos(String nombre, String urlFoto) {
        userViewModel.actualizarPerfilDB(nombre, urlFoto).observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == api.Resource.Status.SUCCESS) {
                Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();

                // 🔥 CORREGIDO: Inyección de carga inmediata. Si se ha modificado la foto, obligamos
                // a Glide a renderizar la URL con el timestamp dinámico para saltarse la caché local de inmediato.
                if (urlFoto != null && !urlFoto.isEmpty()) {
                    Glide.with(this)
                            .load(urlFoto)
                            .circleCrop()
                            .placeholder(R.drawable.ic_foto)
                            .error(R.drawable.ic_foto)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(binding.ivFotoPerfil);
                }

                restaurarBoton();
            } else if (resource.status == api.Resource.Status.ERROR) {
                Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
                restaurarBoton();
            }
        });
    }

    private void hacerLogout() {
        userViewModel.hacerLogout();
        prefs.cerrarSesion();
        android.content.Intent intent = new android.content.Intent(requireContext(), AuthActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("avatar_tmp", ".jpg", requireContext().getCacheDir());
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
        binding.btnGuardarPerfil.setEnabled(true);
        binding.btnGuardarPerfil.setText("Guardar Cambios");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}