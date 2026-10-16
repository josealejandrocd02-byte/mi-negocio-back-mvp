package com.minegocio.mi_negocio.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sale_items")
public class SaleItem {
 @Id @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "sale_id", nullable = false)
 private Sale sale;

 @Column(name = "store_id", nullable = false)
 private UUID storeId;

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "product_id", nullable = false)
 private Product product;

 @Column(nullable = false)
 private int quantity;

 @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
 private BigDecimal unitPrice;

 public UUID getId() {
	return id;
 }

 public void setId(UUID id) {
	this.id = id;
 }

 public Sale getSale() {
	return sale;
 }

 public void setSale(Sale sale) {
	this.sale = sale;
 }

 public UUID getStoreId() {
	return storeId;
 }

 public void setStoreId(UUID storeId) {
	this.storeId = storeId;
 }

 public Product getProduct() {
	return product;
 }

 public void setProduct(Product product) {
	this.product = product;
 }

 public int getQuantity() {
	return quantity;
 }

 public void setQuantity(int quantity) {
	this.quantity = quantity;
 }

 public BigDecimal getUnitPrice() {
	return unitPrice;
 }

 public void setUnitPrice(BigDecimal unitPrice) {
	this.unitPrice = unitPrice;
 }
 
}