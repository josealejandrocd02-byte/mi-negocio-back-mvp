package com.minegocio.mi_negocio.cloudinary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    // ---------- limites ----------
    private static final long MAX_LOGO_SIZE = 5L * 1024 * 1024;
    private static final long MAX_PRODUCT_SIZE = 8L * 1024 * 1024;

    private static final int MIN_WIDTH = 100;
    private static final int MIN_HEIGHT = 100;
    private static final int MAX_WIDTH = 5000;
    private static final int MAX_HEIGHT = 5000;

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/jfif", "image/pjpeg");

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "jfif");

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // =====================================================
    // SUBIDAS
    // =====================================================

    public CloudinaryImage uploadStoreLogo(UUID storeId, MultipartFile file) {
        byte[] bytes = validateAndRead(file, MAX_LOGO_SIZE);
        return upload(bytes, "minegocio/stores/" + storeId + "/logo", "logo",
                "w_400,h_400,c_fill,q_auto:good,f_auto");
    }

    public CloudinaryImage uploadProductImage(UUID storeId, MultipartFile file) {
        byte[] bytes = validateAndRead(file, MAX_PRODUCT_SIZE);
        return upload(bytes, "minegocio/stores/" + storeId + "/products", "prod",
                "w_1200,h_1200,c_limit,q_auto:eco,f_auto");
    }

    private CloudinaryImage upload(byte[] bytes, String folder, String prefix, String transformation) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", prefix + "_" + UUID.randomUUID(),
                    "resource_type", "image",
                    "overwrite", false,
                    "transformation", transformation));

            return new CloudinaryImage(
                    String.valueOf(result.get("secure_url")),
                    String.valueOf(result.get("public_id")));

        } catch (Exception e) {
            log.error("Error subiendo imagen a Cloudinary", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo subir la imagen. Intenta de nuevo");
        }
    }

    // =====================================================
    // BORRADO
    // =====================================================

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
            String status = String.valueOf(result.get("result"));
            if (!"ok".equals(status) && !"not found".equals(status)) {
                throw new IllegalStateException("Cloudinary devolvio: " + status);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error eliminando imagen: " + publicId, e);
        }
    }

    // Para limpiezas: si falla, solo se registra (no debe romper la operacion principal)
    public void deleteImageQuietly(String publicId) {
        try {
            deleteImage(publicId);
        } catch (Exception e) {
            log.warn("No se pudo eliminar la imagen {} de Cloudinary", publicId, e);
        }
    }

    // =====================================================
    // VALIDACION
    // =====================================================

    private byte[] validateAndRead(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw badRequest("Imagen vacia");
        }

        if (file.getSize() > maxSize) {
            throw badRequest("Imagen muy pesada (max " + (maxSize / (1024 * 1024)) + "MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw badRequest("Formato invalido. Solo JPG, PNG o WEBP");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw badRequest("Nombre de archivo invalido");
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw badRequest("Extension invalida");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw badRequest("No se pudo leer el archivo");
        }

        String type = detectType(bytes);
        if (type == null) {
            throw badRequest("El archivo no es una imagen valida");
        }

        if (!"webp".equals(type)) {
            validateDimensions(bytes);
        }

        return bytes;
    }

    private String detectType(byte[] b) {
        if (b.length > 3
                && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpeg";
        }
        if (b.length > 8
                && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "png";
        }
        if (b.length > 12
                && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }

    private void validateDimensions(byte[] bytes) {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                throw badRequest("Formato de imagen no soportado");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(in);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width < MIN_WIDTH || height < MIN_HEIGHT) {
                    throw badRequest("Imagen demasiado pequena (minimo " + MIN_WIDTH + "x" + MIN_HEIGHT + ")");
                }
                if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                    throw badRequest("Imagen demasiado grande (maximo " + MAX_WIDTH + "x" + MAX_HEIGHT + ")");
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw badRequest("Imagen danada o invalida");
        }
    }

    private static ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}