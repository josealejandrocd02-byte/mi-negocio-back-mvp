package com.minegocio.mi_negocio.service.SaleServis;

import static com.minegocio.mi_negocio.util.ApiErrors.conflict;
import static com.minegocio.mi_negocio.util.ApiErrors.forbidden;
import static com.minegocio.mi_negocio.util.ApiErrors.notFound;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import com.minegocio.mi_negocio.dtos.saleitem.SaleItemCreateRequest;
import com.minegocio.mi_negocio.dtos.saleitem.SaleItemUpdateRequest;
import com.minegocio.mi_negocio.service.SaleItemServi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.minegocio.mi_negocio.dtos.sale.SaleItemResponse;

import com.minegocio.mi_negocio.emus.SaleStatus;
import com.minegocio.mi_negocio.entity.Product;
import com.minegocio.mi_negocio.entity.Sale;
import com.minegocio.mi_negocio.entity.SaleItem;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.repository.ProductRepository;
import com.minegocio.mi_negocio.repository.SaleItemRepository;
import com.minegocio.mi_negocio.repository.SaleRepository;
import com.minegocio.mi_negocio.repository.StoreUserRepository;
import com.minegocio.mi_negocio.repository.UserRepository;

@Service
public class SaleItemServiceImpl implements SaleItemServi {

    private final SaleItemRepository saleItemRepository;
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StoreUserRepository storeUserRepository;

    public SaleItemServiceImpl(SaleItemRepository saleItemRepository,
                               SaleRepository saleRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository,
                               StoreUserRepository storeUserRepository) {
        this.saleItemRepository = saleItemRepository;
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.storeUserRepository = storeUserRepository;
    }

    @Override
    @Transactional
    public SaleItemResponse addItemToSale(UUID saleId, SaleItemCreateRequest request, UUID requesterId, boolean superAdmin) {
        Sale sale = findAccessibleSale(saleId, requesterId, superAdmin);

        if (sale.getStatus() == SaleStatus.CANCELLED || sale.getStatus() == SaleStatus.COMPLETED) {
            throw conflict("No se pueden agregar productos a una venta en estado " + sale.getStatus());
        }

        Product product = productRepository.findByIdAndStore_Id(request.getProductId(), sale.getStore().getId())
                .orElseThrow(() -> notFound("Producto no encontrado en esta sucursal"));

        if (!product.isActive()) {
            throw conflict("El producto '" + product.getName() + "' esta inactivo");
        }

        // Si el producto ya existe en la venta, sumamos la cantidad; si no, lo agregamos como nuevo ítem
        SaleItem existingItem = sale.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        SaleItem savedItem;
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            savedItem = existingItem;
        } else {
            SaleItem newItem = new SaleItem();
            newItem.setSale(sale);
            newItem.setStoreId(sale.getStore().getId()); // Asignación automática del storeId de la venta
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setUnitPrice(product.getPrice());
            sale.getItems().add(newItem);
            savedItem = newItem;
        }

        recalculateSaleTotal(sale);
        saleRepository.save(sale);

        return toResponse(savedItem);
    }

    @Override
    @Transactional
    public SaleItemResponse updateSaleItem(UUID itemId, SaleItemUpdateRequest request, UUID requesterId, boolean superAdmin) {
        SaleItem item = saleItemRepository.findByIdWithDetails(itemId)
                .orElseThrow(() -> notFound("Item de venta no encontrado"));

        assertStoreAccess(item.getSale().getStore(), requesterId, superAdmin);

        if (item.getSale().getStatus() == SaleStatus.CANCELLED || item.getSale().getStatus() == SaleStatus.COMPLETED) {
            throw conflict("No se pueden modificar ítems de una venta en estado " + item.getSale().getStatus());
        }

        item.setQuantity(request.getQuantity());
        recalculateSaleTotal(item.getSale());
        saleRepository.save(item.getSale());

        return toResponse(item);
    }

    @Override
    @Transactional
    public void deleteSaleItem(UUID itemId, UUID requesterId, boolean superAdmin) {
        SaleItem item = saleItemRepository.findByIdWithDetails(itemId)
                .orElseThrow(() -> notFound("Item de venta no encontrado"));

        Sale sale = item.getSale();
        assertStoreAccess(sale.getStore(), requesterId, superAdmin);

        if (sale.getStatus() == SaleStatus.CANCELLED || sale.getStatus() == SaleStatus.COMPLETED) {
            throw conflict("No se pueden eliminar ítems de una venta en estado " + sale.getStatus());
        }

        sale.getItems().remove(item);
        recalculateSaleTotal(sale);
        saleRepository.save(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleItemResponse> listItemsBySale(UUID saleId, UUID requesterId, boolean superAdmin) {
        Sale sale = findAccessibleSale(saleId, requesterId, superAdmin);
        return sale.getItems().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaleItemResponse getItemById(UUID itemId, UUID requesterId, boolean superAdmin) {
        SaleItem item = saleItemRepository.findByIdWithDetails(itemId)
                .orElseThrow(() -> notFound("Item de venta no encontrado"));
        assertStoreAccess(item.getSale().getStore(), requesterId, superAdmin);
        return toResponse(item);
    }

    // Helpers privados
    private void recalculateSaleTotal(Sale sale) {
        BigDecimal total = sale.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sale.setTotal(total.setScale(2, RoundingMode.HALF_UP));
    }

    private Sale findAccessibleSale(UUID saleId, UUID requesterId, boolean superAdmin) {
        Sale sale = saleRepository.findByIdWithItems(saleId)
                .orElseThrow(() -> notFound("Venta no encontrada"));
        assertStoreAccess(sale.getStore(), requesterId, superAdmin);
        return sale;
    }

    private void assertStoreAccess(Store store, UUID requesterId, boolean superAdmin) {
        if (superAdmin) {
            return;
        }
        if (!userRepository.existsByIdAndActiveTrue(requesterId)) {
            throw forbidden("Tu cuenta esta bloqueada");
        }

        boolean owner = store.getOwnerUser().getId().equals(requesterId);
        if (!owner && !storeUserRepository.existsByStore_IdAndUser_IdAndActiveTrue(store.getId(), requesterId)) {
            throw forbidden("No tienes acceso a esta sucursal");
        }
    }

    private SaleItemResponse toResponse(SaleItem item) {
        return new SaleItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getCreatedAt()
        );
    }
}