package com.fernando.examplegke;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.BlobId;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {
    @Value("${spring.cloud.gcp.project-id}")
    private String gcpProjectId;

    @Value("${app.gcs.path}")
    private String storageServiceAccountPath;
    @Value("${app.gcs.path1}")
    private String r1;
    @Value("${app.gcs.path2}")
    private String r2;
    @Value("${app.gcs.path3}")
    private String r3;

    private final ResourceLoader resourceLoader;

    private Storage firstStep() {
        Storage storage = null;
        try {
            storage = StorageOptions.getDefaultInstance().getService();
            log.info("Buckets:");
            Page<Bucket> buckets = storage.list();
            for (Bucket bucket : buckets.iterateAll()) {
                log.info(bucket.toString());
            }

            log.info("***** FIRST STEP: SE CREO LA INSTANCIA *****");
        } catch (Exception e) {
            log.error("***** FIRST STEP ERROR:{} *****", e.getMessage());
        }
        return storage;
    }

    private Storage secondStep() {
        Storage storage = null;
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(this.storageServiceAccountPath))
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(this.gcpProjectId).build().getService();
            log.info("***** SECOND STEP: SE CREO LA INSTANCIA *****");
        } catch (Exception e) {
            log.error("***** SECOND STEP ERROR:{} *****", e.getMessage());
        }
        return storage;
    }

    /**
     * Lee un directorio
     *
     * @param route ruta del directorio
     */
    private void readDirectory(String route) {
        log.info("***** LEYENDO EL DIRECTORIO: {} *****", route);
        try {
            File folder = new File(route);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    printContent(file);
                }
            }
        } catch (Exception e) {
            log.info("***** ERROR AL LEER EL DIRECTORIO: {} *****", e.getMessage());
        }
    }

    /**
     * Imprime el contenido de un directorio
     *
     * @param file archivo a leer
     * @throws IOException En caso de no encontrar el archivo
     */
    private void printContent(File file) throws IOException {
        if (file.isFile()) {
            log.info("File " + file.getName());
            try (BufferedReader in = new BufferedReader(new FileReader(file))) {
                String line = in.readLine();
                while (line != null) {
                    log.info(line);
                    line = in.readLine();
                }
            }
        } else if (file.isDirectory()) {
            log.info("Directory " + file.getName());
        }
    }

    private Storage getStorageInstance() {
        Storage s1 = this.firstStep();
        Storage s2 = this.secondStep();
        Storage s3 = this.getInstaceStorage();
        if (s3 != null) {
            log.info("****USANDO S3 *****");
            return s3;
        } else if (s2 != null) {
            log.info("****USANDO S2 *****");
            return s2;
        } else {
            if (s1 != null) {
                log.info("****USANDO S1 *****");
                return s1;
            }
        }
        return StorageOptions.newBuilder().setCredentials(this.getServiceAccountCredentials())
                .setProjectId(this.gcpProjectId).build().getService();
    }

    private Storage getInstaceStorage() {
        try {
            log.info("***** OBTENIENDO INSTANCIA DE LA CUENT DE SERVICIO *****");
            return StorageOptions.newBuilder().setCredentials(this.getServiceAccountCredentials())
                    .setProjectId(this.gcpProjectId).build().getService();
        } catch (Exception e) {
            log.info("ERROR AL LEER LA CUENTA DE SERVICIO: {}", e.getMessage());
        }
        return null;
    }

    private ServiceAccountCredentials getServiceAccountCredentials() {
        log.info("***** STORAGE SERVICE ACCOUNT PATH: {} ***** ", this.storageServiceAccountPath);
        log.info("***** PROJECT ID: {} *****", this.gcpProjectId);
        Resource resource = this.resourceLoader.getResource(this.storageServiceAccountPath);
        try {
            return ServiceAccountCredentials.fromStream(resource.getInputStream());
        } catch (IOException e) {
            log.error("getStorageServiceAccount", e);
            throw new EmisionStorageException(e.getMessage());
        }
    }

    /**
     * Realiza la descarga de un archivo en GCS
     *
     * @param bucketName nombre del bucket
     * @param fullPath   ruta del archivo
     * @return data en byteArray
     */
    @Override
    public ByteArrayOutputStream getFileFromStorage(String bucketName, String fullPath) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Storage storage = this.getStorageInstance();
        Blob blob = storage.get(BlobId.of(bucketName, fullPath));
        blob.downloadTo(outputStream);
        return outputStream;
    }

    /**
     * Sube un archivo a GCS
     *
     * @param bucketName  nombre del bucket
     * @param fullPath    ruta del archivo
     * @param inputStream bytes del archivo
     */
    @Override
    public void uploadFileToBucket(String bucketName, String fullPath, InputStream inputStream) {
        try {
            Storage storage = this.getStorageInstance();
            BlobId blob = BlobId.of(bucketName, fullPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blob).build();
            storage.createFrom(blobInfo, inputStream, Storage.BlobWriteOption.detectContentType());
        } catch (IOException e) {
            log.error("uploadFileToBucket", e);
            throw new EmisionStorageException("Error al subir el archivo");
        }
    }

    /**
     * Genera una URL firmada para la descarga de archivos
     *
     * @param bucket       nombre del bucket
     * @param resourcePath ruta del archivo
     * @param vigencia     tiempo de vigencia
     * @param unidadTiempo unidad de tiempo del documento
     * @return url firmada
     */
    @Override
    public String generarUrlFirmada(String bucket, String resourcePath, int vigencia, TimeUnit unidadTiempo) {
        Storage storage = this.getStorageInstance();
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucket, resourcePath)).build();
        URL url = storage.signUrl(blobInfo, vigencia, unidadTiempo, Storage.SignUrlOption.withV4Signature());
        return url.toString();
    }

    /**
     * Guarda un archivo en GCS
     *
     * @param bucketName  nombre del bucket
     * @param fullPath    ruta donde se almacenara el archivo
     * @param inputStream archivo a subir
     * @return direccion del archivo guardado
     */
    @Override
    public URI uploadFileToBucketWithURL(String bucketName, String fullPath, InputStream inputStream) {
        try {
            Storage storage = this.getStorageInstance();
            BlobId blobId = BlobId.of(bucketName, fullPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            Blob blob1 = storage.createFrom(blobInfo, inputStream, Storage.BlobWriteOption.detectContentType());
            return URI.create("https://storage.googleapis.com/" + bucketName + "/" + URLEncoder.encode(blob1.getName(), String.valueOf(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            log.error("uploadFileToBucket", e);
            throw new EmisionStorageException("Error al subir el archivo");
        }
    }
}
