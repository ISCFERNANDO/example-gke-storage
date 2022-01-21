package com.fernando.examplegke;

import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface IDocumentoService {
    /**
     * Realiza la descarga de un documento en GCS
     *
     * @param bucketName nombre del bucket donde esta el archivo
     * @param url        url del documento
     * @return
     */
    Resource find(String bucketName, String url);

    /**
     * Realiza el guardado de un archivo en GCS
     *
     * @param bucketName  nombre del bucket
     * @param fullPath    ruta del archivo
     * @param inputStream bytes del archivo a subir
     * @return ruta del archivo almacenad
     */
    String uploadFileToBucketWithURL(String bucketName, String fullPath, InputStream inputStream);
}
