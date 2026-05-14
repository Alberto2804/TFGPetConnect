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
        }
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo, tvFecha, tvDesc;
        ImageView ivIcono;
        ViewHolder(View v) {
            super(v);
            tvTipo = v.findViewById(R.id.tvTipoRegistro);
            tvFecha = v.findViewById(R.id.tvFechaRegistro);
            tvDesc = v.findViewById(R.id.tvDescripcionRegistro);
            ivIcono = v.findViewById(R.id.ivIconoMedico);
        }
    }
}