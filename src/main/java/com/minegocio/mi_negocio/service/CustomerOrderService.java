package com.minegocio.mi_negocio.service;

import static com.minegocio.mi_negocio.util.ApiErrors.badRequest;
import static com.minegocio.mi_negocio.util.ApiErrors.conflict;
import static com.minegocio.mi_negocio.util.ApiErrors.forbidden;
import static com.minegocio.mi_negocio.util.ApiErrors.notFound;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minegocio.mi_negocio.dtos.order.OrderItemRequest;
import com.minegocio.mi_negocio.dtos.order.OrderItemResponse;
import com.minegocio.mi_negocio.dtos.order.OrderRequest;
import com.minegocio.mi_negocio.dtos.order.OrderResponse;
import com.minegocio.mi_negocio.emus.OrderStatus;
import com.minegocio.mi_negocio.emus.StoreStatus;
import com.minegocio.mi_negocio.entity.CustomerOrder;
import com.minegocio.mi_negocio.entity.OrderItem;
import com.minegocio.mi_negocio.entity.Product;
import com.minegocio.mi_negocio.entity.Sale;
import com.minegocio.mi_negocio.entity.SaleItem;
import com.minegocio.mi_negocio.entity.Store;
import com.minegocio.mi_negocio.entity.User;
import com.minegocio.mi_negocio.repository.CustomerOrderRepository;
import com.minegocio.mi_negocio.repository.OrderItemRepository;
import com.minegocio.mi_negocio.repository.ProductRepository;
import com.minegocio.mi_negocio.repository.SaleRepository;
import com.minegocio.mi_negocio.repository.StoreRepository;
import com.minegocio.mi_negocio.repository.StoreUserRepository;
import com.minegocio.mi_negocio.repository.UserRepository;
import com.minegocio.mi_negocio.util.OrderMapper;

/**
 * Gestion de pedidos (orders) e items de pedido (order_items) por sucursal.
 * - Pueden operar miembros activos de la sucursal (STORE_ADMIN o EMPLOYEE) o el SUPER_ADMIN.
 * - Garantiza que OrderItem.storeId coincida siempre con el storeId del pedido y del producto
 *   (cumpliendo las llaves foraneas compuestas en PostgreSQL).
 * - Cuando un pedido pasa a estado DELIVERED, descuenta el stock de cada producto y
 *   registra automaticamente la venta en 'sales' y 'sale_items'.
 */
@Service
public class CustomerOrderService {

    private final CustomerOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final StoreUserRepository storeUserRepository;
    private final UserRepository userRepository;
    private final SaleRepository saleRepository;

    public CustomerOrderService(CustomerOrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                ProductRepository productRepository,
                                StoreRepository storeRepository,
                                StoreUserRepository storeUserRepository,
                                UserRepository userRepository,
                                SaleRepository saleRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.storeUserRepository = storeUserRepository;
        this.userRepository = userRepository;
        this.saleRepository = saleRepository;
    }

    // =====================================================
    // CONSULTAS DE PEDIDOS (orders)
    // =====================================================

