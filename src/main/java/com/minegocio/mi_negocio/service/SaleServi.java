package com.minegocio.mi_negocio.service;

import java.util.List;
import java.util.UUID;

import com.minegocio.mi_negocio.dtos.sale.SaleCreateRequest;
import com.minegocio.mi_negocio.dtos.sale.SaleResponse;
import com.minegocio.mi_negocio.dtos.sale.SaleStatusRequest;
import com.minegocio.mi_negocio.dtos.sale.SaleUpdateRequest;

public interface SaleServi {

    // requesterId = usuario del token; superAdmin omite la validacion por sucursal
    SaleResponse createSale(SaleCreateRequest request, UUID requesterId, boolean superAdmin);

    SaleResponse getSaleById(UUID id, UUID requesterId, boolean superAdmin);

    SaleResponse updateSale(UUID id, SaleUpdateRequest request, UUID requesterId, boolean superAdmin);

    SaleResponse changeStatus(UUID id, SaleStatusRequest request, UUID requesterId, boolean superAdmin);

    // Cancelacion logica: la venta se conserva con estado CANCELLED
    SaleResponse cancelSale(UUID id, UUID requesterId, boolean superAdmin);

    void deleteSale(UUID id, UUID requesterId, boolean superAdmin);

    List<SaleResponse> listSalesByStore(UUID storeId, UUID requesterId, boolean superAdmin);
}
