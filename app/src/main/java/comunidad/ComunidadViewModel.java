package comunidad;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import api.Resource;
import comunidad.Mensaje;
import comunidad.ComunidadRepository;

public class ComunidadViewModel extends AndroidViewModel {

    private final ComunidadRepository repository;

    public ComunidadViewModel(@NonNull Application application) {
        super(application);
        repository = new ComunidadRepository();
    }

    public LiveData<Resource<List<Mensaje>>> getMensajes(String token) {
        return repository.obtenerMensajes(token);
    }

    public void agregarMensaje(String token, Mensaje mensaje) {
        repository.agregarMensaje(token, mensaje);
    }

    public void cargarMasMensajes() {
        repository.aumentarLimite();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListening();
    }

    public void pausarListener() {
        repository.stopListening();
    }
}