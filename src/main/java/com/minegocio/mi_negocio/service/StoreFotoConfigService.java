package com.minegocio.mi_negocio.service;

import static com.minegocio.mi_negocio.util.ApiErrors.conflict;
import static com.minegocio.mi_negocio.util.ApiErrors.forbidden;
import static com.minegocio.mi_negocio.util.ApiErrors.notFound;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.minegocio.mi_negocio.cloudinary.CloudinaryImage;
import com.minegocio.mi_negocio.cloudinary.CloudinaryService;
import com.minegocio.mi_negocio.dtos.store.PhotoResponse;
import com.minegocio.mi_negocio.emus.StoreRole;
import com.minegocio.mi_negocio.emus.StoreStatus;
import com.minegocio.mi_negocio.entity.Product;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.repository.ProductRepository;
import com.minegocio.mi_negocio.repository.StoreRepository;
import com.minegocio.mi_negocio.repository.StoreUserRepository;
import com.minegocio.mi_negocio.repository.UserRepository;

/**
 * Fotos de una sucursal: logo de la tienda y foto de cada producto (Cloudinary).
 * - Logo: puede gestionarlo el SUPER_ADMIN o un STORE_ADMIN activo de esa sucursal.
 * - Foto de producto: solo un STORE_ADMIN activo de esa sucursal (el catalogo no lo gestiona SUPER_ADMIN).
 * El id de quien llama sale siempre del token JWT.
 */
@Service
public class StoreFotoConfigService {

    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    public StoreFotoConfigService(StoreRepository storeRepository,
                                  StoreUserRepository storeUserRepository,
                                  UserRepository userRepository,
                                  ProductRepository productRepository,
                                  CloudinaryService cloudinaryService) {
        this.storeRepository = storeRepository;
        this.storeUserRepository = storeUserRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // =====================================================
    // LOGO DE LA SUCURSAL
    // =====================================================

    @Transactional
    public PhotoResponse uploadLogo(UUID storeId, UUID callerId, boolean superAdmin, MultipartFile file) {
        Store store = requireManager(storeId, callerId, superAdmin);

        CloudinaryImage uploaded = cloudinaryService.uploadStoreLogo(storeId, file);
        String oldPublicId = store.getLogoPublicId();

        store.setLogoUrl(uploaded.getUrl());
        store.setLogoPublicId(uploaded.getPublicId());
        try {
            storeRepository.saveAndFlush(store);
        } catch (RuntimeException e) {
            cloudinaryService.deleteImageQuietly(uploaded.getPublicId()); // no dejar huerfana la nueva
            throw e;
        }

        deleteAfterCommit(oldPublicId); // la anterior se borra solo si la BD se confirmo
        return new PhotoResponse(uploaded.getUrl());
    }

    @Transactional
    public void deleteLogo(UUID storeId, UUID callerId, boolean superAdmin) {
        Store store = requireManager(storeId, callerId, superAdmin);

        String oldPublicId = store.getLogoPublicId();
        store.setLogoUrl(null);
        store.setLogoPublicId(null);

        deleteAfterCommit(oldPublicId);
    }

    // =====================================================
    // FOTO DE PRODUCTO
    // =====================================================

    @Transactional
    public PhotoResponse uploadProductPhoto(UUID storeId, UUID productId, UUID callerId, MultipartFile file) {
        // El catalogo no lo gestiona SUPER_ADMIN: solo STORE_ADMIN de la sucursal.
        requireManager(storeId, callerId, false);
        Product product = findProduct(storeId, productId);

        CloudinaryImage uploaded = cloudinaryService.uploadProductImage(storeId, file);
        String oldPublicId = product.getImagePublicId();

        product.setImageUrl(uploaded.getUrl());
        product.setImagePublicId(uploaded.getPublicId());
        try {
            productRepository.saveAndFlush(product);
        } catch (RuntimeException e) {
            cloudinaryService.deleteImageQuietly(uploaded.getPublicId());
            throw e;
        }

        deleteAfterCommit(oldPublicId);
        return new PhotoResponse(uploaded.getUrl());
    }

    @Transactional
    public void deleteProductPhoto(UUID storeId, UUID productId, UUID callerId) {
        // El catalogo no lo gestiona SUPER_ADMIN: solo STORE_ADMIN de la sucursal.
        requireManager(storeId, callerId, false);
        Product product = findProduct(storeId, productId);

        String oldPublicId = product.getImagePublicId();
        product.setImageUrl(null);
        product.setImagePublicId(null);

        deleteAfterCommit(oldPublicId);
    }

    // =====================================================
    // HELPERS
    // =====================================================

    // SUPER_ADMIN: cualquier sucursal no cerrada.
    // STORE_ADMIN: debe estar activo en esa sucursal, con la cuenta y la sucursal activas.
    private Store requireManager(UUID storeId, UUID callerId, boolean superAdmin) {
        if (!superAdmin) {
            boolean allowed = userRepository.existsByIdAndActiveTrue(callerId)
                    && storeUserRepository.existsByStore_IdAndUser_IdAndRoleAndActiveTrue(
                            storeId, callerId, StoreRole.STORE_ADMIN);
            if (!allowed) {
                throw forbidden("No tienes permisos de administrador en esta sucursal");
            }
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> notFound("Sucursal no encontrada"));

        if (store.getStatus() == StoreStatus.CLOSED) {
            throw conflict("La sucursal esta cerrada");
        }
        if (!superAdmin && store.getStatus() != StoreStatus.ACTIVE) {
            throw conflict("La sucursal no esta activa");
        }
        return store;
    }

    // El producto debe pertenecer a esa sucursal (aislamiento entre sucursales)
    private Product findProduct(UUID storeId, UUID productId) {
        return productRepository.findByIdAndStore_Id(productId, storeId)
                .orElseThrow(() -> notFound("Producto no encontrado en esta sucursal"));
    }

    // Borra la imagen vieja de Cloudinary solo despues de confirmar la transaccion
    private void deleteAfterCommit(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cloudinaryService.deleteImageQuietly(publicId);
                }
            });
        } else {
            cloudinaryService.deleteImageQuietly(publicId);
        }
    }
}
