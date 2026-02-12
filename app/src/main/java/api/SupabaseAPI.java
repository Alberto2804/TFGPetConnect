package api;
import com.google.gson.JsonObject;

import data.AuthResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
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
}

