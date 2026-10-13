package com.minegocio.mi_negocio.repository;


import java.math.BigDecimal; 

public interface StoreSalesSummary {
 String getStoreName();
 Long getSalesCount();
 BigDecimal getTotal();
}