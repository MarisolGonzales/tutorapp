package com.tutorapp.tutorapp.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.common.collect.ImmutableSet;

@Service
public class FotoPerfilService {

    private static final Logger log = LoggerFactory.getLogger(FotoPerfilService.class);

    // Carpeta lógica dentro de Cloudinary
    private static final String CARPETA_CLOUDINARY = "tutorapp/fotos-perfil";

    // ImmutableSet: colección inmutable, no puede alterarse en tiempo de ejecución
    private static final Set<String> TIPOS_PERMITIDOS =
            ImmutableSet.of("image/jpeg", "image/png", "image/webp");
    private static final long TAMANO_MAXIMO = 2 * 1024 * 1024; // 2 MB

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Sube la foto a Cloudinary y devuelve su URL pública (https), que se guarda
     * en el campo fotoPerfil del alumno o tutor.
     * Lanza IllegalArgumentException si el archivo no es una imagen válida.
     */
    public String guardar(MultipartFile foto) {
        if (!TIPOS_PERMITIDOS.contains(foto.getContentType())) {
            throw new IllegalArgumentException("La foto debe ser JPG, PNG o WEBP.");
        }
        if (foto.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("La foto no puede pesar más de 2 MB.");
        }

        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(
                    foto.getBytes(),
                    ObjectUtils.asMap(
                            "folder", CARPETA_CLOUDINARY,
                            "public_id", UUID.randomUUID().toString(),
                            "resource_type", "image"));
            String url = resultado.get("secure_url").toString();
            log.info("Foto de perfil subida correctamente a Cloudinary");
            return url;
        } catch (IOException e) {
            log.error("Error al subir la foto de perfil a Cloudinary", e);
            throw new IllegalArgumentException("No se pudo guardar la foto. Intenta de nuevo.");
        }
    }
}
