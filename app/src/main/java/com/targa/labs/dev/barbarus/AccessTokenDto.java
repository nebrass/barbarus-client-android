package com.targa.labs.dev.barbarus;

public class AccessTokenDto {
    private String accessToken;
    private String refreshToken;

    public AccessTokenDto() {
    }

    public AccessTokenDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
