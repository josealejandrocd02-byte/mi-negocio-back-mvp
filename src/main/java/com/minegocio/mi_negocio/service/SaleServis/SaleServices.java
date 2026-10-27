package com.minegocio.mi_negocio.service.SaleServis;

import static com.minegocio.mi_negocio.util.ApiErrors.conflict;
import static com.minegocio.mi_negocio.util.ApiErrors.forbidden;
import static com.minegocio.mi_negocio.util.ApiErrors.notFound;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.minegocio.mi_negocio.service.SaleServi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minegocio.mi_negocio.dtos.sale.SaleCreateRequest;
import com.minegocio.mi_negocio.dtos.sale.SaleItemRequest;
import com.minegocio.mi_negocio.dtos.sale.SaleItemResponse;
import com.minegocio.mi_negocio.dtos.sale.SaleResponse;
import com.minegocio.mi_negocio.dtos.sale.SaleStatusRequest;
import com.minegocio.mi_negocio.dtos.sale.SaleUpdateRequest;
import com.minegocio.mi_negocio.emus.SaleStatus;
import com.minegocio.mi_negocio.emus.StoreStatus;
import com.minegocio.mi_negocio.entity.CustomerOrder;
import com.minegocio.mi_negocio.entity.Product;
import com.minegocio.mi_negocio.entity.Sale;
import com.minegocio.mi_negocio.entity.SaleItem;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.entity.User;
import com.minegocio.mi_negocio.repository.CustomerOrderRepository;
import com.minegocio.mi_negocio.repository.ProductRepository;
import com.minegocio.mi_negocio.repository.SaleRepository;
import com.minegocio.mi_negocio.repository.StoreRepository;
import com.minegocio.mi_negocio.repository.StoreUserRepository;
import com.minegocio.mi_negocio.repository.UserRepository;

/**
 * Gestion de ventas.
 * Todas las operaciones validan que el usuario del token sea dueño de la
 * sucursal, miembro activo de ella, o SUPER_ADMIN.
 * Los precios y el total SIEMPRE se calculan con el catalogo de la BD.
 */
@Service
public class SaleServices implements SaleServi {

    private final SaleRepository saleRepository;
    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final ProductRepository productRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final UserRepository userRepository;

    public SaleServices(SaleRepository saleRepository,
                        StoreRepository storeRepository,
                        StoreUserRepository storeUserRepository,
                        ProductRepository productRepository,
                        CustomerOrderRepository customerOrderRepository,
                        UserRepository userRepository) {
        this.saleRepository = saleRepository;
        this.storeRepository = storeRepository;
        this.storeUserRepository = storeUserRepository;
        this.productRepository = productRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.userRepository = userRepository;
    }

    // =====================================================
    // CREATE
    // =====================================================

    @Override
    @Transactional
    public SaleResponse createSale(SaleCreateRequest req, UUID requesterId, boolean superAdmin) {
        Store store = findStore(req.getStoreId());
        assertStoreAccess(store, requesterId, superAdmin);

        if (store.getStatus() == StoreStatus.CLOSED) {
            throw conflict("La sucursal '" + store.getName() + "' esta cerrada");
        }

        // CAMBIO: Validar estrictamente que se envíe el ID de la orden
        if (req.getOrderId() == null) {
            throw conflict("Es obligatorio asociar una orden para poder registrar una venta");
        }

        CustomerOrder order = customerOrderRepository.findByIdAndStore_Id(req.getOrderId(), store.getId())
                .orElseThrow(() -> notFound("Pedido no encontrado en esta sucursal"));

        User soldBy = userRepository.findById(requesterId)
                .orElseThrow(() -> notFound("Usuario no encontrado"));

        Sale sale = new Sale();
        sale.setStore(store);
        sale.setOrder(order); // Asignación obligatoria
        sale.setSoldBy(soldBy);
        sale.setStatus(SaleStatus.PENDING);

        applyItems(sale, store, req.getItems());
        saleRepository.saveAndFlush(sale);
        return toResponse(sale);
    }

    // =====================================================
    // UPDATE
    // =====================================================

    @Override
    @Transactional
    public SaleResponse updateSale(UUID id, SaleUpdateRequest req, UUID requesterId, boolean superAdmin) {
        Sale sale = findAccessibleSale(id, requesterId, superAdmin);

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw conflict("No se puede modificar una venta cancelada");
        }

