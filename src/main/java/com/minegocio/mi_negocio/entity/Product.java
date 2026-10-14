package com.minegocio.mi_negocio.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "products")
public class Product {
 @Id @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "store_id", nullable = false)
 private Store store;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "category_id")
 private Category category;

 @Column(nullable = false, length = 150)
 private String name;

 @Column(nullable = false, precision = 12, scale = 2)
 private BigDecimal price;

 @Column(name = "stock_quantity", nullable = false)
 private int stockQuantity;

 @Column(name = "is_active", nullable = false)
 private boolean active = true;

 @CreationTimestamp
 @Column(name = "created_at", nullable = false, updatable = false)
 private Instant createdAt;
 
 @Column(name = "image_url")
 private String imageUrl;

 @Column(name = "image_public_id")
 private String imagePublicId;
 
 
 

 public String getImageUrl() {
	return imageUrl;
}

 public void setImageUrl(String imageUrl) {
	this.imageUrl = imageUrl;
 }

 public String getImagePublicId() {
	return imagePublicId;
 }

 public void setImagePublicId(String imagePublicId) {
	this.imagePublicId = imagePublicId;
 }

 public UUID getId() {
	return id;
 }

 public void setId(UUID id) {
	this.id = id;
 }

 public Store getStore() {
	return store;
 }

 public void setStore(Store store) {
	this.store = store;
 }

 public Category getCategory() {
	return category;
 }

 public void setCategory(Category category) {
	this.category = category;
 }

 public String getName() {
	return name;
 }

 public void setName(String name) {
	this.name = name;
 }

 public BigDecimal getPrice() {
	return price;
 }

 public void setPrice(BigDecimal price) {
	this.price = price;
 }

 public int getStockQuantity() {
	return stockQuantity;
 }

 public void setStockQuantity(int stockQuantity) {
	this.stockQuantity = stockQuantity;
 }

 public boolean isActive() {
	return active;
 }

 public void setActive(boolean active) {
	this.active = active;
 }

 public Instant getCreatedAt() {
	return createdAt;
 }

 public void setCreatedAt(Instant createdAt) {
	this.createdAt = createdAt;
 }
 
 
}