    @Transactional(readOnly = true)
    public List<OrderResponse> list(UUID storeId, UUID callerId, boolean superAdmin, OrderStatus status) {
        requireStoreAccess(storeId, callerId, superAdmin);

        List<CustomerOrder> orders = (status == null)
                ? orderRepository.findAllWithDetailsByStoreId(storeId)
                : orderRepository.findAllWithDetailsByStoreIdAndStatus(storeId, status);

        return orders.stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(UUID storeId, UUID orderId, UUID callerId, boolean superAdmin) {
        requireStoreAccess(storeId, callerId, superAdmin);
        CustomerOrder order = findOrderWithDetails(storeId, orderId);
        return OrderMapper.toResponse(order);
    }

    // =====================================================
    // CRUD DE PEDIDOS (orders)
    // =====================================================

    @Transactional
    public OrderResponse create(UUID storeId, UUID callerId, boolean superAdmin, OrderRequest req) {
        Store store = requireStoreAccess(storeId, callerId, superAdmin);
        User creator = findActiveCaller(callerId);

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw badRequest("El pedido debe incluir al menos un producto");
        }

        CustomerOrder order = new CustomerOrder();
        order.setStore(store);
        order.setCreatedBy(creator);
        order.setCustomerName(blankToNull(req.getCustomerName()));
        order.setCustomerPhone(blankToNull(req.getCustomerPhone()));
        order.setDeliveryAddress(blankToNull(req.getDeliveryAddress()));
        order.setNotes(blankToNull(req.getNotes()));
        order.setStatus(OrderStatus.PENDING);

        applyItemsToOrder(order, storeId, req.getItems());

        orderRepository.save(order);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse update(UUID storeId, UUID orderId, UUID callerId, boolean superAdmin, OrderRequest req) {
        requireStoreAccess(storeId, callerId, superAdmin);
        CustomerOrder order = findOrderWithDetails(storeId, orderId);
        requireEditableOrder(order);

        order.setCustomerName(blankToNull(req.getCustomerName()));
        order.setCustomerPhone(blankToNull(req.getCustomerPhone()));
        order.setDeliveryAddress(blankToNull(req.getDeliveryAddress()));
        order.setNotes(blankToNull(req.getNotes()));

        // Si se envia la lista de items, reemplaza los items actuales y recalcula el total
        if (req.getItems() != null) {
            if (req.getItems().isEmpty()) {
                throw badRequest("El pedido no puede quedar sin productos");
            }
            applyItemsToOrder(order, storeId, req.getItems());
        }

        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(UUID storeId, UUID orderId, UUID callerId,
                                      boolean superAdmin, OrderStatus newStatus) {
        requireStoreAccess(storeId, callerId, superAdmin);
        User caller = findActiveCaller(callerId);
        CustomerOrder order = findOrderWithDetails(storeId, orderId);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw conflict("El pedido ya fue entregado y no puede cambiar de estado");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw conflict("El pedido esta cancelado y no puede cambiar de estado");
        }

        if (newStatus == OrderStatus.DELIVERED) {
            deliverOrderAndCreateSale(order, caller);
        } else {
            order.setStatus(newStatus);
        }

        return OrderMapper.toResponse(order);
    }

    @Transactional
    public void delete(UUID storeId, UUID orderId, UUID callerId, boolean superAdmin) {
        requireStoreAccess(storeId, callerId, superAdmin);
        CustomerOrder order = findOrderWithDetails(storeId, orderId);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw conflict("No se puede eliminar un pedido ya entregado porque tiene una venta asociada");
        }

        orderRepository.delete(order);
    }

    // =====================================================
    // CRUD DE ITEMS DEL PEDIDO (order_items)
    // =====================================================

    @Transactional(readOnly = true)
    public List<OrderItemResponse> listItems(UUID storeId, UUID orderId, UUID callerId, boolean superAdmin) {
        requireStoreAccess(storeId, callerId, superAdmin);
        findOrderWithDetails(storeId, orderId);
        return orderItemRepository.findWithProductByOrderIdAndStoreId(orderId, storeId).stream()
                .map(OrderMapper::toItemResponse)
                .toList();
    }

    @Transactional
    public OrderResponse addItem(UUID storeId, UUID orderId, UUID callerId,
                                 boolean superAdmin, OrderItemRequest req) {
        requireStoreAccess(storeId, callerId, superAdmin);
        CustomerOrder order = findOrderWithDetails(storeId, orderId);
        requireEditableOrder(order);

        Product product = findActiveProduct(storeId, req.getProductId());

        // Si el producto ya existe en el pedido, acumula la cantidad
        OrderItem existing = order.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int totalQuantity = req.getQuantity() + (existing != null ? existing.getQuantity() : 0);
        if (product.getStockQuantity() < totalQuantity) {
            throw conflict("Stock insuficiente para '" + product.getName()
                    + "' (disponible: " + product.getStockQuantity() + ")");
        }

        if (existing != null) {
            existing.setQuantity(totalQuantity);
            existing.setUnitPrice(product.getPrice());
        } else {
            OrderItem newItem = new OrderItem();
            newItem.setOrder(order);
            newItem.setStoreId(storeId);
            newItem.setProduct(product);
            newItem.setQuantity(req.getQuantity());
            newItem.setUnitPrice(product.getPrice());
            order.getItems().add(newItem);
        }

        recalculateTotal(order);
        orderRepository.saveAndFlush(order);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateItem(UUID storeId, UUID orderId, UUID itemId,
                                    UUID callerId, boolean superAdmin, OrderItemRequest req) {
        requireStoreAccess(storeId, callerId, superAdmin);
        CustomerOrder order = findOrderWithDetails(storeId, orderId);
        requireEditableOrder(order);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> notFound("Item no encontrado en este pedido"));

        Product product = findActiveProduct(storeId, req.getProductId());
        if (product.getStockQuantity() < req.getQuantity()) {
            throw conflict("Stock insuficiente para '" + product.getName()
                    + "' (disponible: " + product.getStockQuantity() + ")");
        }

        item.setProduct(product);
        item.setQuantity(req.getQuantity());
        item.setUnitPrice(product.getPrice());

        recalculateTotal(order);
        orderRepository.saveAndFlush(order);
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse removeItem(UUID storeId, UUID orderId, UUID itemId,
                                    UUID callerId, boolean superAdmin) {
        requireStoreAccess(storeId, callerId, superAdmin);
        CustomerOrder order = findOrderWithDetails(storeId, orderId);
        requireEditableOrder(order);

        if (order.getItems().size() <= 1) {
            throw conflict("El pedido debe conservar al menos un producto; si deseas descartarlo, cancela o elimina el pedido");
        }

        boolean removed = order.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw notFound("Item no encontrado en este pedido");
        }

        recalculateTotal(order);
        orderRepository.saveAndFlush(order);
        return OrderMapper.toResponse(order);
    }
    
