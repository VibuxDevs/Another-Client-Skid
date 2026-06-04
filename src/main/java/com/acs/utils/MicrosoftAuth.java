package com.acs.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MicrosoftAuth {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String CLIENT_ID = "c36a9fb6-4f2a-41ff-90bd-ae7cc92031eb";

    public static class DeviceCodeInfo {
        public String userCode;
        public String deviceCode;
        public String verificationUri;
        public int interval;
    }

    public static class LoginResult {
        public String username;
        public String uuid;
        public String token;
        public String refreshToken;
        public String error;
    }

    public static DeviceCodeInfo getDeviceCode() throws Exception {
        String body = "client_id=" + CLIENT_ID + "&scope=" + java.net.URLEncoder.encode("XboxLive.signin offline_access", java.nio.charset.StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String resBody = response.body();

        if (resBody.contains("error")) {
            String errDesc = getJsonValue(resBody, "error_description");
            if (errDesc == null) errDesc = getJsonValue(resBody, "error");
            throw new RuntimeException(errDesc != null ? errDesc : "Failed to get device code");
        }

        DeviceCodeInfo info = new DeviceCodeInfo();
        info.userCode = getJsonValue(resBody, "user_code");
        info.deviceCode = getJsonValue(resBody, "device_code");
        info.verificationUri = getJsonValue(resBody, "verification_uri");
        String intervalStr = getJsonValue(resBody, "interval");
        info.interval = intervalStr != null ? Integer.parseInt(intervalStr) : 5;

        return info;
    }

    public static LoginResult loginWithRefreshToken(String refreshToken) {
        LoginResult result = new LoginResult();
        try {
            String body = "grant_type=refresh_token" +
                          "&client_id=" + java.net.URLEncoder.encode(CLIENT_ID, java.nio.charset.StandardCharsets.UTF_8) +
                          "&refresh_token=" + java.net.URLEncoder.encode(refreshToken, java.nio.charset.StandardCharsets.UTF_8) +
                          "&scope=" + java.net.URLEncoder.encode("XboxLive.signin offline_access", java.nio.charset.StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://login.microsoftonline.com/consumers/oauth2/v2.0/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String resBody = response.body();

            if (resBody.contains("error")) {
                result.error = getJsonValue(resBody, "error_description");
                if (result.error == null) result.error = getJsonValue(resBody, "error");
                return result;
            }

            String msAccessToken = getJsonValue(resBody, "access_token");
            String newRefreshToken = getJsonValue(resBody, "refresh_token");
            if (msAccessToken == null) {
                result.error = "Failed to refresh MS access token";
                return result;
            }

            result.refreshToken = newRefreshToken != null ? newRefreshToken : refreshToken;

            return completeMinecraftLogin(msAccessToken, result);
        } catch (Exception e) {
            result.error = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
        }
        return result;
    }

    public static LoginResult pollToken(String deviceCode) {
        LoginResult result = new LoginResult();
        try {
            String body = "grant_type=" + java.net.URLEncoder.encode("urn:ietf:params:oauth:grant-type:device_code", java.nio.charset.StandardCharsets.UTF_8) +
                          "&client_id=" + java.net.URLEncoder.encode(CLIENT_ID, java.nio.charset.StandardCharsets.UTF_8) +
                          "&device_code=" + java.net.URLEncoder.encode(deviceCode, java.nio.charset.StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://login.microsoftonline.com/consumers/oauth2/v2.0/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String resBody = response.body();

            if (resBody.contains("authorization_pending")) {
                result.error = "PENDING";
                return result;
            }

            if (resBody.contains("error")) {
                result.error = getJsonValue(resBody, "error_description");
                if (result.error == null) result.error = getJsonValue(resBody, "error");
                return result;
            }

            String msAccessToken = getJsonValue(resBody, "access_token");
            String msRefreshToken = getJsonValue(resBody, "refresh_token");
            if (msAccessToken == null) {
                result.error = "Failed to get MS access token";
                return result;
            }

            result.refreshToken = msRefreshToken;

            return completeMinecraftLogin(msAccessToken, result);
        } catch (Exception e) {
            result.error = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
        }
        return result;
    }

    private static LoginResult completeMinecraftLogin(String msAccessToken, LoginResult result) throws Exception {
        // Step 2: Xbox Live Auth
        String xblBody = "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d=" + msAccessToken + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
        HttpRequest xblReq = HttpRequest.newBuilder()
            .uri(URI.create("https://user.auth.xboxlive.com/user/authenticate"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(xblBody))
            .build();

        HttpResponse<String> xblRes = client.send(xblReq, HttpResponse.BodyHandlers.ofString());
        String xblResBody = xblRes.body();
        String xblToken = getJsonValue(xblResBody, "Token");
        String uhs = getJsonValue(xblResBody, "uhs");

        if (xblToken == null || uhs == null) {
            result.error = "Failed to authenticate with Xbox Live";
            return result;
        }

        // Step 3: XSTS Auth
        String xstsBody = "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblToken + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
        HttpRequest xstsReq = HttpRequest.newBuilder()
            .uri(URI.create("https://xsts.auth.xboxlive.com/xsts/authorize"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(xstsBody))
            .build();

        HttpResponse<String> xstsRes = client.send(xstsReq, HttpResponse.BodyHandlers.ofString());
        String xstsResBody = xstsRes.body();
        
        if (xstsResBody.contains("error")) {
            result.error = "XSTS Error. Make sure you have an Xbox Live profile.";
            return result;
        }

        String xstsToken = getJsonValue(xstsResBody, "Token");
        uhs = getJsonValue(xstsResBody, "uhs");

        if (xstsToken == null || uhs == null) {
            result.error = "Failed to authenticate with XSTS";
            return result;
        }

        // Step 4: Login with Xbox to Mojang API
        String mcBody = "{\"identityToken\":\"XBL3.0 x=" + uhs + ";" + xstsToken + "\"}";
        HttpRequest mcReq = HttpRequest.newBuilder()
            .uri(URI.create("https://api.minecraftservices.com/authentication/login_with_xbox"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mcBody))
            .build();

        HttpResponse<String> mcRes = client.send(mcReq, HttpResponse.BodyHandlers.ofString());
        String mcResBody = mcRes.body();
        String mcToken = getJsonValue(mcResBody, "access_token");

        if (mcToken == null) {
            result.error = "Failed to get Minecraft access token";
            return result;
        }

        // Step 5: Get Profile
        HttpRequest profReq = HttpRequest.newBuilder()
            .uri(URI.create("https://api.minecraftservices.com/minecraft/profile"))
            .header("Authorization", "Bearer " + mcToken)
            .GET()
            .build();

        HttpResponse<String> profRes = client.send(profReq, HttpResponse.BodyHandlers.ofString());
        String profResBody = profRes.body();
        String username = getJsonValue(profResBody, "name");
        String uuid = getJsonValue(profResBody, "id");

        if (username == null || uuid == null) {
            result.error = "Failed to fetch Minecraft profile. Do you own the game?";
            return result;
        }

        result.username = username;
        result.uuid = uuid;
        result.token = mcToken;

        return result;
    }

    private static String getJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
