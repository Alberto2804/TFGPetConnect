package agenda;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.google.gson.JsonObject;
import java.util.List;
import api.Resource;
import sharedpreferences.PreferencesRepository;

public class AgendaViewModel extends AndroidViewModel {
    private final AgendaRepository repository;
    private final PreferencesRepository prefs;

    public AgendaViewModel(@NonNull Application application) {
        super(application);
        repository = new AgendaRepository();
        prefs = new PreferencesRepository(application);
    }

    public LiveData<Resource<Void>> crearCita(String titulo, String fecha, String hora) {
        JsonObject json = new JsonObject();
        json.addProperty("user_id", prefs.getUserId());
        json.addProperty("titulo", titulo);
        json.addProperty("fecha", fecha);
        json.addProperty("hora", hora);
        return repository.guardarCita(prefs.getToken(), json);
    }

    public LiveData<Resource<List<JsonObject>>> getCitasUsuario() {
        return repository.obtenerCitas(prefs.getToken(), prefs.getUserId());
    }
}