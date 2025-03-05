package labit.analytics;

import java.io.IOException;

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
}
