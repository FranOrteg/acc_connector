package labit.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

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

     /* DEVUELVE UNA COLECCIÓN DE FORMATOS DE ARCHIVOS A LOS QUE ESTA VERSIÓN
       PODRÍA CONVERTIRSE Y DESCARGARSE */
    public static List<String> getFileFormatCollection(String accessToken, String projectId, String versionId) throws IOException {
     OkHttpClient client = new OkHttpClient();
    
     Request request = new Request.Builder()
             .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/versions/" + versionId + "/downloadFormats")
             .addHeader("Authorization", "Bearer " + accessToken)
             .addHeader("Content-Type", "application/json")
             .build();
    
             try (Response response = client.newCall(request).execute()) {
                 if (!response.isSuccessful()) {
                     throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
                }
         
                String responseBody = response.body().string();
         
                JSONObject json = new JSONObject(responseBody);
         
                // Extraer la colección de formatos de la respuesta
         
                List<String> fileFormats = new ArrayList<>();
         
                JSONArray formatsArray = json.optJSONArray("formats"); // Ajusta el nombre si es diferente en la API
         
                if (formatsArray != null) {
         
                for (int i = 0; i < formatsArray.length(); i++) {
                    fileFormats.add(formatsArray.getString(i));     
                }
         
            }
            return fileFormats;
        }
    }

    /* DEVUELVE UN SET DE LAS DESCARGAS DISPONIBLES PARA ESTA VERSION */
    public static Set<String> getAvaliableDownloads(String accessToken, String projectId, String versionId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
             .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/versions/" + versionId + "/downloads")
             .addHeader("Authorization", "Bearer " + accessToken)
             .addHeader("Content-Type", "application/json")
             .build();

             try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
                }

                String responseBody = response.body().string();

                JSONObject json = new JSONObject(responseBody);

                // Extraer el del set los resultados disponibles

                Set<String> downloadsSet = new HashSet<>();

                if(json.has("data") && json.get("data") instanceof JSONArray){
                    JSONArray downloadsArray = json.getJSONArray("data");

                    for (int i = 0; i < downloadsArray.length(); i++){
                        JSONObject downloadObj = downloadsArray.getJSONObject(i);
                        
                        if (downloadObj.has("id")) {
                            downloadsSet.add(downloadObj.getString("id"));
                        }
                    }
                }

                return downloadsSet;
        }    
    }

    /* DEVUELVE EL ITEM ASOCIADO A LA VERSION DADA */
    public static String getItemByVersion(String accessToken, String projectId, String versionId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/versions/" + versionId + "/item")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            // Extraemos el ID del item si está presente en la respuesta
            if (json.has("data") && json.get("data") instanceof JSONObject) {
                JSONObject dataObject = json.getJSONObject("data");
                if (dataObject.has("id")) {
                    return dataObject.getString("id");
                }
            }
            return null; // Si no se encuentra el ID
        }
    }
}

