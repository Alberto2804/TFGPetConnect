package agenda;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.CalendarView;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import api.Resource;
import es.iesagora.proyectopetconnect.NotificationReceiver;
import es.iesagora.proyectopetconnect.R;

public class AgendaFragment extends Fragment {
    private AgendaViewModel viewModel;
    private AgendaAdapter adapter;

    // Variables globales para recordar el día seleccionado
    private String fechaSeleccionada;
    private Calendar calendarFechaElegida = Calendar.getInstance(); // Inicializado por defecto a hoy

    private List<JsonObject> todasLasCitas = new ArrayList<>();

    private CalendarView calendarView;
    private TextView tvFechaSeleccionada;
    private RecyclerView rvCitasDia;
    private FloatingActionButton fabAnadirCita; // Nuevo control

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_agenda, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AgendaViewModel.class);

        // Pedir permiso de notificaciones para Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        calendarView = view.findViewById(R.id.calendarView);
        tvFechaSeleccionada = view.findViewById(R.id.tvFechaSeleccionada);
        rvCitasDia = view.findViewById(R.id.rvCitasDia);
        fabAnadirCita = view.findViewById(R.id.fabAnadirCita); // Instanciamos el botón +

        // Inicializamos el adaptador pasándole la lista vacía Y la acción de borrar
        adapter = new AgendaAdapter(new ArrayList<>(), citaId -> {

            // Cuando el adaptador nos avisa de que han pulsado la papelera y confirmado, llamamos al ViewModel
            viewModel.borrarCita(citaId).observe(getViewLifecycleOwner(), resource -> {
                if (resource.status == api.Resource.Status.SUCCESS) {
                    android.widget.Toast.makeText(getContext(), "Cita borrada", android.widget.Toast.LENGTH_SHORT).show();

                    cargarCitasGlobales();

                } else if (resource.status == api.Resource.Status.ERROR) {
                    android.widget.Toast.makeText(getContext(), "Error al borrar", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
        rvCitasDia.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCitasDia.setAdapter(adapter);

        // Configuramos la fecha de hoy por defecto por si pulsa el botón sin tocar el calendario
        Calendar hoy = Calendar.getInstance();
        String mesStr = String.format("%02d", hoy.get(Calendar.MONTH) + 1);
        String diaStr = String.format("%02d", hoy.get(Calendar.DAY_OF_MONTH));
        fechaSeleccionada = hoy.get(Calendar.YEAR) + "-" + mesStr + "-" + diaStr;
        tvFechaSeleccionada.setText("Citas para hoy: " + fechaSeleccionada);

        cargarCitasGlobales();

        // Control de clics en el Calendario
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String mStr = String.format("%02d", month + 1);
            String dStr = String.format("%02d", dayOfMonth);
            fechaSeleccionada = year + "-" + mStr + "-" + dStr;

            // Actualizamos nuestro objeto Calendar global para las notificaciones
            calendarFechaElegida.set(year, month, dayOfMonth);

            // SOLO mostramos las citas de ese día sin interrumpir con el diálogo
            tvFechaSeleccionada.setText("Citas para: " + fechaSeleccionada);
            filtrarCitasPorDia(fechaSeleccionada);
        });

        // El diálogo solo saltará cuando pulsemos el botón flotante "+"
        fabAnadirCita.setOnClickListener(v -> mostrarDialogoNuevaCita());
    }

    private void cargarCitasGlobales() {
        viewModel.getCitasUsuario().observe(getViewLifecycleOwner(), resource -> {
            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                todasLasCitas = resource.data;
                if (fechaSeleccionada != null) {
                    filtrarCitasPorDia(fechaSeleccionada);
                }
            }
        });
    }

    private void filtrarCitasPorDia(String fecha) {
        List<JsonObject> citasDelDia = new ArrayList<>();
        for (JsonObject cita : todasLasCitas) {
            if (cita.has("fecha") && cita.get("fecha").getAsString().equals(fecha)) {
                citasDelDia.add(cita);
            }
        }
        adapter.setCitas(citasDelDia);
    }

    private void mostrarDialogoNuevaCita() {
        Calendar hoy = Calendar.getInstance();

        if (calendarFechaElegida.before(hoy) && !mismoDia(hoy, calendarFechaElegida)) {
            Toast.makeText(getContext(), "No puedes añadir citas en días pasados", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText etCita = new EditText(getContext());
        etCita.setHint("Ej: Vacuna de la rabia");

        new AlertDialog.Builder(requireContext())
                .setTitle("Motivo de la cita")
                .setMessage("Para el " + fechaSeleccionada)
                .setView(etCita)
                // Cambiamos "Guardar" por "Elegir Hora"
                .setPositiveButton("Elegir Hora", (dialog, which) -> {
                    String titulo = etCita.getText().toString().trim();
                    if (!titulo.isEmpty()) {
                        // Si ha escrito algo, abrimos el reloj
                        abrirSelectorDeHora(titulo);
                    } else {
                        Toast.makeText(getContext(), "El motivo no puede estar vacío", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // NUEVO MÉTODO: Abre el reloj nativo de Android
    private void abrirSelectorDeHora(String titulo) {
        Calendar c = Calendar.getInstance();
        int horaActual = c.get(Calendar.HOUR_OF_DAY);
        int minutoActual = c.get(Calendar.MINUTE);

        android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(getContext(),
                (view, hourOfDay, minute) -> {

                    // --- NUEVA VALIDACIÓN: EVITAR HORAS PASADAS EN EL DÍA DE HOY ---
                    Calendar hoy = Calendar.getInstance();
                    if (mismoDia(hoy, calendarFechaElegida)) {
                        // Si la hora elegida es menor que la actual, o es la misma hora pero menos minutos:
                        if (hourOfDay < hoy.get(Calendar.HOUR_OF_DAY) ||
                                (hourOfDay == hoy.get(Calendar.HOUR_OF_DAY) && minute < hoy.get(Calendar.MINUTE))) {

                            Toast.makeText(getContext(), "No puedes elegir una hora que ya ha pasado", Toast.LENGTH_SHORT).show();
                            return; // Expulsamos al usuario y NO guardamos la cita
                        }
                    }
                    // ----------------------------------------------------------------

                    String horaFormateada = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute);

                    viewModel.crearCita(titulo, fechaSeleccionada, horaFormateada).observe(getViewLifecycleOwner(), res -> {
                        if (res.status == api.Resource.Status.SUCCESS) {
                            programarNotificacion(titulo, calendarFechaElegida, hourOfDay, minute);
                            cargarCitasGlobales();
                            Toast.makeText(getContext(), "Cita guardada a las " + horaFormateada, Toast.LENGTH_SHORT).show();
                        } else if (res.status == api.Resource.Status.ERROR) {
                            Toast.makeText(getContext(), "Error: " + res.message, Toast.LENGTH_LONG).show();
                        }
                    });
                }, horaActual, minutoActual, true);

        timePickerDialog.setTitle("Hora de la cita");
        timePickerDialog.show();
    }

    // MÉTODO ACTUALIZADO: Para las notificaciones
    private void programarNotificacion(String titulo, Calendar fechaElegida, int horaCita, int minutoCita) {
        Calendar aviso = (Calendar) fechaElegida.clone();
        aviso.add(Calendar.DAY_OF_YEAR, -1); // Un día antes
        aviso.set(Calendar.HOUR_OF_DAY, 10);
        aviso.set(Calendar.MINUTE, 0);
        aviso.set(Calendar.SECOND, 0);

        if (aviso.before(Calendar.getInstance())) {
            aviso = (Calendar) fechaElegida.clone();
            aviso.set(Calendar.HOUR_OF_DAY, 9);

            if (aviso.before(Calendar.getInstance())) {
                aviso = Calendar.getInstance();
                aviso.add(Calendar.SECOND, 5);
            }
        }

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), NotificationReceiver.class);
        // Le añadimos la hora al texto de la notificación
        String horaTexto = String.format(java.util.Locale.getDefault(), "%02d:%02d", horaCita, minutoCita);
        intent.putExtra("titulo", "Recordatorio: " + titulo + " a las " + horaTexto);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, aviso.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, aviso.getTimeInMillis(), pendingIntent);
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, aviso.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private boolean mismoDia(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

}