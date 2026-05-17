package historial_clinico;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import es.iesagora.proyectopetconnect.R;
import historial_clinico.HistorialMedico;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private List<HistorialMedico> lista = new ArrayList<>();

    // 1. NUEVA INTERFAZ PARA AVISAR AL FRAGMENTO
    public interface OnRegistroDeleteListener {
        void onDeleteClick(String registroId);
    }
    private OnRegistroDeleteListener deleteListener;

    // 2. NUEVO CONSTRUCTOR PARA RECIBIR EL LISTENER
    public HistorialAdapter(OnRegistroDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setLista(List<HistorialMedico> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial_medico, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistorialMedico h = lista.get(position);
        holder.tvTipo.setText(h.getTipo().toUpperCase());
        holder.tvFecha.setText(h.getFecha());
        holder.tvDesc.setText(h.getDescripcion());

        // Lógica de iconos dinámicos
        switch (h.getTipo()) {
            case "Vacuna": holder.ivIcono.setImageResource(R.drawable.ic_vacuna); break;
            case "Cirugía": holder.ivIcono.setImageResource(R.drawable.ic_cirugia); break;
            case "Medicación": holder.ivIcono.setImageResource(R.drawable.ic_medicacion); break;
            case "Desparasitación": holder.ivIcono.setImageResource(R.drawable.ic_desparasitacion); break;
            case "Visita": holder.ivIcono.setImageResource(R.drawable.ic_visita); break;
            default: holder.ivIcono.setImageResource(R.drawable.ic_visita); break; // Por si acaso
        }

        // 3. PROGRAMAR EL CLICK DE LA PAPELERA
        holder.btnBorrar.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Borrar Registro")
                    .setMessage("¿Eliminar este registro médico de forma permanente?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        if (deleteListener != null) {
                            deleteListener.onDeleteClick(String.valueOf(h.getId()));
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvFecha, tvDesc;
        ImageView ivIcono, btnBorrar; // Añadido el btnBorrar

        ViewHolder(View v) {
            super(v);
            tvTipo = v.findViewById(R.id.tvTipoRegistro);
            tvFecha = v.findViewById(R.id.tvFechaRegistro);
            tvDesc = v.findViewById(R.id.tvDescripcionRegistro);
            ivIcono = v.findViewById(R.id.ivIconoMedico);
            // 4. VINCULAMOS LA PAPELERA DEL XML
            btnBorrar = v.findViewById(R.id.btnBorrarRegistro);
        }
    }
}