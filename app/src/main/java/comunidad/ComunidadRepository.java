package comunidad;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.Collections;
import java.util.List;
import api.Resource;
import api.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComunidadRepository {

    private final MutableLiveData<Resource<List<Mensaje>>> mensajesLiveData = new MutableLiveData<>();
    private Handler listenerHandler;
    private Runnable listenerRunnable;
    private boolean isListening = false;

    // Controlamos el límite (empezamos por 50)
    private int limiteActual = 50;

    public void aumentarLimite() {
        this.limiteActual += 50; // Cada vez que suba, cargamos 50 más
    }

    public LiveData<Resource<List<Mensaje>>> obtenerMensajes(String token) {
        if (listenerHandler == null) {
            mensajesLiveData.setValue(Resource.loading(null));
            listenerHandler = new Handler(Looper.getMainLooper());
            isListening = true;

            listenerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isListening) {
                        // Enviamos el limiteActual a la API
                        RetrofitClient.getApi().getMensajesChat(token, limiteActual).enqueue(new Callback<List<Mensaje>>() {
                            @Override
                            public void onResponse(Call<List<Mensaje>> call, Response<List<Mensaje>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    List<Mensaje> lista = response.body();
                                    Collections.reverse(lista); // El más nuevo abajo
                                    mensajesLiveData.postValue(Resource.success(lista));
                                }
                            }
                            @Override
                            public void onFailure(Call<List<Mensaje>> call, Throwable t) {}
                        });
                        listenerHandler.postDelayed(this, 2000);
                    }
                }
            };
            listenerHandler.post(listenerRunnable);
        }
        return mensajesLiveData;
    }

    public void agregarMensaje(String token, Mensaje mensaje) {
        RetrofitClient.getApi().enviarMensajeChat(token, mensaje).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    public void stopListening() {
        isListening = false;
        if (listenerHandler != null && listenerRunnable != null) {
            listenerHandler.removeCallbacks(listenerRunnable);
            listenerHandler = null;
        }
    }
}