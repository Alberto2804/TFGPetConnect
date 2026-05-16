package agenda;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonObject;
import java.util.List;

public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.ViewHolder> {
    private List<JsonObject> listaCitas;

    public AgendaAdapter(List<JsonObject> listaCitas) {
        this.listaCitas = listaCitas;
    }

    public void setCitas(List<JsonObject> nuevasCitas) {
        this.listaCitas = nuevasCitas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JsonObject cita = listaCitas.get(position);
        holder.tvTitulo.setText(cita.get("titulo").getAsString());
        holder.tvSubtitulo.setText("Hora: " + cita.get("hora").getAsString());
    }

    @Override
    public int getItemCount() {
        return listaCitas != null ? listaCitas.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvSubtitulo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(android.R.id.text1);
            tvSubtitulo = itemView.findViewById(android.R.id.text2);
        }
    }
}