package labit.analytics;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Folders {

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
                System.out.println("Root, Project ID " + projectId + " does not have root folders or does not exist.");
                System.out.println("");
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
            System.out.println("");

            return folderId;
        }
    }

    /* LISTAR EL CONTENIDO DE LOS DIRECTORIOS */
    public static String listFolderContents(String accessToken, String projectId, String folderId) throws IOException {
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
            // System.out.println("API Response: " + responseBody); // DEBUG
            System.out.println("");

            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            if (!json.has("data") || json.get("data").isJsonNull()) {
                System.out.println("Error: 'data' no está presente en la respuesta.");
                return null;
            }

            JsonArray items = json.getAsJsonArray("data");

            for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();

            // Verificar si 'id' y 'type' existen
            if (!item.has("id") || !item.has("type")) {
                System.out.println("Error: Item sin 'id' o 'type' en índice " + i);
                continue;
            }

            String itemId = item.get("id").getAsString();
            String itemType = item.get("type").getAsString(); // "folders" o "items"

            if (itemType.equals("folders")) {
                // Solo acceder a 'attributes' si es una carpeta
                if (item.has("attributes") && item.getAsJsonObject("attributes").has("name")) {
                    String itemName = item.getAsJsonObject("attributes").get("name").getAsString();
                    System.out.println("📂 Directorio - ID: " + itemId + " | Nombre: " + itemName);
                } else {
                    System.out.println("⚠️ Directorio sin nombre en índice " + i);
                }
            } else if (itemType.equals("items")) {
                // Procesar archivos (.rvt, .nwc, etc.)
                if (item.has("attributes") && item.getAsJsonObject("attributes").has("displayName")) {
                    String itemName = item.getAsJsonObject("attributes").get("displayName").getAsString();
                    System.out.println("📄 Archivo - ID: " + itemId + " | Nombre: " + itemName);
                } else {
                    System.out.println("⚠️ Archivo sin nombre en índice " + i);
                }
            } else {
                System.out.println("🔹 Tipo desconocido: " + itemType);
            }
        }
        return responseBody;
        }
    }

    /* EXPLORAR SUBCARPETAS */
    public static void exploreFolder(String accessToken, String projectId, String folderId) {
        try {
            // Llamada para obtener el contenido del directorio
            String response = listFolderContents(accessToken, projectId, folderId);
    
            // Parsear la respuesta
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            JsonArray items = json.getAsJsonArray("data");
    
            // Iterar sobre los elementos del directorio
            for (int i = 0; i < items.size(); i++) {
                JsonObject item = items.get(i).getAsJsonObject();
                String type = item.get("type").getAsString(); // Puede ser "folders" o "items"
    
                // Validar el nombre y otros atributos para evitar excepciones
                String itemId = item.has("id") ? item.get("id").getAsString() : "Unknown ID";
                String itemName = item.has("attributes") && item.getAsJsonObject("attributes").has("name")
                        ? item.getAsJsonObject("attributes").get("name").getAsString()
                        : "Unknown Name";
    
                // Mostrar información del elemento
                if (type.equals("folders")) {
                    System.out.println("Exploring Folder ID: " + itemId + ", Name: " + itemName);
                    // Llamada recursiva para explorar subcarpeta
                    exploreFolder(accessToken, projectId, itemId);
                } else if (type.equals("items")) {
                    System.out.println("Item ID: " + itemId + ", Name: " + itemName);
                } else {
                    System.out.println("Unknown type: " + type);
                }
            }
        } catch (Exception e) {
            System.err.println("Error exploring folder: " + e.getMessage());
        }
    }
    
    
    /*  LISTAR LA CARPETA POR ID */
    public static String listFolderById(String accessToken, String projectId, String folderId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/folders/" + folderId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonObject folder = json.getAsJsonObject("data");

            String folderName = folder.getAsJsonObject("attributes").get("name").getAsString();
            System.out.println("Folder ID: " + folderId + ", Name: " + folderName);
            return folderId;
        }
    }

    /* Devuelve los recursos ( items, folders, y versions) que tienen una relación personalizada con el folder_id */
    public static void listFolderRelations(String accessToken, String projectId, String folderId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/folders/" + folderId + "/refs")
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
    
    public static String findFirstItemInFolder(String accessToken, String projectId, String folderId) throws IOException {
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
            String itemType = item.get("type").getAsString();

            if (itemType.equals("items")) {
                String itemId = item.get("id").getAsString();
                System.out.println("Found Item ID: " + itemId);
                return itemId; // Devuelve el primer archivo encontrado
            } else if (itemType.equals("folders")) {
                String subFolderId = item.get("id").getAsString();
                System.out.println("Exploring Subfolder ID: " + subFolderId);

                // Llamada recursiva para explorar subcarpetas
                String foundItemId = findFirstItemInFolder(accessToken, projectId, subFolderId);
                if (foundItemId != null) {
                    return foundItemId;
                }
            }
        }

        System.out.println("No items found in folder: " + folderId);
        return null; // Ningún archivo encontrado en este folder o subcarpetas
        }
    }

    /* Devuelve la carpeta principal (si existe). En un proyecto, las subcarpetas y los elementos de recursos se almacenan en una carpeta, excepto la carpeta raíz, que no tiene una carpeta principal propia */
    public static void listPrincipalFolder(String accessToken, String projectId, String folderId) throws IOException{
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/folders/" + folderId + "/parent")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try(Response response = client.newCall(request).execute()){
            if(!response.isSuccessful()){
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonObject folder = json.getAsJsonObject("data");

            String folderName = folder.getAsJsonObject("attributes").get("name").getAsString();
            System.out.println("Folder ID: " + folderId + ", Name: " + folderName);
        }catch(IOException e){
            System.out.println("No principal folder found for folder ID: " + folderId);
        }
    }

    /* Devuelve una colección de links para el determinado folder_id */

    public static void listFolderLinks(String accessToken, String projectId, String folderId) throws IOException {
        OkHttpClient client = new OkHttpClient();
    
        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/folders/" + folderId + "/relationships/links")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();
    
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }
    
            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
    
            JsonArray dataArray = json.getAsJsonArray("data");
            if (dataArray.size() == 0) {
                System.out.println("No data available in the folder links.");
                return;
            }
    
            // Procesar cada elemento del array
            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject element = dataArray.get(i).getAsJsonObject();
                System.out.println("Element " + i + ": " + element);
            }
        }
    }
    
}
