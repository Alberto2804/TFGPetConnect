package repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.JsonObject;
import java.io.File;
import java.util.List;
import api.Resource;
import api.RetrofitClient;
import api.SupabaseAPI;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {

    private final SupabaseAPI supabaseAPI;

    // 1. LOS LIVEDATA AHORA SON GLOBALES Y ÚNICOS
    private final MutableLiveData<Resource<JsonObject>> perfilLiveData = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<JsonObject>>> mascotasLiveData = new MutableLiveData<>();

    private List<JsonObject> cacheMascotas = null;
    private JsonObject cachePerfil = null;

    public UserRepository() {
        supabaseAPI = RetrofitClient.getInstance().create(SupabaseAPI.class);
    }

    // ==========================================
    // MÉTODOS DEL PERFIL DE USUARIO
    // ==========================================

    public LiveData<Resource<JsonObject>> obtenerPerfil(String token, String userId) {
        // 2. SI YA TENEMOS LA CACHÉ, LA DEVOLVEMOS Y CORTAMOS LA EJECUCIÓN (Carga en 0ms)
        if (cachePerfil != null) {
            perfilLiveData.setValue(Resource.success(cachePerfil));
            return perfilLiveData;
        }

        perfilLiveData.setValue(Resource.loading(null));

        supabaseAPI.obtenerUsuario("Bearer " + token, "eq." + userId, "*")
                .enqueue(new Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            cachePerfil = response.body().get(0);
                            perfilLiveData.setValue(Resource.success(cachePerfil));
                        } else {
                            perfilLiveData.setValue(Resource.error("Error al cargar", null));
                        }
                    }
                    @Override
                    public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                        perfilLiveData.setValue(Resource.error(t.getMessage(), null));
                    }
                });
        return perfilLiveData;
    }

    public LiveData<Resource<Void>> actualizarDatosUsuario(String token, String userId, String nuevoNombre, String urlFoto) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        JsonObject jsonUpdate = new JsonObject();
        if (nuevoNombre != null && !nuevoNombre.isEmpty()) jsonUpdate.addProperty("usuario", nuevoNombre);
        if (urlFoto != null && !urlFoto.isEmpty()) jsonUpdate.addProperty("foto_url", urlFoto);

        supabaseAPI.actualizarUsuarioDB("Bearer " + token, "eq." + userId, jsonUpdate).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cachePerfil = null; // Limpiamos caché
                    obtenerPerfil(token, userId); // 3. FORZAMOS RECARGA SILENCIOSA PARA QUE LA UI SE ACTUALICE SOLA
                    resultado.setValue(Resource.success(null));
                } else {
                    resultado.setValue(Resource.error("Error al actualizar la base de datos", null));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<String>> subirFotoPerfil(String token, String userId, File archivoFoto) {
        MutableLiveData<Resource<String>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), archivoFoto);
        String nombreArchivo = userId + ".jpg";
        MultipartBody.Part bodySubeImagen = MultipartBody.Part.createFormData("file", nombreArchivo, requestFile);

        supabaseAPI.subirImagenPerfil("Bearer " + token, "avatares", nombreArchivo, bodySubeImagen).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String urlPublica = "https://evrsywohqxoehdnbhpkg.supabase.co/storage/v1/object/public/avatares/" + nombreArchivo + "?t=" + System.currentTimeMillis();
                    cachePerfil = null; // 🧼 Limpiamos caché para actualizar la foto del perfil
                    resultado.setValue(Resource.success(urlPublica));
                } else {
                    resultado.setValue(Resource.error("Error al subir foto al Storage", null));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    // ==========================================
    // MÉTODOS DE MASCOTAS
    // ==========================================

    public LiveData<Resource<List<JsonObject>>> obtenerMascotas(String token, String userId) {
        // SI YA TENEMOS CACHÉ, NO LLAMAMOS A INTERNET
        if (cacheMascotas != null) {
            mascotasLiveData.setValue(Resource.success(cacheMascotas));
            return mascotasLiveData;
        }

        mascotasLiveData.setValue(Resource.loading(null));

        String filtroUserId = "eq." + userId;
        supabaseAPI.obtenerMascota("Bearer " + token, filtroUserId, "*").enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cacheMascotas = response.body();
                    mascotasLiveData.setValue(Resource.success(cacheMascotas));
                } else {
                    mascotasLiveData.setValue(Resource.success(new java.util.ArrayList<>()));
                }
            }
            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                mascotasLiveData.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return mascotasLiveData;
    }

    public LiveData<Resource<String>> subirFotoMascota(String token, String nombreArchivo, File archivoFoto) {
        MutableLiveData<Resource<String>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), archivoFoto);
        MultipartBody.Part bodySubeImagen = MultipartBody.Part.createFormData("file", nombreArchivo, requestFile);

        supabaseAPI.subirImagenPerfil("Bearer " + token, "mascotas", nombreArchivo, bodySubeImagen).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    String urlPublica = "https://evrsywohqxoehdnbhpkg.supabase.co/storage/v1/object/public/mascotas/" + nombreArchivo;
                    resultado.setValue(Resource.success(urlPublica));
                } else {
                    resultado.setValue(Resource.error("Error al subir foto al Storage", null));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<Void>> crearMascota(String token, String userId, String nombre, String animal, String raza, String fechaNacimiento, String sexo, String peso, String fotoUrl) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        JsonObject jsonMascota = new JsonObject();
        jsonMascota.addProperty("user_id", userId);
        jsonMascota.addProperty("nombre", nombre);
        jsonMascota.addProperty("animal", animal);
        jsonMascota.addProperty("raza", raza);
        jsonMascota.addProperty("fecha_nacimiento", fechaNacimiento);
        jsonMascota.addProperty("sexo", sexo);

        if (peso != null && !peso.trim().isEmpty()) {
            try { jsonMascota.addProperty("peso", Double.parseDouble(peso.replace(",", "."))); } catch (NumberFormatException ignored) {}
        }
        if (fotoUrl != null) jsonMascota.addProperty("foto_url", fotoUrl);

        supabaseAPI.crearMascotaDB("Bearer " + token, jsonMascota).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cacheMascotas = null; // 🧼 LIMPIAMOS CACHÉ: Al crear un perro, obligamos a recargar la lista de Supabase
                    resultado.setValue(Resource.success(null));
                } else {
                    resultado.setValue(Resource.error("Error al guardar mascota", null));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<Void>> actualizarMascota(String token, String mascotaId, String nombre, String animal, String raza, String fechaNacimiento, String sexo, String peso, String fotoUrl) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        JsonObject jsonUpdate = new JsonObject();
        jsonUpdate.addProperty("nombre", nombre);
        jsonUpdate.addProperty("animal", animal);
        jsonUpdate.addProperty("raza", raza);
        jsonUpdate.addProperty("fecha_nacimiento", fechaNacimiento);
        jsonUpdate.addProperty("sexo", sexo);

        if (peso != null && !peso.trim().isEmpty()) {
            try { jsonUpdate.addProperty("peso", Double.parseDouble(peso.replace(",", "."))); } catch (NumberFormatException ignored) {}
        }
        if (fotoUrl != null) jsonUpdate.addProperty("foto_url", fotoUrl);

        String filtroMascotaId = "eq." + mascotaId;

        supabaseAPI.actualizarMascotaDB("Bearer " + token, filtroMascotaId, jsonUpdate).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cacheMascotas = null; // 🧼 LIMPIAMOS CACHÉ: Al editar un perro, invalidamos la lista vieja
                    resultado.setValue(Resource.success(null));
                } else {
                    resultado.setValue(Resource.error("Error al actualizar datos", null));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<Void>> eliminarMascota(String token, String mascotaId) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        supabaseAPI.borrarMascotaDB("Bearer " + token, "eq." + mascotaId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    cacheMascotas = null; // 🧼 LIMPIAMOS CACHÉ: Borrado con éxito, la lista cambia por completo
                    resultado.setValue(Resource.success(null));
                } else {
                    resultado.setValue(Resource.error("Error al borrar la mascota", null));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<Void>> actualizarContrasena(String token, String nuevaContrasena) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        JsonObject jsonAuth = new JsonObject();
        jsonAuth.addProperty("password", nuevaContrasena);

        supabaseAPI.cambiarContrasenaAuth("Bearer " + token, jsonAuth).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) resultado.setValue(Resource.success(null));
                else resultado.setValue(Resource.error("Error al cambiar contraseña. Minimo 6 caracteres.", null));
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }
}