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
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import api.Resource;
import es.iesagora.proyectopetconnect.databinding.FragmentAppBinding;
import viewmodel.UserViewModel;

public class AppFragment extends Fragment {

    private FragmentAppBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnSettings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_appFragment_to_ajustesFragment)
        );

        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        userViewModel.getPerfilUsuario().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == api.Resource.Status.SUCCESS) {
                if (resource.data != null && resource.data.has("usuario") && !resource.data.get("usuario").isJsonNull()) {

                    String nombreUsuario = resource.data.get("usuario").getAsString();

                    binding.tvNombre.setText(nombreUsuario);
                }
            }
        });
        userViewModel.getMascota().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null && resource.status == Resource.Status.SUCCESS) {

                if (resource.data != null) {
                    binding.btnCrearMascota.setVisibility(View.GONE);
                    binding.cardPerfilMascota.setVisibility(View.VISIBLE);

                    String nombreMascota = resource.data.has("nombre") && !resource.data.get("nombre").isJsonNull() ? resource.data.get("nombre").getAsString() : "";
                    String razaMascota = resource.data.has("raza") && !resource.data.get("raza").isJsonNull() ? resource.data.get("raza").getAsString() : "";
                    String edadMascota = resource.data.has("edad") && !resource.data.get("edad").isJsonNull() ? resource.data.get("edad").getAsString() : "";
                    String userId = resource.data.get("user_id").getAsString();

                    binding.tvNombreMascota.setText(nombreMascota);
                    binding.tvRazaEdad.setText(razaMascota + " • " + edadMascota + " años");

                    String urlBase = "https://evrsywohqxoehdnbhpkg.supabase.co"; // Tu URL de Supabase
                    String urlFotoMascota = urlBase + "/storage/v1/object/public/mascotas/" + userId + ".jpg?t=" + System.currentTimeMillis();

                    Glide.with(this)
                            .load(urlFotoMascota)
                            .centerCrop()
                            .error(R.drawable.goldenretriever)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(binding.imgMascota);

                    binding.cardPerfilMascota.setClickable(true);
                    binding.cardPerfilMascota.setFocusable(true);

                    binding.cardPerfilMascota.setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("nombre", nombreMascota);
                        bundle.putString("raza", razaMascota);
                        bundle.putString("edad", edadMascota);
                        bundle.putString("urlFoto", urlFotoMascota);

                        Navigation.findNavController(v).navigate(R.id.action_appFragment_to_crearMascotaFragment, bundle);
                    });

                } else {

                    binding.cardPerfilMascota.setVisibility(View.GONE);
                    binding.btnCrearMascota.setVisibility(View.VISIBLE);

                    binding.btnCrearMascota.setOnClickListener(v ->
                            Navigation.findNavController(v).navigate(R.id.action_appFragment_to_crearMascotaFragment)
                    );
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