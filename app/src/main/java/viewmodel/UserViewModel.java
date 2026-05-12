package viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonObject;

import java.io.File;

import api.Resource;
import repository.UserRepository;
import sharedpreferences.PreferencesRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final PreferencesRepository prefsRepo;

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository();
        prefsRepo = new PreferencesRepository(application);
    }
    public LiveData<Resource<JsonObject>> getPerfilUsuario() {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();

        if (token.isEmpty() || userId.isEmpty()) {
            MutableLiveData<Resource<JsonObject>> error = new MutableLiveData<>();
            error.setValue(Resource.error("Sesión no válida", null));
            return error;
        }

        return userRepository.obtenerPerfil(token, userId);
    }
    public LiveData<Resource<Void>> actualizarPerfil(String nuevoNombre, File archivoFoto) {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();

        if (token.isEmpty() || userId.isEmpty()) {
            MutableLiveData<Resource<Void>> error = new MutableLiveData<>();
            error.setValue(Resource.error("Sesión no válida", null));
            return error;
        }

        if (archivoFoto != null) {

            return userRepository.subirFotoPerfil(token, userId, archivoFoto);
        } else {

            return userRepository.actualizarNombre(token, userId, nuevoNombre);
        }
    }

    public LiveData<Resource<Void>> actualizarSoloNombre(String nuevoNombre) {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();
        return userRepository.actualizarNombre(token, userId, nuevoNombre);
    }

    public LiveData<Resource<com.google.gson.JsonObject>> getMascota() {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();
        return userRepository.obtenerMascota(token, userId);
    }

    public LiveData<Resource<Void>> guardarMascota(String nombre, String raza, String edad) {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();
        return userRepository.crearMascota(token, userId, nombre, raza, edad);
    }

    public LiveData<Resource<Void>> guardarFotoMascota(File archivoFoto) {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();
        return userRepository.subirFotoMascota(token, userId, archivoFoto);
    }


    public LiveData<Resource<Void>> editarMascota(String nombre, String raza, String edad) {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();
        return userRepository.actualizarMascota(token, userId, nombre, raza, edad);
    }

    public void setModoOscuro(boolean isOscuro) {
        prefsRepo.guardarModoOscuro(isOscuro);
    }

    public boolean isModoOscuro() {
        return prefsRepo.isModoOscuro();
    }

    public void hacerLogout() {
        prefsRepo.cerrarSesion();
    }
}