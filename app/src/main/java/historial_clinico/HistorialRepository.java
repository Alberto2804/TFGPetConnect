package historial_clinico;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;
import api.Resource;
import api.RetrofitClient;
import historial_clinico.HistorialMedico;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistorialRepository {

    private List<HistorialMedico> cacheHistorial = null;
    private String cachedMascotaId = null;

    public LiveData<Resource<List<HistorialMedico>>> fetchHistorial(String token, String mascotaId) {
        MutableLiveData<Resource<List<HistorialMedico>>> data = new MutableLiveData<>();

        // 🚀 TRUCO CACHÉ: Solo devolvemos la caché si coincide exactamente con la mascota activa actual
        if (cacheHistorial != null && mascotaId != null && mascotaId.equals(cachedMascotaId)) {
            data.setValue(Resource.success(cacheHistorial));
        } else {
            data.setValue(Resource.loading(null));
        }

        RetrofitClient.getApi().obtenerHistorialMascota(token, "eq." + mascotaId, "*")
                .enqueue(new Callback<List<HistorialMedico>>() {
                    @Override
                    public void onResponse(Call<List<HistorialMedico>> call, Response<List<HistorialMedico>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            cacheHistorial = response.body();   // Guardamos los registros
                            cachedMascotaId = mascotaId;        // Guardamos a qué mascota pertenecen
                            data.setValue(Resource.success(cacheHistorial));
                        } else {
                            if (cacheHistorial == null) {
                                data.setValue(Resource.error("Error al cargar historial", null));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<HistorialMedico>> call, Throwable t) {
                        if (cacheHistorial == null) {
                            data.setValue(Resource.error(t.getMessage(), null));
                        }
                    }
                });
        return data;
    }

    public LiveData<Resource<Void>> insertarRegistro(String token, HistorialMedico registro) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        RetrofitClient.getApi().insertarRegistroMedico(token, registro).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cacheHistorial = null; // 🧼 LIMPIAMOS CACHÉ: Hay una vacuna/visita nueva
                    cachedMascotaId = null;
                    resultado.postValue(Resource.success(null));
                } else {
                    resultado.postValue(Resource.error("Error al guardar", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.postValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<Void>> eliminarRegistro(String token, String registroId) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        RetrofitClient.getApi().borrarRegistroHistorialDB(token, "eq." + registroId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cacheHistorial = null; // 🧼 LIMPIAMOS CACHÉ: Registro eliminado del historial
                    cachedMascotaId = null;
                    resultado.postValue(Resource.success(null));
                } else {
                    resultado.postValue(Resource.error("Error al borrar el registro", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.postValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }
}