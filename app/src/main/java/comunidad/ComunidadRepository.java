package comunidad;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import api.Resource;
import api.RetrofitClient;
import comunidad.Mensaje;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComunidadRepository {

    private final MutableLiveData<Resource<List<Mensaje>>> mensajesLiveData = new MutableLiveData<>();
    private Handler listenerHandler;
    private Runnable listenerRunnable;
    private boolean isListening = false;

    // Simula el addSnapshotListener de Firestore
    public LiveData<Resource<List<Mensaje>>> obtenerMensajes(String token) {
        if (listenerHandler == null) {
            mensajesLiveData.setValue(Resource.loading(null));
            listenerHandler = new Handler(Looper.getMainLooper());
            isListening = true;

            listenerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isListening) {
                        RetrofitClient.getApi().getMensajesChat(token).enqueue(new Callback<List<Mensaje>>() {
                            @Override
                            public void onResponse(Call<List<Mensaje>> call, Response<List<Mensaje>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    mensajesLiveData.postValue(Resource.success(response.body()));
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Mensaje>> call, Throwable t) {
                                // Falla silenciosamente en el polling igual que el error de Firestore
                            }
                        });
                        // Se recarga cada 2 segundos (Tiempo real)
                        listenerHandler.postDelayed(this, 2000);
                    }
                }
            };
            listenerHandler.post(listenerRunnable);
        }
        return mensajesLiveData;
    }

    // Simula el db.collection("...").add(c)
    public void agregarMensaje(String token, Mensaje mensaje) {
        RetrofitClient.getApi().enviarMensajeChat(token, mensaje).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // Si tienes un LiveData de estado de envío podrías actualizarlo aquí
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    // Simula el listener.remove()
    public void stopListening() {
        isListening = false;
        if (listenerHandler != null && listenerRunnable != null) {
            listenerHandler.removeCallbacks(listenerRunnable);
            listenerHandler = null;
        }
    }
}