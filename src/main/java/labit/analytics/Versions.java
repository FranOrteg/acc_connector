package labit.analytics;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Versions {
    
    /* DEVUELVE LA VERSION CON UN VERSION ID PROPORCIONADO */

    public static void getVersions(String accessToken, String projectId, String versionId) throws IOException {
         OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/versions/" + versionId + "")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
                    }
    
                String responseBody = response.body().string();
                System.out.println("Version Project: " + responseBody);
                System.out.println("");
            }

    }
}