        applyItems(sale, sale.getStore(), req.getItems());
        saleRepository.flush();
        return toResponse(sale);
    }

    // =====================================================
    // ESTADO Y CANCELACION LOGICA
    // =====================================================

    @Override
    @Transactional
    public SaleResponse changeStatus(UUID id, SaleStatusRequest req, UUID requesterId, boolean superAdmin) {
        Sale sale = findAccessibleSale(id, requesterId, superAdmin);
        updateStatus(sale, req.getStatus());
        return toResponse(sale);
    }

    @Override
    @Transactional
    public SaleResponse cancelSale(UUID id, UUID requesterId, boolean superAdmin) {
        Sale sale = findAccessibleSale(id, requesterId, superAdmin);

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw conflict("La venta ya esta cancelada");
        }

        sale.setStatus(SaleStatus.CANCELLED);
        return toResponse(sale);
    }

    // =====================================================
    // DELETE
    // =====================================================

    @Override
    @Transactional
    public void deleteSale(UUID id, UUID requesterId, boolean superAdmin) {
        Sale sale = findAccessibleSale(id, requesterId, superAdmin);

        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw conflict("Una venta completada no puede eliminarse; cancelala en lugar de eliminarla");
        }

        saleRepository.delete(sale);
    }

    // =====================================================
    // CONSULTAS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getSaleById(UUID id, UUID requesterId, boolean superAdmin) {
        Sale sale = findAccessibleSale(id, requesterId, superAdmin);
        return toResponse(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> listSalesByStore(UUID storeId, UUID requesterId, boolean superAdmin) {
        Store store = findStore(storeId);
        assertStoreAccess(store, requesterId, superAdmin);

        return saleRepository.findByStoreWithItems(storeId).stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void applyItems(Sale sale, Store store, List<SaleItemRequest> requests) {
        Map<UUID, Integer> quantities = mergeQuantities(requests);
        Map<UUID, Product> products = productRepository.findByStore_IdAndIdIn(store.getId(), quantities.keySet())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        sale.getItems().clear();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> entry : quantities.entrySet()) {
            Product product = products.get(entry.getKey());
            if (product == null) {
                throw notFound("Producto no encontrado en esta sucursal: " + entry.getKey());
            }
            if (!product.isActive()) {
                throw conflict("El producto '" + product.getName() + "' esta inactivo");
            }

            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setStoreId(store.getId());
            item.setProduct(product);
            item.setQuantity(entry.getValue());
            item.setUnitPrice(product.getPrice());
            sale.getItems().add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }

        sale.setTotal(total.setScale(2, RoundingMode.HALF_UP));
    }

    private Map<UUID, Integer> mergeQuantities(List<SaleItemRequest> items) {
        Map<UUID, Integer> quantities = new LinkedHashMap<>();
        for (SaleItemRequest item : items) {
            quantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        return quantities;
    }

    private void updateStatus(Sale sale, SaleStatus newStatus) {
        SaleStatus current = sale.getStatus();

        if (current == newStatus) {
            throw conflict("La venta ya esta en estado " + current);
        }
        if (current == SaleStatus.CANCELLED) {
            throw conflict("Una venta cancelada no puede cambiar de estado");
        }
        if (current == SaleStatus.COMPLETED && newStatus == SaleStatus.PENDING) {
            throw conflict("Una venta completada no puede volver a PENDING");
        }

        sale.setStatus(newStatus);
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

    private Store findStore(UUID storeId) {
        return storeRepository.findByIdWithOwner(storeId)
                .orElseThrow(() -> notFound("Sucursal no encontrada"));
    }

    private Sale findAccessibleSale(UUID id, UUID requesterId, boolean superAdmin) {
        Sale sale = saleRepository.findByIdWithItems(id)
                .orElseThrow(() -> notFound("Venta no encontrada"));
        assertStoreAccess(sale.getStore(), requesterId, superAdmin);
        return sale;
    }

    private SaleResponse toResponse(Sale sale) {
        List<SaleItemResponse> items = sale.getItems().stream()
                .map(item -> new SaleItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getCreatedAt()))
                .toList();

        return new SaleResponse(
                sale.getId(),
                sale.getStore().getId(),
                sale.getStore().getName(),
                sale.getOrder() != null ? sale.getOrder().getId() : null,
                sale.getSoldBy().getId(),
                sale.getSoldBy().getFullName(),
                sale.getTotal(),
                sale.getStatus(),
                sale.getCreatedAt(),
                items);
    }
}