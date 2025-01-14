package labit.analytics;

import okhttp3.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

public class Main {

    private static final String CLIENT_ID = "hETiFjq1hJVAEiQsqMSAj9nIgFAvgQtsybvfAiWGWt2sC05Y";
    private static final String CLIENT_SECRET = "8YTVTE3m35mrgifD2yRIUGeorizxAS6JrvGIDKNCUboTChTU9bFgyUFgNVBxtxKt";
    private static final String AUTH_URL = "https://developer.api.autodesk.com/authentication/v2/token";

    public static void main(String[] args) {
        try {
            String accessToken = getAccessToken();
            System.out.println("Access Token: " + accessToken);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getAccessToken() throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Construimos el cuerpo de la solicitud
        RequestBody formBody = new FormBody.Builder()
            .add("client_id", CLIENT_ID)
            .add("client_secret", CLIENT_SECRET)
            .add("grant_type", "client_credentials") // Cambiado a client_credentials
            .add("scope", "data:read data:write") // Ajusta los scopes según tus necesidades
            .build();

        // Construimos la solicitud
        Request request = new Request.Builder()
            .url(AUTH_URL)
            .post(formBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build();

        // Ejecutamos la solicitud
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            // Parseamos la respuesta
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            return json.get("access_token").getAsString();
        }
    }
}
