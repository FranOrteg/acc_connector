package labit.analytics;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class buckets {
    
    /*  This endpoint will return the buckets owned by the application. This endpoint supports pagination.*/
    public static void getAppBuckets(String accessToken) throws IOException{
        
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
        .url("https://developer.api.autodesk.com/oss/v2/buckets")
        .addHeader("Authorization", "Bearer " + accessToken)
        .addHeader("Content-Type", "application/json")
        .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            System.out.println("Buckets: " + responseBody);
            System.out.println("");
        }
    }

    /* Generate a signed URL to an object, which can be used to download it directly from S3. */
    public static String getSignedUrl(String accessToken, String bucketKey, String object_Key) throws IOException{
        
        OkHttpClient client = new OkHttpClient();

        String url = "https://developer.api.autodesk.com/oss/v2/buckets/" + bucketKey + "/objects/" + object_Key + "/signeds3download";

        Request request = new Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer " + accessToken)
        .addHeader("Content-Type", "application/json")
        .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }
        
            String responseBody = response.body().string();
            System.out.println("Signed S3 URL: " + responseBody);
            System.out.println("");
        
            return extractSignedUrl(responseBody);
        }

    }

    /* MÉTODO PARA EXTRAER LA URL FIRMADA DEL JSON */
    public static String extractSignedUrl(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        return jsonObject.get("url").getAsString();
    }
}
