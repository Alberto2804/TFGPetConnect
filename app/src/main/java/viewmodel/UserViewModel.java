package viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonObject;

import java.io.File;
import java.util.List;

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

    // ==========================================
    // MÉTODOS DEL PERFIL DE USUARIO Y AJUSTES (ORIGINALES)
    // ==========================================

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

    public LiveData<Resource<Void>> actualizarPerfilDB(String nuevoNombre, String urlFoto) {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();
        return userRepository.actualizarDatosUsuario(token, userId, nuevoNombre, urlFoto);
    }

    public LiveData<Resource<String>> subirFotoPerfil(File archivoFoto) {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();
        return userRepository.subirFotoPerfil(token, userId, archivoFoto);
    }

    public LiveData<Resource<Void>> cambiarContrasena(String nuevaContrasena) {
        String token = prefsRepo.getToken();
        return userRepository.actualizarContrasena(token, nuevaContrasena);
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

    // ==========================================
    // MÉTODOS DE LA MASCOTA ACTIVA Y BBDD
    // ==========================================

    public LiveData<Resource<List<JsonObject>>> getMascotas() {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();
        return userRepository.obtenerMascotas(token, userId);
    }

    public void setMascotaActiva(String mascotaId) {
        prefsRepo.guardarMascotaActivaId(mascotaId);
    }

    public String getMascotaActivaId() {
        return prefsRepo.getMascotaActivaId();
    }

    public LiveData<Resource<String>> guardarFotoMascota(String nombreArchivo, File archivoFoto) {
        String token = prefsRepo.getToken();
        return userRepository.subirFotoMascota(token, nombreArchivo, archivoFoto);
    }

    public LiveData<Resource<Void>> guardarMascota(String nombre, String animal, String raza, String fechaNacimiento, String sexo, String peso, String fotoUrl) {
        String token = prefsRepo.getToken();
        String userId = prefsRepo.getUserId();
        return userRepository.crearMascota(token, userId, nombre, animal, raza, fechaNacimiento, sexo, peso, fotoUrl);
    }

    public LiveData<Resource<Void>> editarMascota(String mascotaId, String nombre, String animal, String raza, String fechaNacimiento, String sexo, String peso, String fotoUrl) {
        String token = prefsRepo.getToken();
        return userRepository.actualizarMascota(token, mascotaId, nombre, animal, raza, fechaNacimiento, sexo, peso, fotoUrl);
    }

    public LiveData<Resource<Void>> borrarMascota(String mascotaId) {
        String token = prefsRepo.getToken();
        return userRepository.eliminarMascota(token, mascotaId);
    }

    // ==========================================
    // DATOS ESTÁTICOS Y LÓGICA DE NEGOCIO (MVVM)
    // ==========================================

    public String[] getListaAnimales() {
        return new String[]{"Perro", "Gato", "Pájaro", "Conejo", "Tortuga", "Hámster", "Cobaya"};
    }

    public String[] getListaSexo() {
        return new String[]{"Masculino", "Femenino"};
    }

    public String[] getRazasPorAnimal(String animal) {
        java.util.HashMap<String, String[]> mapaRazas = new java.util.HashMap<>();
        mapaRazas.put("Perro", new String[]{"Mestizo", "Pastor Belga", "Golden Retriever", "Bulldog", "Caniche"});
        mapaRazas.put("Gato", new String[]{"Común Europeo", "Siamés", "Persa", "Sphynx", "Bengalí"});
        mapaRazas.put("Pájaro", new String[]{"Canario", "Periquito", "Agapornis", "Loro", "Ninfa"});
        mapaRazas.put("Conejo", new String[]{"Belier", "Angora", "Toy", "Común"});
        mapaRazas.put("Tortuga", new String[]{"De agua", "De tierra", "Mediterránea"});
        mapaRazas.put("Hámster", new String[]{"Ruso", "Sirio", "Roborowski"});
        mapaRazas.put("Cobaya", new String[]{"Americana", "Peruana", "Abisinia"});

        return mapaRazas.containsKey(animal) ? mapaRazas.get(animal) : new String[]{""};
    }

    public String calcularEdad(String fechaNacimientoStr) {
        if (fechaNacimientoStr == null || fechaNacimientoStr.isEmpty()) return "Edad desconocida";
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date fechaNacimiento = sdf.parse(fechaNacimientoStr);

            java.util.Calendar hoy = java.util.Calendar.getInstance();
            java.util.Calendar nacimiento = java.util.Calendar.getInstance();
            nacimiento.setTime(fechaNacimiento);

            // Calculamos diferencias directas
            int yearDiff = hoy.get(java.util.Calendar.YEAR) - nacimiento.get(java.util.Calendar.YEAR);
            int monthDiff = hoy.get(java.util.Calendar.MONTH) - nacimiento.get(java.util.Calendar.MONTH);
            int dayDiff = hoy.get(java.util.Calendar.DAY_OF_MONTH) - nacimiento.get(java.util.Calendar.DAY_OF_MONTH);

            // Convertimos todo a meses
            int totalMeses = (yearDiff * 12) + monthDiff;

            // Si el día de hoy es anterior al día en que nació, restamos un mes porque no lo ha cumplido entero
            if (dayDiff < 0) {
                totalMeses--;
            }

            // Lógica para mostrar Años o Meses
            if (totalMeses < 12) {
                if (totalMeses <= 0) {
                    return "Menos de 1 mes";
                }
                return totalMeses + (totalMeses == 1 ? " mes" : " meses");
            } else {
                int anios = totalMeses / 12;
                return anios + (anios == 1 ? " año" : " años");
            }

        } catch (Exception e) {
            return "Edad desconocida";
        }
    }

}