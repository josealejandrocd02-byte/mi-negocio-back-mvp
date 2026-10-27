package com.minegocio.mi_negocio.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "sales")
public class Sale {
 @Id @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "store_id", nullable = false)
 private Store store;

 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "order_id")
 private CustomerOrder order;          // null = venta directa en mostrador

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "sold_by", nullable = false)
 private User soldBy;

 @Column(nullable = false, precision = 12, scale = 2)
 private BigDecimal total;

 @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
 private List<SaleItem> items = new ArrayList<>();

 @CreationTimestamp
 @Column(name = "created_at", nullable = false, updatable = false)
 private Instant createdAt;
 
 

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

 public CustomerOrder getOrder() {
	return order;
 }

 public void setOrder(CustomerOrder order) {
	this.order = order;
 }

 public User getSoldBy() {
	return soldBy;
 }

 public void setSoldBy(User soldBy) {
	this.soldBy = soldBy;
 }

 public BigDecimal getTotal() {
	return total;
 }

 public void setTotal(BigDecimal total) {
	this.total = total;
 }

 public List<SaleItem> getItems() {
	return items;
 }

 public void setItems(List<SaleItem> items) {
	this.items = items;
 }

 public Instant getCreatedAt() {
	return createdAt;
 }

 public void setCreatedAt(Instant createdAt) {
	this.createdAt = createdAt;
 }
 
 
 
}