package com.minegocio.mi_negocio.service;

import static com.minegocio.mi_negocio.util.ApiErrors.conflict;
import static com.minegocio.mi_negocio.util.ApiErrors.notFound;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minegocio.mi_negocio.dtos.catalog.ProductCreateRequest;
import com.minegocio.mi_negocio.dtos.catalog.ProductResponse;
import com.minegocio.mi_negocio.dtos.catalog.ProductUpdateRequest;
import com.minegocio.mi_negocio.entity.Category;
import com.minegocio.mi_negocio.entity.Product;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.repository.CategoryRepository;
import com.minegocio.mi_negocio.repository.ProductRepository;
import com.minegocio.mi_negocio.repository.StoreRepository;
import com.minegocio.mi_negocio.repository.StoreUserRepository;
import com.minegocio.mi_negocio.util.CatalogMapper;

/**
 * Catalogo de productos de una sucursal, SIN control de stock (RF-19..RF-23).
 * - STORE_ADMIN: crea, edita y activa/desactiva (nunca borra, RF-21).
 * - EMPLOYEE: solo consulta los productos activos de su sucursal (RF-22).
 * Aislamiento: todo acceso exige que el producto y su categoria sean de {storeId} (RF-23).
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeUserRepository;
    
    private final CatalogAccess access;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          StoreRepository storeUserRepository,
                          CatalogAccess access) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.storeUserRepository = storeUserRepository;
        this.access = access;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(UUID storeId, UUID callerId, UUID categoryId, String q) {
        access.requireMember(storeId, callerId);
        boolean manager = access.isManager(storeId, callerId);

        List<Product> products = load(storeId, categoryId, manager);
        String search = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);

        return products.stream()
                .filter(p -> search == null || p.getName().toLowerCase(Locale.ROOT).contains(search))
                .map(CatalogMapper::toResponse)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponse> listEmployee( UUID callerId, UUID categoryId, String q) {
        UUID storeId = storeUserRepository.findStoreIdByUserId(callerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El usuario no pertenece a ninguna tienda"
                        )
                );
        access.requireMember(storeId, callerId);
        boolean manager = access.isManager(storeId, callerId);

        List<Product> products = load(storeId, categoryId, manager);
        String search = (q == null || q.isBlank()) ? null : q.trim().toLowerCase(Locale.ROOT);

        return products.stream()
                .filter(p -> search == null || p.getName().toLowerCase(Locale.ROOT).contains(search))
                .map(CatalogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID storeId, UUID callerId, UUID productId) {
        access.requireMember(storeId, callerId);
        Product product = findInStore(storeId, productId);
        if (!access.isManager(storeId, callerId)
                && (!product.isActive()
                        || (product.getCategory() != null && !product.getCategory().isActive()))) {
            throw notFound("Producto no encontrado en esta sucursal");
        }
        return CatalogMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse create(UUID storeId, UUID callerId, ProductCreateRequest req) {
        Store store = access.requireManager(storeId, callerId);

        String name = req.getName().trim();
        if (productRepository.existsByStore_IdAndNameIgnoreCase(storeId, name)) {
            throw conflict("Ya existe un producto con ese nombre en esta sucursal");
        }

        Product product = new Product();
        product.setStore(store);
        product.setName(name);
        product.setCategory(resolveCategory(storeId, req.getCategoryId()));
        product.setPrice(req.getPrice());
        product.setActive(req.getActive() == null || req.getActive());
        // Flush para que @CreationTimestamp se materialice antes de mapear la respuesta.
        productRepository.saveAndFlush(product);

        return CatalogMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse update(UUID storeId, UUID callerId, UUID productId, ProductUpdateRequest req) {
        access.requireManager(storeId, callerId);
        Product product = findInStore(storeId, productId);

        String name = req.getName().trim();
        if (productRepository.existsByStore_IdAndNameIgnoreCaseAndIdNot(storeId, name, productId)) {
            throw conflict("Ya existe otro producto con ese nombre en esta sucursal");
        }

        product.setName(name);
        product.setCategory(resolveCategory(storeId, req.getCategoryId()));
        product.setPrice(req.getPrice());

        return CatalogMapper.toResponse(product);
    }

    // Ocultar un producto agotado = marcarlo inactivo (RF-21)
    @Transactional
    public ProductResponse setActive(UUID storeId, UUID callerId, UUID productId, boolean active) {
        access.requireManager(storeId, callerId);
        Product product = findInStore(storeId, productId);
        product.setActive(active);
        return CatalogMapper.toResponse(product);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private List<Product> load(UUID storeId, UUID categoryId, boolean manager) {
        return productRepository.findForList(storeId, categoryId, !manager);
    }

    // La categoria, si se indica, debe pertenecer a la misma sucursal (RF-23)
    private Category resolveCategory(UUID storeId, UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findByIdAndStore_Id(categoryId, storeId)
                .orElseThrow(() -> notFound("Categoria no encontrada en esta sucursal"));
    }

    private Product findInStore(UUID storeId, UUID productId) {
        return productRepository.findDetail(productId, storeId)
                .orElseThrow(() -> notFound("Producto no encontrado en esta sucursal"));
    }
}
