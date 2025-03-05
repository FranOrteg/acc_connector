package labit.analytics;

import java.io.IOException;
import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class Items {

    /* Retrieves metadata for a specified item. Items represent word documents, fusion design files, drawings, spreadsheets, etc.*/
    public static String[] dataItem(String accessToken, String projectId, String itemId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/items/" + itemId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            String[] extractedKeys = extractBucketAndObjectKey(responseBody);

            System.out.println("Item Data: " + Arrays.toString(extractedKeys));
            return extractedKeys;
        }
    }

    /* Returns the "parent" folder for the given item. */
    public static String getItemParentFolder(String accessToken, String projectId, String itemId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/items/" + itemId + "/parent")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            System.out.println("Item Parent Folder: " + responseBody);
            System.out.println("");
            return responseBody;
        }
    }

    /* returns the resources (items, folders, and versions) that have a custom relationship with the given item_id */
    public static String getItemRefs(String accessToken, String projectId, String itemId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/items/" + itemId + "/refs")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            System.out.println("Item Refs: " + responseBody);
            System.out.println("");
            return responseBody;
        }
    }

    /* Returns a collection of links for the given item_id */
    public static String getItemLinks(String accessToken, String projectId, String itemId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/items/" + itemId + "/relationships/links")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            System.out.println("Item Links: " + responseBody);
            System.out.println("");
            return responseBody;
        }
    }

    /* Returns the custom relationships that are associated with the given item_id */
    public static String getItemRelationships(String accessToken, String projectId, String itemId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/items/" + itemId + "/relationships/refs")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            System.out.println("Item Relationships: " + responseBody);
            System.out.println("");
            return responseBody;
        }
    }

    /* Returns the “tip” version for the given item. */
    public static String getItemTip(String accessToken, String projectId, String itemId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/items/:" + itemId + "/tip")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            System.out.println("Item Tip: " + responseBody);
            System.out.println("");
            return responseBody;
        }
    }

    /* Returns versions for the given item */
    public static String getItemVersions(String accessToken, String projectId, String itemId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/items/" + itemId + "/versions")
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            System.out.println("Item Versions: " + responseBody);
            System.out.println("");
            return responseBody;
        }
    }

    /* EXTRAER EL BUCKET_KEY Y EL OBJECT_KEY */
    public static String[] extractBucketAndObjectKey(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Obtener la sección "included" donde está el almacenamiento
        JsonArray includedArray = jsonObject.getAsJsonArray("included");
        if (includedArray == null || includedArray.size() == 0) {
            throw new IllegalStateException("No se encontraron datos de almacenamiento en la respuesta JSON.");
        }

        JsonObject includedObject = includedArray.get(0).getAsJsonObject();
        JsonObject storageData = includedObject.getAsJsonObject("relationships")
                                               .getAsJsonObject("storage")
                                               .getAsJsonObject("data");

        // Obtener la URN completa (ejemplo: "urn:adsk.objects:os.object:wip.dm.prod/9c696903-31c6-4b01-8717-8d198644eb42.nwc")
        String urn = storageData.get("id").getAsString();

        // Extraer el bucketKey y objectKey correctamente
        if (!urn.startsWith("urn:adsk.objects:os.object:")) {
            throw new IllegalStateException("URN de almacenamiento no tiene el formato esperado: " + urn);
        }

        // Eliminar "urn:adsk.objects:os.object:" y dividir
        String[] urnParts = urn.replace("urn:adsk.objects:os.object:", "").split("/");

        if (urnParts.length < 2) {
            throw new IllegalStateException("No se pudo extraer bucketKey y objectKey del URN: " + urn);
        }

        String bucketKey = urnParts[0]; // "wip.dm.prod"
        String objectKey = urnParts[1]; // "9c696903-31c6-4b01-8717-8d198644eb42.nwc"

        System.out.println("Bucket Key extraído: " + bucketKey);
        System.out.println("Object Key extraído: " + objectKey);

        return new String[]{bucketKey, objectKey};
    }

}
