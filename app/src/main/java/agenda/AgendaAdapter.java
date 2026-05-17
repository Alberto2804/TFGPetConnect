package agenda;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonObject;
import java.util.List;
import es.iesagora.proyectopetconnect.R; // Asegúrate de que esta importación existe

public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.ViewHolder> {

    private List<JsonObject> listaCitas;

    // 1. NUEVA INTERFAZ PARA EL BORRADO
    public interface OnCitaDeleteListener {
        void onDeleteClick(String citaId);
    }
    private OnCitaDeleteListener deleteListener;

    // 2. CONSTRUCTOR MODIFICADO
    public AgendaAdapter(List<JsonObject> listaCitas, OnCitaDeleteListener listener) {
        this.listaCitas = listaCitas;
        this.deleteListener = listener;
    }

    public void setCitas(List<JsonObject> nuevasCitas) {
        this.listaCitas = nuevasCitas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 3. CAMBIO: Ahora infla tu diseño personalizado en lugar del simple_list_item_2
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agenda, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject cita = listaCitas.get(position);

        holder.tvTitulo.setText(cita.has("titulo") && !cita.get("titulo").isJsonNull() ? cita.get("titulo").getAsString() : "Sin título");

        String hora = cita.has("hora") && !cita.get("hora").isJsonNull() ? cita.get("hora").getAsString() : "--:--";
        String fecha = cita.has("fecha") && !cita.get("fecha").isJsonNull() ? cita.get("fecha").getAsString() : "--/--/----";
        holder.tvSubtitulo.setText(fecha + " - " + hora);

        // 4. PROGRAMAR EL CLIC DE LA PAPELERA
        holder.btnBorrar.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Borrar Cita")
                    .setMessage("¿Quieres eliminar la cita: " + holder.tvTitulo.getText().toString() + "?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        if (deleteListener != null && cita.has("id")) {
                            deleteListener.onDeleteClick(cita.get("id").getAsString());
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listaCitas != null ? listaCitas.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvSubtitulo;
        ImageView btnBorrar; // Añadida la papelera

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 5. CAMBIO: Usamos los IDs de tu item_agenda.xml
            tvTitulo = itemView.findViewById(R.id.tvTituloCita);
            tvSubtitulo = itemView.findViewById(R.id.tvFechaHoraCita);
            btnBorrar = itemView.findViewById(R.id.btnBorrarCita);
        }
    }
}