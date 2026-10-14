package com.minegocio.mi_negocio.entity;

//entity/CustomerOrder.java   (tabla "orders"; se evita el nombre Order por ser palabra reservada)
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

import com.minegocio.mi_negocio.emus.OrderStatus;

@Entity
@Table(name = "orders")
public class CustomerOrder {
 @Id @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "store_id", nullable = false)
 private Store store;

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "created_by", nullable = false)
 private User createdBy;

 @Column(name = "customer_name", length = 150)
 private String customerName;

 @Column(name = "customer_phone", length = 30)
 private String customerPhone;

 @Column(name = "delivery_address", columnDefinition = "text")
 private String deliveryAddress;

 @Column(columnDefinition = "text")
 private String notes;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 20)
 private OrderStatus status = OrderStatus.PENDING;

 @Column(nullable = false, precision = 12, scale = 2)
 private BigDecimal total = BigDecimal.ZERO;

 @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
 private List<OrderItem> items = new ArrayList<>();

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

 public User getCreatedBy() {
	return createdBy;
 }

 public void setCreatedBy(User createdBy) {
	this.createdBy = createdBy;
 }

 public String getCustomerName() {
	return customerName;
 }

 public void setCustomerName(String customerName) {
	this.customerName = customerName;
 }

 public String getCustomerPhone() {
	return customerPhone;
 }

 public void setCustomerPhone(String customerPhone) {
	this.customerPhone = customerPhone;
 }

 public String getDeliveryAddress() {
	return deliveryAddress;
 }

 public void setDeliveryAddress(String deliveryAddress) {
	this.deliveryAddress = deliveryAddress;
 }

 public String getNotes() {
	return notes;
 }

 public void setNotes(String notes) {
	this.notes = notes;
 }

 public OrderStatus getStatus() {
	return status;
 }

 public void setStatus(OrderStatus status) {
	this.status = status;
 }

 public BigDecimal getTotal() {
	return total;
 }

 public void setTotal(BigDecimal total) {
	this.total = total;
 }

 public List<OrderItem> getItems() {
	return items;
 }

 public void setItems(List<OrderItem> items) {
	this.items = items;
 }

 public Instant getCreatedAt() {
	return createdAt;
 }

 public void setCreatedAt(Instant createdAt) {
	this.createdAt = createdAt;
 }
 
 
}