package api;

import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SupabaseInterceptor implements Interceptor {

    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImV2cnN5d29ocXhvZWhkbmJocGtnIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzA4ODYzNzIsImV4cCI6MjA4NjQ2MjM3Mn0.o_Ut41RpIit6jTHi8u0LHfQgFqd-SPFpOrhUkkuvjRA";

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header("apikey", API_KEY)
                .header("Content-Type", "application/json");


        Request request = builder.build();
        return chain.proceed(request);
    }
}
