package com.fernando.examplegke;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public interface StorageService {
    /**
     * Realiza la descarga de un archivo en GCS
     *
     * @param bucketName nombre del bucket
     * @param fullPath   ruta del archivo
     * @return
     */
    ByteArrayOutputStream getFileFromStorage(String bucketName, String fullPath);

    /**
     * Sube un archivo a GCS
     *
     * @param bucketName  nombre del bucket
     * @param fullPath    ruta del archivo
     * @param inputStream bytes del archivo
     */
    void uploadFileToBucket(String bucketName, String fullPath, InputStream inputStream);

    /**
     * Genera una URL firmada para la descarga de archivos
     *
     * @param bucket       nombre del bucket
     * @param resourcePath ruta del archivo
     * @param vigencia     tiempo de vigencia
     * @param unidadTiempo unidad de tiempo del documento
     * @return
     */
    String generarUrlFirmada(String bucket, String resourcePath, int vigencia,
                             TimeUnit unidadTiempo);

    /**
     * Guarda un archivo en GCS
     *
     * @param bucketName  nombre del bucket
     * @param fullPath    ruta donde se almacenara el archivo
     * @param inputStream archivo a subir
     * @return direccion del archivo guardado
     */
    URI uploadFileToBucketWithURL(String bucketName, String fullPath, InputStream inputStream);
}
