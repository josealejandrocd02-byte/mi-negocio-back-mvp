package com.minegocio.mi_negocio.service;

import static com.minegocio.mi_negocio.util.ApiErrors.conflict;
import static com.minegocio.mi_negocio.util.ApiErrors.notFound;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minegocio.mi_negocio.dtos.catalog.CategoryCreateRequest;
import com.minegocio.mi_negocio.dtos.catalog.CategoryResponse;
import com.minegocio.mi_negocio.dtos.catalog.CategoryUpdateRequest;
import com.minegocio.mi_negocio.entity.Category;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.repository.CategoryRepository;
import com.minegocio.mi_negocio.util.CatalogMapper;

/**
 * Catalogo de categorias de una sucursal (RF-19, RF-22, RF-23).
 * - STORE_ADMIN: crea, edita y activa/desactiva (nunca borra).
 * - EMPLOYEE: solo consulta las categorias activas de su sucursal.
 * Aislamiento: toda busqueda por id exige que la categoria sea de {storeId}.
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CatalogAccess access;

    public CategoryService(CategoryRepository categoryRepository, CatalogAccess access) {
        this.categoryRepository = categoryRepository;
        this.access = access;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(UUID storeId, UUID callerId) {
        access.requireMember(storeId, callerId);
        boolean manager = access.isManager(storeId, callerId);

        List<Category> categories = manager
                ? categoryRepository.findByStore_IdOrderByNameAsc(storeId)
                : categoryRepository.findByStore_IdAndActiveTrueOrderByNameAsc(storeId);

        return categories.stream().map(CatalogMapper::toResponse).toList();
    }

    @Transactional
    public CategoryResponse create(UUID storeId, UUID callerId, CategoryCreateRequest req) {
        Store store = access.requireManager(storeId, callerId);

        String name = req.getName().trim();
        if (categoryRepository.existsByStore_IdAndNameIgnoreCase(storeId, name)) {
            throw conflict("Ya existe una categoria con ese nombre en esta sucursal");
        }

        Category category = new Category();
        category.setStore(store);
        category.setName(name);
        category.setActive(req.getActive() == null || req.getActive());
        categoryRepository.save(category);

        return CatalogMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse update(UUID storeId, UUID callerId, UUID categoryId, CategoryUpdateRequest req) {
        access.requireManager(storeId, callerId);
        Category category = findInStore(storeId, categoryId);

        String name = req.getName().trim();
        if (categoryRepository.existsByStore_IdAndNameIgnoreCaseAndIdNot(storeId, name, categoryId)) {
            throw conflict("Ya existe otra categoria con ese nombre en esta sucursal");
        }

        category.setName(name);
        return CatalogMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse setActive(UUID storeId, UUID callerId, UUID categoryId, boolean active) {
        access.requireManager(storeId, callerId);
        Category category = findInStore(storeId, categoryId);
        category.setActive(active);
        return CatalogMapper.toResponse(category);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Category findInStore(UUID storeId, UUID categoryId) {
        return categoryRepository.findByIdAndStore_Id(categoryId, storeId)
                .orElseThrow(() -> notFound("Categoria no encontrada en esta sucursal"));
    }
}
