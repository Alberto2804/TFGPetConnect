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

    public UserRepository() {
        supabaseAPI = RetrofitClient.getInstance().create(SupabaseAPI.class);
    }
    public LiveData<Resource<com.google.gson.JsonObject>> obtenerPerfil(String token, String userId) {
        MutableLiveData<Resource<com.google.gson.JsonObject>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));
        supabaseAPI.obtenerUsuario("Bearer " + token, "eq." + userId, "*").enqueue(new retrofit2.Callback<java.util.List<com.google.gson.JsonObject>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call, retrofit2.Response<java.util.List<com.google.gson.JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    resultado.setValue(Resource.success(response.body().get(0)));
                } else { resultado.setValue(Resource.error("Error al cargar", null)); }
            }
            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;

    }
    public LiveData<Resource<Void>> actualizarNombre(String token, String userId, String nuevoNombreUsuario) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        JsonObject jsonUpdate = new JsonObject();
        jsonUpdate.addProperty("usuario", nuevoNombreUsuario);

        String filtroId = "eq." + userId;

        supabaseAPI.actualizarUsuarioDB("Bearer " + token, filtroId, jsonUpdate).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    resultado.setValue(Resource.success(null));
                } else {
                    resultado.setValue(Resource.error("Error al actualizar el usuario en Supabase", null));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<Void>> subirFotoPerfil(String token, String userId, File archivoFoto) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), archivoFoto);
        MultipartBody.Part bodySubeImagen = MultipartBody.Part.createFormData("file", archivoFoto.getName(), requestFile);

        String nombreArchivo = userId + ".jpg";

        supabaseAPI.subirImagenPerfil("Bearer " + token, "avatares", nombreArchivo, bodySubeImagen).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    resultado.setValue(Resource.success(null));
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

    public LiveData<Resource<com.google.gson.JsonObject>> obtenerMascota(String token, String userId) {
        MutableLiveData<Resource<com.google.gson.JsonObject>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        String filtroUserId = "eq." + userId;

        supabaseAPI.obtenerMascota("Bearer " + token, filtroUserId, "*").enqueue(new retrofit2.Callback<java.util.List<com.google.gson.JsonObject>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call, retrofit2.Response<java.util.List<com.google.gson.JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    resultado.setValue(Resource.success(response.body().get(0)));
                } else {
                    resultado.setValue(Resource.success(null));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.google.gson.JsonObject>> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<Void>> crearMascota(String token, String userId, String nombre, String raza, String edad) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        com.google.gson.JsonObject jsonMascota = new com.google.gson.JsonObject();
        jsonMascota.addProperty("user_id", userId);
        jsonMascota.addProperty("nombre", nombre);
        jsonMascota.addProperty("raza", raza);
        jsonMascota.addProperty("edad", edad);

        supabaseAPI.crearMascotaDB("Bearer " + token, jsonMascota).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    resultado.setValue(Resource.success(null));
                } else {
                    resultado.setValue(Resource.error("Error al guardar mascota", null));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }

    public LiveData<Resource<Void>> subirFotoMascota(String token, String userId, File archivoFoto) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), archivoFoto);
        MultipartBody.Part bodySubeImagen = MultipartBody.Part.createFormData("file", archivoFoto.getName(), requestFile);


        String nombreArchivo = userId + ".jpg";

        supabaseAPI.subirImagenPerfil("Bearer " + token, "mascotas", nombreArchivo, bodySubeImagen).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    resultado.setValue(Resource.success(null));
                } else {
                    resultado.setValue(Resource.error("Error Supabase (Código " + response.code() + ")", null));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }


    public LiveData<Resource<Void>> actualizarMascota(String token, String userId, String nombre, String raza, String edad) {
        MutableLiveData<Resource<Void>> resultado = new MutableLiveData<>();
        resultado.setValue(Resource.loading(null));

        com.google.gson.JsonObject jsonUpdate = new com.google.gson.JsonObject();
        jsonUpdate.addProperty("nombre", nombre);
        jsonUpdate.addProperty("raza", raza);
        jsonUpdate.addProperty("edad", edad);


        String filtroUserId = "eq." + userId;

        supabaseAPI.actualizarMascotaDB("Bearer " + token, filtroUserId, jsonUpdate).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    resultado.setValue(Resource.success(null));
                } else {
                    resultado.setValue(Resource.error("Error al actualizar datos: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                resultado.setValue(Resource.error(t.getMessage(), null));
            }
        });
        return resultado;
    }



}