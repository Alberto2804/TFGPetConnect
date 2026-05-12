package api;

import com.google.gson.JsonObject;

import java.util.List;

import data.AuthResponse;
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
}