package api;

import com.google.gson.JsonObject;

import java.util.List;

import auth.AuthResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseAPI {

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> signIn(@Body JsonObject credenciales);

    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body JsonObject credenciales);

    // Añade esto dentro de SupabaseAPI
    @POST("auth/v1/token?grant_type=id_token")
    Call<AuthResponse> loginWithGoogleToken(
            @Body com.google.gson.JsonObject body
    );

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/usuarios")
    Call<Void> crearUsuarioDB(
            @Header("Authorization") String token,
            @Body JsonObject datosUsuario
    );



    @Headers("Prefer: return=minimal")
    @PATCH("rest/v1/usuarios")
    Call<Void> actualizarUsuarioDB(
            @Header("Authorization") String token,
            @Query("id") String idFiltro,
            @Body JsonObject datosActualizados
    );


    @Multipart
    @Headers("x-upsert: true")
    @POST("storage/v1/object/{bucket}/{fileName}")
    Call<Void> subirImagenPerfil(
            @Header("Authorization") String token,
            @Path("bucket") String bucket,
            @Path("fileName") String fileName,
            @Part MultipartBody.Part file
    );

    @GET("rest/v1/usuarios")
    Call<List<JsonObject>> obtenerUsuario(
            @Header("Authorization") String token,
            @Query("id") String filtroId,
            @Query("select") String columnas
    );

    @GET("rest/v1/mascotas")
    Call<java.util.List<com.google.gson.JsonObject>> obtenerMascota(
            @Header("Authorization") String token,
            @Query("user_id") String filtroUserId,
            @Query("select") String columnas
    );


    // --- CHAT DE COMUNIDAD ---
    @retrofit2.http.GET("rest/v1/comunidad?select=*&order=created_at.desc")
    retrofit2.Call<java.util.List<comunidad.Mensaje>> getMensajesChat(
            @retrofit2.http.Header("Authorization") String token,
            @retrofit2.http.Query("limit") int limite // <--- Añadimos el límite aquí
    );

    @retrofit2.http.POST("rest/v1/comunidad")
    retrofit2.Call<Void> enviarMensajeChat(
            @retrofit2.http.Header("Authorization") String token,
            @retrofit2.http.Body comunidad.Mensaje mensaje
    );

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/mascotas")
    Call<Void> crearMascotaDB(
            @Header("Authorization") String token,
            @Body com.google.gson.JsonObject datosMascota
    );


    @Headers("Prefer: return=minimal")
    @PATCH("rest/v1/mascotas")
    Call<Void> actualizarMascotaDB(
            @Header("Authorization") String token,
            @Query("user_id") String filtroUserId,
            @Body com.google.gson.JsonObject datosActualizados
    );

    @GET("rest/v1/historial_clinico")
    Call<List<historial_clinico.HistorialMedico>> obtenerHistorialMascota(
            @Header("Authorization") String token,
            @Query("mascota_id") String filtroMascotaId,
            @Query("select") String columnas
    );

    @Headers("Prefer: return=minimal")
    @POST("rest/v1/historial_clinico")
    Call<Void> insertarRegistroMedico(
            @Header("Authorization") String token,
            @Body historial_clinico.HistorialMedico nuevoRegistro
    );

    @POST("rest/v1/citas")
    Call<Void> crearCitaDB(@Header("Authorization") String token, @Body com.google.gson.JsonObject citaData);

    @GET("rest/v1/citas")
    Call<java.util.List<com.google.gson.JsonObject>> obtenerCitasUsuario(@Header("Authorization") String token, @Query("user_id") String userId);
}