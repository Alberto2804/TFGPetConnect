package auth;

import com.google.gson.annotations.SerializedName;

import data.SupabaseUser;

public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("user")
    private SupabaseUser user;

    public String getAccessToken() { return accessToken; }
    public SupabaseUser getUser() { return user; }
    public String getTokenType() { return tokenType; }

}
