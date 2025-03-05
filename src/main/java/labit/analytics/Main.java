package labit.analytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
            String projectId = "b.76947f01-cc26-47db-9681-fff27e5430ce"; /* 00 PRUEBA MODEL COORDINATION */
            String folderId = "urn:adsk.wipprod:fs.folder:co.D7F4NTwdRqqYqMaK-M01Yw";
            
            // Obtener los datos de un proyecto por el ID de proyecto
            // Projects.getProjectById(accessToken,hubId,projectId);
            
            /* String folderId = Folders.listRootFolders(accessToken, projectId);

            if (folderId != null) {
                Folders.listFolderContents(accessToken, projectId, folderId);
            } else {
                System.out.println("No root folder found for Project ID: " + projectId);
                System.out.println("");
            } */

            Folders.listFolderContents(accessToken, projectId, folderId);
            System.out.println("");

            String itemId = "urn:adsk.wipprod:fs.folder:co._HMyOfTySzGn6hpRrYrdvA";
            String itemId2 = "urn:adsk.wipprod:fs.folder:co.DNfxkrkRTY6TLyLWdQjUTw";
            //String itemId3 = "urn:adsk.wipprod:fs.folder:co.CEK-EkKGSWq0dDfl7waJmQ";
            //String itemId4 = "urn:adsk.wipprod:fs.folder:co.QS95ugBYTRK6y2DeB1dTAg";
            //String itemId5 = "urn:adsk.wipprod:fs.folder:co.oEDfGjJsTDiwBBoHgL4xDA";

            // Items.dataItem(accessToken, projectId, itemId);

            // Detalles de los directorios superiores a los que pertenece un proyecto
            //String folderprojectId = Projects.listFolderDetail(accessToken, hubId, projectId);
            //System.out.println(folderprojectId);

            // Devuelve los recursos
            //Folders.listFolderRelations(accessToken, projectId, folderprojectId);

            /*  */
            //Folders.listFolderLinks(accessToken, projectId, folderprojectId);

            // Obtener un item dentro del folder
            /* String itemId = Folders.findFirstItemInFolder(accessToken, projectId, folderprojectId);
            if (itemId == null) {
                System.out.println("No items found in folder.");
                return;
            } */

            // Obtener la carpeta principal
            // Folders.listPrincipalFolder(accessToken, projectId, folderId2);

    
            // Obtener el version_id del item
            /* String versionId = getVersionId(accessToken, projectId, itemId);
            if (versionId == null) {
                System.out.println("No version ID found for item: " + itemId);
                return;
            } */
    
            // Verificar formatos soportados
            // verifyFormats(accessToken, projectId, versionId);
    
            // Crear una descarga en el formato deseado
            // String fileType = "dwf"; // Cambiar según el formato disponible
            // createDownload(accessToken, projectId, versionId, fileType);
    
            System.out.println("------------------------");
            System.out.println("");
            Folders.listFolderContents(accessToken, projectId, itemId);
            System.out.println("");
            
            System.out.println("------------------------");
            System.out.println("");
            Folders.listFolderContents(accessToken, projectId, itemId2);
            System.out.println("");


            /*Folders.listFolderContents(accessToken, projectId, itemId3);
            System.out.println("");
            Folders.listFolderContents(accessToken, projectId, itemId4);
            System.out.println(" ------------------------------------------------- ");
            Folders.listFolderContents(accessToken, projectId, itemId5); */
            // Folders.exploreFolder(accessToken, projectId, itemId);

            /* CREATE A DOWNLOAD */

            /* Step 1: Find the hub that has your resource
             * Step 2: Find the project that has your resource
             * Step 3: Find the resource item in a project folder
             * Step 4: Find the storage location of the item
             * Step 5: Find the versions of an item
             * Step 6: Get the S3 URL
             * Step 7: Download the item
            */

            String finalItemID = "urn:adsk.wipprod:dm.lineage:VvOC3BqVTEKnoPrdX5uM_Q";
            String versionID = getVersionId(accessToken, projectId, finalItemID);
            System.out.println("");
            System.out.println("------------------------");
            System.out.println("");
            Versions.getVersions(accessToken, projectId, versionID);

            System.out.println("");
            System.out.println("------------------------");
            System.out.println("");
            String[] data = Items.dataItem(accessToken, projectId, finalItemID);

            System.out.println("");
            System.out.println("------------------------");
            System.out.println("");
            //buckets.getAppBuckets(accessToken);

            String bucketKey = data[0];
            String objectKey = data[1];
            String itemName = data[2];

            String signedUrl =  buckets.getSignedUrl(accessToken, bucketKey, objectKey);

            System.out.println("");
            System.out.println("------------------------");
            System.out.println("");
            
            System.out.println("Nombre del archivo a descargar: " + itemName);
            
            downloadFile(signedUrl,itemName);
 

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
                .add("scope", "data:create data:read data:write bucket:read")
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

    /* LISTAR LOS DATOS DE UN HUB EN ESPECIFICO */
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
    
    /* INICIAR LA DESCARGA */
    public static void downloadFile(String signedUrl, String fileName) {
        // Ruta al escritorio del usuario
        String home = System.getProperty("user.home");
        String downloadPath = home + File.separator + "Escritorio" + File.separator + fileName;

        try {
            // Convertir String a URI y luego a URL
            URL url = new URI(signedUrl).toURL();

            // Abrir la conexión y descargar el archivo
            try (InputStream in = url.openStream();
                 FileOutputStream out = new FileOutputStream(downloadPath)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                
                // Leer y escribir el archivo en bloques de 8 KB
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                System.out.println("Archivo descargado con éxito en: " + downloadPath);
            } catch (IOException e) {
                System.err.println("Error descargando el archivo: " + e.getMessage());
            }

        } catch (URISyntaxException | MalformedURLException e) {
            System.err.println("Error en la URL firmada: " + e.getMessage());
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
                "Name: " + projectName + 
                "\nProject ID: " + projectId + 
                "\nRoot Folder ID: " + rootFolferId);
        }
    }
}
