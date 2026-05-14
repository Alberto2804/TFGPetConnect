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

    public LiveData<Resource<List<HistorialMedico>>> fetchHistorial(String token, String mascotaId) {
        MutableLiveData<Resource<List<HistorialMedico>>> data = new MutableLiveData<>();
        data.setValue(Resource.loading(null));

        RetrofitClient.getApi().obtenerHistorialMascota(token, "eq." + mascotaId, "*")
                .enqueue(new Callback<List<HistorialMedico>>() {
                    @Override
                    public void onResponse(Call<List<HistorialMedico>> call, Response<List<HistorialMedico>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            data.setValue(Resource.success(response.body()));
                        } else {
                            data.setValue(Resource.error("Error al cargar historial", null));
                        }
                    }

                    @Override
                    public void onFailure(Call<List<HistorialMedico>> call, Throwable t) {
                        data.setValue(Resource.error(t.getMessage(), null));
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
}