    // =====================================================
    // HELPERS Y REGLAS DE NEGOCIO
    // =====================================================

    private void applyItemsToOrder(CustomerOrder order, UUID storeId, List<OrderItemRequest> requestedItems) {
        // Consolidar cantidades si el request repite el mismo productId
        Map<UUID, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (OrderItemRequest itemReq : requestedItems) {
            quantitiesByProduct.merge(itemReq.getProductId(), itemReq.getQuantity(), Integer::sum);
        }

        order.getItems().clear();

        for (Map.Entry<UUID, Integer> entry : quantitiesByProduct.entrySet()) {
            Product product = findActiveProduct(storeId, entry.getKey());
            int qty = entry.getValue();

            if (product.getStockQuantity() < qty) {
                throw conflict("Stock insuficiente para '" + product.getName()
                        + "' (disponible: " + product.getStockQuantity() + ")");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setStoreId(storeId);
            item.setProduct(product);
            item.setQuantity(qty);
            item.setUnitPrice(product.getPrice());
            order.getItems().add(item);
        }

        recalculateTotal(order);
    }

    private void recalculateTotal(CustomerOrder order) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(lineTotal);
        }
        order.setTotal(total);
    }

    /**
     * Flujo del SQL: Pedido entregado -> venta:
     * 1. UPDATE orders SET status = 'DELIVERED'
     * 2. Descontar products.stock_quantity
     * 3. INSERT INTO sales y copiar order_items a sale_items
     */
    private void deliverOrderAndCreateSale(CustomerOrder order, User caller) {
        if (order.getItems().isEmpty()) {
            throw conflict("No se puede entregar un pedido sin productos");
        }

        UUID storeId = order.getStore().getId();

        Sale sale = new Sale();
        sale.setStore(order.getStore());
        sale.setOrder(order);
        sale.setSoldBy(caller);
        sale.setTotal(order.getTotal());

        for (OrderItem orderItem : order.getItems()) {
            Product product = orderItem.getProduct();
            if (!product.isActive()) {
                throw conflict("El producto '" + product.getName() + "' ya no esta activo");
            }
            if (product.getStockQuantity() < orderItem.getQuantity()) {
                throw conflict("Stock insuficiente para entregar '" + product.getName()
                        + "' (disponible: " + product.getStockQuantity()
                        + ", requerido: " + orderItem.getQuantity() + ")");
            }

            // Descontar stock del producto
            product.setStockQuantity(product.getStockQuantity() - orderItem.getQuantity());

            // Copiar order_item a sale_item
            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setStoreId(storeId);
            saleItem.setProduct(product);
            saleItem.setQuantity(orderItem.getQuantity());
            saleItem.setUnitPrice(orderItem.getUnitPrice());
            sale.getItems().add(saleItem);
        }

        order.setStatus(OrderStatus.DELIVERED);
        saleRepository.save(sale);
    }

    private Store requireStoreAccess(UUID storeId, UUID callerId, boolean superAdmin) {
        if (!superAdmin) {
            boolean allowed = userRepository.existsByIdAndActiveTrue(callerId)
                    && storeUserRepository.existsByStore_IdAndUser_IdAndActiveTrue(storeId, callerId);
            if (!allowed) {
                throw forbidden("No tienes acceso a esta sucursal");
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

    private User findActiveCaller(UUID callerId) {
        User user = userRepository.findById(callerId)
                .orElseThrow(() -> notFound("Usuario no encontrado"));
        if (!user.isActive()) {
            throw forbidden("Tu cuenta de usuario esta desactivada");
        }
        return user;
    }

    private CustomerOrder findOrderWithDetails(UUID storeId, UUID orderId) {
        return orderRepository.findByIdAndStoreIdWithDetails(orderId, storeId)
                .orElseThrow(() -> notFound("Pedido no encontrado en esta sucursal"));
    }

    private Product findActiveProduct(UUID storeId, UUID productId) {
        Product product = productRepository.findByIdAndStore_Id(productId, storeId)
                .orElseThrow(() -> notFound("Producto no encontrado en esta sucursal: " + productId));
        if (!product.isActive()) {
            throw conflict("El producto '" + product.getName() + "' esta inactivo");
        }
        return product;
    }

    private void requireEditableOrder(CustomerOrder order) {
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw conflict("No se puede modificar un pedido en estado " + order.getStatus());
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}