//package com.fernando.examplegke;
package com.fernando.examplegke;;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/moto/documento/")
@RequiredArgsConstructor
public class DocumentoController {

    private final IDocumentoService documentoService;
    @Value("${app.gcs.bucket.name}")
    private String bucketName;

    /**
     * Realiza la descarga de un doducmento
     *
     * @param url url del documento
     * @return
     */
    @GetMapping("download/{url}")
    public ResponseEntity<Resource> download(@PathVariable String url) {
        log.info("***** DESCARGANDO  DOCUMENTO *****");
        String listo = "LISTO";
        Resource resource = this.documentoService.find(this.bucketName, url);
        return new ResponseEntity<>(resource, HttpStatus.ACCEPTED);
    }

    /**
     * Realiza el guardado de un archivo en GCS
     *
     * @param key archivo a subir
     * @return Ruta del documento guardado
     */
    @PostMapping("upload")
    public ResponseEntity<String> upload(@RequestPart(name = "file") MultipartFile key) {
        String path = GCSUtil.buildRoute(key);
        try {
            String url = this.documentoService.uploadFileToBucketWithURL(this.bucketName, path, key.getInputStream());
            log.info("URL: {}", url);
            return new ResponseEntity<>(url, HttpStatus.ACCEPTED);
        } catch (IOException e) {
            throw new IllegalArgumentException("Ocurrio un error al subir el archivo");
        }
    }
}
