package api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.google.gson.JsonObject;
import retrofit2.Call;

public class WeatherClient {

    public interface WeatherAPI {
        @GET("v1/forecast")
        Call<JsonObject> getCurrentWeather(
                @Query("latitude") double lat,
                @Query("longitude") double lon,
                @Query("current_weather") boolean current
        );
    }

    public static WeatherAPI getApi() {
        return new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherAPI.class);
    }
}