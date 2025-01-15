package labit.analytics;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Main {

    private static final Dotenv dotenv = Dotenv.load();
    
    private static final String CLIENT_ID = dotenv.get("CLIENT_ID");
    private static final String CLIENT_SECRET = dotenv.get("CLIENT_SECRET");
    private static final String AUTH_URL = dotenv.get("AUTH_URL");
    
    public static void main(String[] args) {
        try {
            // Obtener el token de acceso
            String accessToken = getAccessToken();
            System.out.println("Access Token: " + accessToken);

            // Listar hubs
            listHubs(accessToken);

            // ID del hub a explorar
            String hubId = "b.1bb899d4-8dd4-42d8-aefd-6c0e35acd825";

            // Listar proyectos del hub
            listProjects(accessToken, hubId);

            // Explorar un proyecto específico
            String projectId = "b.76947f01-cc26-47db-9681-fff27e5430ce";
            String folderId = listRootFolders(accessToken, projectId);

            if (folderId != null) {
                listFolderContents(accessToken, projectId, folderId);
            } else {
                System.out.println("No root folder found for Project ID: " + projectId);
            }

            // Detalles de los directorios superiores a los que pertenece un proyecto
            listFolderDetail(accessToken, hubId, projectId);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* OBTENER EL TOKEN DE ACCESO */
    public static String getAccessToken() throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("grant_type", "client_credentials")
                .add("scope", "data:read data:write")
                .build();

        Request request = new Request.Builder()
                .url(AUTH_URL)
                .post(formBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            return json.get("access_token").getAsString();
        }
    }

    /* LISTAR LOS HUBS */
    public static void listHubs(String accessToken) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/project/v1/hubs")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            System.out.println("Hubs: " + responseBody);
        }
    }

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
            parseAndPrintProjects(responseBody);
        }
    }


    /* ************************ DIRECTORIOS ************************ */
    /* LISTAR LA CARPETA RAIZ DE LOS PROYECTOS */
    public static String listRootFolders(String accessToken, String projectId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/folders")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code() == 404) {
                System.out.println("Project ID " + projectId + " does not have root folders or does not exist.");
                return null;
            }
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray folders = json.getAsJsonArray("data");

            if (folders.size() == 0) {
                System.out.println("No root folders found for Project ID: " + projectId);
                return null;
            }

            JsonObject folder = folders.get(0).getAsJsonObject();
            String folderId = folder.get("id").getAsString();
            String folderName = folder.getAsJsonObject("attributes").get("name").getAsString();
            System.out.println("Folder ID: " + folderId + ", Name: " + folderName);

            return folderId;
        }
    }

    /* OBTENER LOS DETALLES DE LAS CARTERAS SUPERIORES A LAS QUE PERTENECEN LOS PROYECTOS */
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
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray folders = json.getAsJsonArray("data");

            if (folders.size() == 0) {
                System.out.println("No root folders found for Project ID: " + projectId);
                return null;
            }

            JsonObject folder = folders.get(0).getAsJsonObject();
            String folderId = folder.get("id").getAsString();
            String folderName = folder.getAsJsonObject("attributes").get("name").getAsString();
            System.out.println("Folder ID: " + folderId + ", Name: " + folderName);

            return folderId;
        }
    }

    /* LISTAR EL CONTENIDO DE LOS DIRECTORIOS */
    public static void listFolderContents(String accessToken, String projectId, String folderId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/folders/" + folderId + "/contents")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("data");

            for (int i = 0; i < items.size(); i++) {
                JsonObject item = items.get(i).getAsJsonObject();
                String itemId = item.get("id").getAsString();
                String itemName = item.getAsJsonObject("attributes").get("name").getAsString();
                System.out.println("Item ID: " + itemId + ", Name: " + itemName);
            }
        }
    }

    public static void parseAndPrintProjects(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray projects = json.getAsJsonArray("data");

        for (int i = 0; i < projects.size(); i++) {
            JsonObject project = projects.get(i).getAsJsonObject();
            String projectId = project.get("id").getAsString();
            String projectName = project.getAsJsonObject("attributes").get("name").getAsString();
            System.out.println("Project ID: " + projectId + ", Name: " + projectName);
        }
    }
}
