package agenda;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.JsonObject;
import java.util.List;
import api.Resource;
import api.RetrofitClient;
import api.SupabaseAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgendaRepository {
    private final SupabaseAPI api;

    public AgendaRepository() {
        api = RetrofitClient.getInstance().create(SupabaseAPI.class);
    }

    public LiveData<Resource<Void>> guardarCita(String token, JsonObject cita) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        api.crearCitaDB("Bearer " + token, cita).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) resultado.setValue(Resource.success(null));
                else resultado.setValue(Resource.error("Error al guardar cita", null));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error("Fallo de red", null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<List<JsonObject>>> obtenerCitas(String token, String userId) {
        MutableLiveData<Resource<List<JsonObject>>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        api.obtenerCitasUsuario("Bearer " + token, "eq." + userId).enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    resultado.setValue(Resource.success(response.body()));
                } else {
                    resultado.setValue(Resource.error("Error al obtener citas", null));
                }
            }
            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                resultado.setValue(Resource.error("Fallo de red", null));
            }
        });
        return resultado;
    }
}