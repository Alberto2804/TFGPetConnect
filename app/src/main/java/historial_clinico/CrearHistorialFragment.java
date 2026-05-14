package historial_clinico;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.Calendar;

import historial_clinico.HistorialMedico;
import es.iesagora.proyectopetconnect.databinding.FragmentCrearHistorialBinding;
import sharedpreferences.PreferencesRepository;
import historial_clinico.HistorialViewModel;

public class CrearHistorialFragment extends Fragment {

    private FragmentCrearHistorialBinding binding;
    private HistorialViewModel viewModel;
    private PreferencesRepository prefs;
    private String mascotaId;

    // Variable de tu código para guardar los milisegundos si los necesitas en el futuro
    private long fechaSeleccionadaMillis;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCrearHistorialBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HistorialViewModel.class);
        prefs = new PreferencesRepository(requireContext());

        // Recuperamos el ID de la mascota
        if (getArguments() != null) {
            mascotaId = getArguments().getString("mascota_id");
        }

        // 1. CONFIGURAR EL DESPLEGABLE (SPINNER)
        String[] opciones = {"Vacuna", "Desparasitación", "Visita", "Cirugía", "Medicación"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                opciones
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTipo.setAdapter(adapter);

        // 2. CONFIGURAR EL CALENDARIO AL TOCAR EL CAMPO DE FECHA
        binding.etFecha.setFocusable(false);
        binding.etFecha.setClickable(true);
        binding.etFecha.setOnClickListener(v -> mostrarDatePicker());

        // 3. CONFIGURAR EL BOTÓN DE GUARDAR
        binding.btnGuardarRegistro.setOnClickListener(v -> {
            String tipoSeleccionado = binding.spinnerTipo.getSelectedItem().toString();
            String fecha = binding.etFecha.getText().toString().trim();
            String desc = binding.etDescripcion.getText().toString().trim();
            String notas = binding.etNotas.getText().toString().trim();

            if (fecha.isEmpty() || desc.isEmpty()) {
                Toast.makeText(getContext(), "Rellena los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            HistorialMedico nuevoRegistro = new HistorialMedico(mascotaId, tipoSeleccionado, fecha, desc, notas);

            String token = "Bearer " + prefs.getToken();
            viewModel.crearRegistro(token, nuevoRegistro).observe(getViewLifecycleOwner(), resource -> {
                if (resource != null) {
                    switch (resource.status) {
                        case LOADING:
                            binding.btnGuardarRegistro.setEnabled(false);
                            binding.btnGuardarRegistro.setText("Guardando...");
                            break;
                        case SUCCESS:
                            Toast.makeText(getContext(), "Registro guardado", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).popBackStack(); // Vuelve a la pantalla anterior
                            break;
                        case ERROR:
                            binding.btnGuardarRegistro.setEnabled(true);
                            binding.btnGuardarRegistro.setText("GUARDAR REGISTRO");
                            Toast.makeText(getContext(), "Error: " + resource.message, Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            });
        });
    }

    // MÉTODO DATEPICKER PERSONALIZADO
    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year1, monthOfYear, dayOfMonth, 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    fechaSeleccionadaMillis = cal.getTimeInMillis();

                    // Formato ajustado a YYYY-MM-DD para la compatibilidad con Supabase
                    String fecha = String.format("%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    binding.etFecha.setText(fecha);
                }, year, month, day);

        // Bloquea seleccionar fechas en el futuro (genial para el historial)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
}