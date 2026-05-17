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

    // ==========================================
    // 🧠 LAS COMPUERTAS DE LA CACHÉ (EN MEMORIA RAM)
    // ==========================================
    private List<JsonObject> cacheMascotas = null;
    private JsonObject cachePerfil = null;

    public UserRepository() {
        supabaseAPI = RetrofitClient.getInstance().create(SupabaseAPI.class);
    }

    // ==========================================
    // MÉTODOS DEL PERFIL DE USUARIO
    // ==========================================

    public LiveData<Resource<JsonObject>> obtenerPerfil(String token, String userId) {
        MutableLiveData<Resource<JsonObject>> resultado = new MutableLiveData<>();

        // 🚀 TRUCO CACHÉ: Si ya tenemos el perfil guardado en RAM, lo escupimos YA (0 ms)
        if (cachePerfil != null) {
            resultado.setValue(Resource.success(cachePerfil));
        } else {
            resultado.setValue(Resource.loading(null));
        }

        // De fondo, actualizamos por si acaso ha cambiado algo en la web de Supabase
        supabaseAPI.obtenerUsuario("Bearer " + token, "eq." + userId, "*")
                .enqueue(new Callback<List<JsonObject>>() {
                    @Override
                    public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            cachePerfil = response.body().get(0); // Guardamos en la caché
                            resultado.setValue(Resource.success(cachePerfil)); // Emitimos el dato fresco
                        } else {
                            if (cachePerfil == null) {
                                resultado.setValue(Resource.error("Error al cargar", null));
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                        if (cachePerfil == null) {
                            resultado.setValue(Resource.error(t.getMessage(), null));
                        }
                    }
                });
        return resultado;
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
                    cachePerfil = null; // 🧼 LIMPIAMOS CACHÉ: Forzamos a que se descargue limpio la próxima vez
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
        MutableLiveData<Resource<List<JsonObject>>> resultado = new MutableLiveData<>();

        // 🚀 TRUCO CACHÉ: Si las mascotas ya están en la RAM, las pintamos DE GOLPE (0 ms)
        if (cacheMascotas != null) {
            resultado.setValue(Resource.success(cacheMascotas));
        } else {
            resultado.setValue(Resource.loading(null));
        }

        String filtroUserId = "eq." + userId;
        supabaseAPI.obtenerMascota("Bearer " + token, filtroUserId, "*").enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cacheMascotas = response.body(); // Guardamos la lista en la memoria interna
                    resultado.setValue(Resource.success(cacheMascotas)); // Emitimos los datos frescos
                } else {
                    if (cacheMascotas == null) {
                        resultado.setValue(Resource.success(new java.util.ArrayList<>()));
                    }
                }
            }
            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                if (cacheMascotas == null) {
                    resultado.setValue(Resource.error(t.getMessage(), null));
                }
            }
        });
        return resultado;
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

    public LiveData<Resource<List<JsonObject>>> obtenerTodosLosUsuarios(String token) {
        MutableLiveData<Resource<List<JsonObject>>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        supabaseAPI.obtenerTodosLosUsuarios("Bearer " + token, "*").enqueue(new Callback<List<JsonObject>>() {
            @Override
            public void onResponse(Call<List<JsonObject>> call, Response<List<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    resultado.setValue(Resource.success(response.body()));
                } else {
                    resultado.setValue(Resource.error("Error al obtener la lista de usuarios", null));
                }
            }
            @Override
            public void onFailure(Call<List<JsonObject>> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<Void>> eliminarUsuarioAdmin(String token, String usuarioId) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        supabaseAPI.borrarUsuarioDB("Bearer " + token, "eq." + usuarioId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    resultado.setValue(Resource.success(null));
                } else {
                    resultado.setValue(Resource.error("No se pudo eliminar al usuario", null));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }
}