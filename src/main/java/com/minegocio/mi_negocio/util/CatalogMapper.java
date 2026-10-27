package com.minegocio.mi_negocio.util;

import com.minegocio.mi_negocio.dtos.catalog.CategoryResponse;
import com.minegocio.mi_negocio.dtos.catalog.ProductResponse;
import com.minegocio.mi_negocio.entity.Category;
import com.minegocio.mi_negocio.entity.Product;

public final class CatalogMapper {

    private CatalogMapper() {}

    // Requiere la entidad gestionada (o una transaccion abierta) para leer la categoria
    public static ProductResponse toResponse(Product p) {
        Category c = p.getCategory();
        return new ProductResponse(
                p.getId(), p.getName(),
                c != null ? c.getId() : null,
                c != null ? c.getName() : null,
                p.getPrice(), p.isActive(), p.getImageUrl(), p.getCreatedAt());
    }

    public static CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.isActive());
    }
}
