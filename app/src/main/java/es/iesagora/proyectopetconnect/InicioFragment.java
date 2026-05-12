package es.iesagora.proyectopetconnect;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import es.iesagora.proyectopetconnect.databinding.FragmentInicioBinding;


public class InicioFragment extends Fragment {

    private FragmentInicioBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentInicioBinding.inflate(inflater, container, false);

        binding.btnIniciarSesion.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_inicioFragment_to_loginFragment)
        );

        binding.btnRegistrarse.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_inicioFragment_to_registroFragment)
        );

        return binding.getRoot();
    }
}
