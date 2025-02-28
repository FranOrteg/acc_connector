package labit.analytics;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Projects {

    /* ************************ PROYECTOS ************************ */
    /* LISTAR LOS PROYECTOS */
    public static void listProjects(String accessToken, String hubId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/project/v1/hubs/" + hubId + "/projects")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            Main.parseAndPrintProjects(responseBody);
        }
    }

    /* Devuelve un proyecto para un determinado project_id */
    public static void getProjectById(String accessToken, String hubId, String projectId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/project/v1/hubs/" + hubId + "/projects/" + projectId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();
        
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
                }

            String responseBody = response.body().string();
            System.out.println("Project Data By ID: " + responseBody);
            System.out.println("");
        }
    }

    /* OBTENER LOS DETALLES DE LAS CARPETAS SUPERIORES A LAS QUE PERTENECEN LOS PROYECTOS */
    public static String listFolderDetail(String accessToken, String hubId, String projectId) throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/project/v1/hubs/" + hubId + "/projects/" + projectId + "/topFolders")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            // System.out.println("folderDetails" + responseBody);
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray folders = json.getAsJsonArray("data");

            if (folders.size() == 0) {
                System.out.println("No root folders found for Project ID: " + projectId);
                System.out.println("");
                return null;
            }

            JsonObject folder = folders.get(0).getAsJsonObject();
            String folderId = folder.get("id").getAsString();
            String folderName = folder.getAsJsonObject("attributes").get("name").getAsString();
            System.out.println("Folder Detailed, ID: " + folderId + ", Name: " + folderName);
            System.out.println("");

            return folderId;
        }
    }

    /* DEVUELVE LOS DETALLES PARA UNA DESCARGA ESPECIFICA */
    public static void getDownloadDetails(String accessToken, String projectId, String downloadId) throws IOException {
        OkHttpClient client = new OkHttpClient();
    
        // Construimos la URL con projectId y downloadId
        String url = "https://developer.api.autodesk.com/data/v1/projects/" 
                      + projectId + "/downloads/" + downloadId;
    
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                // Si deseas limitar la llamada a un usuario en un contexto 2-legged,
                // puedes agregar la cabecera x-user-id:
                // .addHeader("x-user-id", "user_id_aqui")
                .build();
    
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(
                        "Unexpected code " + response 
                        + ", body: " + (response.body() != null ? response.body().string() : "No body"));
            }
        
            String responseBody = response.body().string();
            System.out.println("Detalles de la descarga: " + responseBody);
        }
    }
    
}
