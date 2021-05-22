package com.targa.labs.dev.barbarus;

public class BarbarusLoginDto {
    private String username;

    private String password;

    private String viewId;

    private String loginUrl;

    public BarbarusLoginDto() {
    }

    public BarbarusLoginDto(String username, String password, String viewId, String loginUrl) {
        this.username = username;
        this.password = password;
        this.viewId = viewId;
        this.loginUrl = loginUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }
}
