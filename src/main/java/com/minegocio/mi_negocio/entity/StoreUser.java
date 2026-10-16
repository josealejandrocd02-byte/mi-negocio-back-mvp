package com.minegocio.mi_negocio.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

import com.minegocio.mi_negocio.emus.StoreRole;

@Entity
@Table(name = "store_users")
public class StoreUser {
 @Id @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "store_id", nullable = false)
 private Store store;

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "user_id", nullable = false)
 private User user;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 20)
 private StoreRole role;

 @Column(name = "is_active", nullable = false)
 private boolean active = true;

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

 public User getUser() {
	return user;
 }

 public void setUser(User user) {
	this.user = user;
 }

 public StoreRole getRole() {
	return role;
 }

 public void setRole(StoreRole role) {
	this.role = role;
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