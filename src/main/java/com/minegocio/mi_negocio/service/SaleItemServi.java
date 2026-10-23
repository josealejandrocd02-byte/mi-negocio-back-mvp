package com.minegocio.mi_negocio.service;

import java.util.List;
import java.util.UUID;


import com.minegocio.mi_negocio.dtos.sale.SaleItemResponse;
import com.minegocio.mi_negocio.dtos.saleitem.SaleItemCreateRequest;
import com.minegocio.mi_negocio.dtos.saleitem.SaleItemUpdateRequest;


public interface SaleItemServi {
    SaleItemResponse addItemToSale(UUID saleId, SaleItemCreateRequest request, UUID requesterId, boolean superAdmin);
    SaleItemResponse updateSaleItem(UUID itemId, SaleItemUpdateRequest request, UUID requesterId, boolean superAdmin);
    void deleteSaleItem(UUID itemId, UUID requesterId, boolean superAdmin);
    List<SaleItemResponse> listItemsBySale(UUID saleId, UUID requesterId, boolean superAdmin);
    SaleItemResponse getItemById(UUID itemId, UUID requesterId, boolean superAdmin);
}