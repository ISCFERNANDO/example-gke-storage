package com.fernando.examplegke;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentoServiceImpl implements IDocumentoService {

    private final StorageService storageService;


    /**
     * Realiza la descarga de un documento en GCS
     *
     * @param bucketName nombre del bucket donde esta el archivo
     * @param url        url del documento
     * @return Retorna el recurso encontrado
     */
    @Override
    public Resource find(String bucketName, String url) {
        log.info("***** BUSCANDO EL DOCUMENTO,bucket: {}, url: {} ***** ", bucketName, url);
        ByteArrayOutputStream outputStream = this.storageService.getFileFromStorage(bucketName, url);
        return new ByteArrayResource(outputStream.toByteArray());
    }

    /**
     * Realiza el guardado de un archivo en GCS
     *
     * @param bucketName  nombre del bucket
     * @param fullPath    ruta del archivo
     * @param inputStream bytes del archivo a subir
     * @return ruta del archivo almacenad
     */
    @Override
    public String uploadFileToBucketWithURL(String bucketName, String fullPath, InputStream inputStream) {
        URI uri = this.storageService.uploadFileToBucketWithURL(bucketName, fullPath, inputStream);
        return uri.toString();
    }
}
