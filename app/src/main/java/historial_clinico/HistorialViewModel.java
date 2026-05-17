package historial_clinico;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import api.Resource;
import historial_clinico.HistorialMedico;
import historial_clinico.HistorialRepository;

public class HistorialViewModel extends AndroidViewModel {
    private HistorialRepository repository;

    public HistorialViewModel(@NonNull Application application) {
        super(application);
        repository = new HistorialRepository();
    }

    public LiveData<Resource<List<HistorialMedico>>> getHistorial(String token, String mascotaId) {
        return repository.fetchHistorial(token, mascotaId);
    }
    public LiveData<Resource<Void>> crearRegistro(String token, HistorialMedico registro) {
        return repository.insertarRegistro(token, registro);
    }

    public LiveData<Resource<Void>> borrarRegistro(String token, String registroId) {
        return repository.eliminarRegistro(token, registroId);
    }
}
