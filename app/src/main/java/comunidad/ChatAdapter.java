package comunidad;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import comunidad.Mensaje;
import es.iesagora.proyectopetconnect.R;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TIPO_MIO = 1;
    private static final int TIPO_OTRO = 2;

    private List<Mensaje> lista = new ArrayList<>();
    private final String miUserId;

    public ChatAdapter(String miUserId) {
        this.miUserId = miUserId;
    }

    public void setLista(List<Mensaje> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (lista.get(position).getUser_id().equals(miUserId)) {
            return TIPO_MIO;
        }
        return TIPO_OTRO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TIPO_MIO) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensaje_mio, parent, false);
            return new MiViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensaje_otro, parent, false);
            return new OtroViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mensaje m = lista.get(position);

        if (holder.getItemViewType() == TIPO_MIO) {
            ((MiViewHolder) holder).tvTexto.setText(m.getMensaje());
        } else {
            ((OtroViewHolder) holder).tvAutor.setText(m.getUsuario());
            ((OtroViewHolder) holder).tvTexto.setText(m.getMensaje());
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class MiViewHolder extends RecyclerView.ViewHolder {
        TextView tvTexto;
        MiViewHolder(View v) {
            super(v);
            tvTexto = v.findViewById(R.id.tvMensajeMio);
        }
    }

    static class OtroViewHolder extends RecyclerView.ViewHolder {
        TextView tvAutor, tvTexto;
        OtroViewHolder(View v) {
            super(v);
            tvAutor = v.findViewById(R.id.tvNombreOtro);
            tvTexto = v.findViewById(R.id.tvMensajeOtro);
        }
    }
}