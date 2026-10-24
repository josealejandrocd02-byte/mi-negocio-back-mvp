package com.minegocio.mi_negocio.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

import com.minegocio.mi_negocio.emus.StoreStatus;

@Entity
@Table(name = "stores")
public class Store {
 @Id @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "owner_user_id", nullable = false)
 private User ownerUser;

 @Column(nullable = false, length = 150)
 private String name;

 @Column(columnDefinition = "text")
 private String address;

 @Column(length = 30)
 private String phone;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false, length = 20)
 private StoreStatus status = StoreStatus.ACTIVE;
 
 @Column(name = "logo_url")
 private String logoUrl;

 @Column(name = "logo_public_id")
 private String logoPublicId;

 public String getLogoUrl() {
	return logoUrl;
}

 public void setLogoUrl(String logoUrl) {
	this.logoUrl = logoUrl;
 }

 public String getLogoPublicId() {
	return logoPublicId;
 }

 public void setLogoPublicId(String logoPublicId) {
	this.logoPublicId = logoPublicId;
 }

 @CreationTimestamp
 @Column(name = "created_at", nullable = false, updatable = false)
 private Instant createdAt;

 public UUID getId() {
	return id;
 }

 public void setId(UUID id) {
	this.id = id;
 }

 public User getOwnerUser() {
	return ownerUser;
 }

 public void setOwnerUser(User ownerUser) {
	this.ownerUser = ownerUser;
 }

 public String getName() {
	return name;
 }

 public void setName(String name) {
	this.name = name;
 }

 public String getAddress() {
	return address;
 }

 public void setAddress(String address) {
	this.address = address;
 }

 public String getPhone() {
	return phone;
 }

 public void setPhone(String phone) {
	this.phone = phone;
 }

 public StoreStatus getStatus() {
	return status;
 }

 public void setStatus(StoreStatus status) {
	this.status = status;
 }

 public Instant getCreatedAt() {
	return createdAt;
 }

 public void setCreatedAt(Instant createdAt) {
	this.createdAt = createdAt;
 }
 
}