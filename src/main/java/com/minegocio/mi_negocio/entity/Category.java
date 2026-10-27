package com.minegocio.mi_negocio.entity;


import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "categories")
public class Category {
 @Id @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "store_id", nullable = false)
 private Store store;

 @Column(nullable = false, length = 100)
 private String name;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP")
    private Instant createdAt;



 public Instant getCreatedAt() {
	return createdAt;
 }

 public void setCreatedAt(Instant createdAt) {
	this.createdAt = createdAt;
 }

 public boolean isActive() {
	return active;
 }

 public void setActive(boolean active) {
	this.active = active;
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

 public String getName() {
	return name;
 }

 public void setName(String name) {
	this.name = name;
 }
 
 
 
}