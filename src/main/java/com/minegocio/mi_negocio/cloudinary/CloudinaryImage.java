package com.minegocio.mi_negocio.cloudinary;

// Resultado de subir una imagen: url publica + id para poder borrarla despues
public class CloudinaryImage {

    private final String url;
    private final String publicId;

    public CloudinaryImage(String url, String publicId) {
        this.url = url;
        this.publicId = publicId;
    }

    public String getUrl() { return url; }
    public String getPublicId() { return publicId; }
}
