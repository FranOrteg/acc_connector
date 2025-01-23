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
    
    private static final String CLIENT_ID = dotenv.get("CLIENT_ID_BB");
    private static final String CLIENT_SECRET = dotenv.get("CLIENT_SECRET_BB");
    private static final String AUTH_URL = dotenv.get("AUTH_URL");
    
    public static void main(String[] args) {
        try {
            // Obtener el token de acceso
            String accessToken = getAccessToken();
            System.out.println("");
            System.out.println("Access Token: " + accessToken);
            System.out.println("");

            // Listar hubs
            getHubs(accessToken);

            // ID del hub a explorar
            String hubId = "b.1bb899d4-8dd4-42d8-aefd-6c0e35acd825";

            // listar los datos de un hub en especifico
            // getHubDataById(accessToken, hubId);

            // Listar proyectos del hub
            Projects.listProjects(accessToken, hubId);
            System.out.println("");

            // Explorar un proyecto específico
            String projectId = "b.4f95a7b1-9e87-4399-8927-4acb2349134b";
            
            String folderId2 = "urn:adsk.wipprod:fs.folder:co.LVs6tq9ZRpynB8ASuBy-9g";

            // Obtener los datos de un proyecto por el ID de proyecto
            Projects.getProjectById(accessToken,hubId,projectId);
            
            String folderId = Folders.listRootFolders(accessToken, projectId);

            if (folderId != null) {
                Folders.listFolderContents(accessToken, projectId, folderId);
            } else {
                System.out.println("No root folder found for Project ID: " + projectId);
                System.out.println("");
            }

            // Detalles de los directorios superiores a los que pertenece un proyecto
            String folderprojectId = Projects.listFolderDetail(accessToken, hubId, projectId);
            System.out.println(folderprojectId);

            // Devuelve los recursos
            Folders.listFolderRelations(accessToken, projectId, folderprojectId);

            /*  */
            Folders.listFolderLinks(accessToken, projectId, folderprojectId);

            // Obtener un item dentro del folder
            String itemId = Folders.findFirstItemInFolder(accessToken, projectId, folderprojectId);
            if (itemId == null) {
                System.out.println("No items found in folder.");
                return;
            }

            // Obtener la carpeta principal
            Folders.listPrincipalFolder(accessToken, projectId, folderId2);

    
            // Obtener el version_id del item
            String versionId = getVersionId(accessToken, projectId, itemId);
            if (versionId == null) {
                System.out.println("No version ID found for item: " + itemId);
                return;
            }
    
            // Verificar formatos soportados
            verifyFormats(accessToken, projectId, versionId);
    
            // Crear una descarga en el formato deseado
            String fileType = "dwf"; // Cambiar según el formato disponible
            createDownload(accessToken, projectId, versionId, fileType);
    

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
                .add("scope", "data:create data:read data:write")
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
    public static void getHubs(String accessToken) throws IOException {
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
            System.out.println("");
        }
    }

    public static void getHubDataById(String accessToken, String hubId) throws IOException{
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/project/v1/hubs/" + hubId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            String responseBody = response.body().string();
            System.out.println("Hub Data: " + responseBody);
            System.out.println("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* SOLICITAR DESCARGA DE ARCHIVO */   
    public static void createDownload(String accessToken, String projectId, String versionId, String fileType) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Cuerpo de la solicitud
        JsonObject requestBody = new JsonObject();
        JsonObject jsonapi = new JsonObject();
        jsonapi.addProperty("version", "1.0");
        requestBody.add("jsonapi", jsonapi);

        JsonObject data = new JsonObject();
        data.addProperty("type", "downloads");

        JsonObject attributes = new JsonObject();
        JsonObject format = new JsonObject();
        format.addProperty("fileType", fileType);
        attributes.add("format", format);
        data.add("attributes", attributes);

        JsonObject relationships = new JsonObject();
        JsonObject source = new JsonObject();
        JsonObject sourceData = new JsonObject();
        sourceData.addProperty("type", "versions");
        sourceData.addProperty("id", versionId);
        source.add("data", sourceData);
        relationships.add("source", source);
        data.add("relationships", relationships);

        requestBody.add("data", data);

        // Solicitud HTTP
        Request request = new Request.Builder()
                .url("https://developer.api.autodesk.com/data/v1/projects/" + projectId + "/downloads")
                .post(RequestBody.create(requestBody.toString(), okhttp3.MediaType.parse("application/vnd.api+json")))
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/vnd.api+json")
                .build();

        // Enviar la solicitud
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response + ", body: " + response.body().string());
            }

            // Respuesta exitosa
            String responseBody = response.body().string();
            System.out.println("Download job created successfully: " + responseBody);
        }
    }

    /* OBTENER EL VERSION ID DE UN ARCHIVO */
    public static String getVersionId(String accessToken, String projectId, String itemId) throws IOException {
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
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray versions = json.getAsJsonArray("data");

            if (versions.size() > 0) {
                JsonObject version = versions.get(0).getAsJsonObject();
                String versionId = version.get("id").getAsString();
                System.out.println("Version ID: " + versionId);
                return versionId;
            } else {
                System.out.println("No versions found for item ID: " + itemId);
                return null;
            }
        }
    }

    /* VERIFICAR FORMATOS SOPORTADOS */
    public static void verifyFormats(String accessToken, String projectId, String versionId) throws IOException {
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
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray formats = json.getAsJsonArray("data");

            System.out.println("Supported formats:");
            for (int i = 0; i < formats.size(); i++) {
                JsonObject format = formats.get(i).getAsJsonObject();
                String fileType = format.get("attributes").getAsJsonObject().get("fileType").getAsString();
                System.out.println("- " + fileType);
            }
        }
    }


    /* PARSEAR Y MOSTRAR LOS PROYECTOS */
    public static void parseAndPrintProjects(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray projects = json.getAsJsonArray("data");

        for (int i = 0; i < projects.size(); i++) {
            JsonObject project = projects.get(i).getAsJsonObject();
            String projectId = project.get("id").getAsString();
            String projectName = project.getAsJsonObject("attributes").get("name").getAsString();
            String rootFolferId = project.getAsJsonObject("relationships").getAsJsonObject("rootFolder").getAsJsonObject("data").get("id").getAsString();
            System.out.println(
                "Project ID: " + projectId + 
                "\nName: " + projectName + 
                "\nRoot Folder ID: " + rootFolferId);
        }
    }
}
