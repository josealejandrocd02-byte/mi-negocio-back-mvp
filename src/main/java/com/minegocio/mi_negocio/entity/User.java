package com.minegocio.mi_negocio.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

import com.minegocio.mi_negocio.emus.PlatformRole;

@Entity
@Table(name = "users")
public class User {
 @Id @GeneratedValue(strategy = GenerationType.UUID)
 private UUID id;

 @Column(name = "full_name", nullable = false, length = 150)
 private String fullName;

 @Column(nullable = false, unique = true)
 private String email;

 @Column(name = "password_hash", nullable = false, columnDefinition = "text")
 private String passwordHash;

 @Enumerated(EnumType.STRING)
 @Column(name = "platform_role", nullable = false, length = 20)
 private PlatformRole platformRole = PlatformRole.USER;

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

 public String getFullName() {
	return fullName;
 }

 public void setFullName(String fullName) {
	this.fullName = fullName;
 }

 public String getEmail() {
	return email;
 }

 public void setEmail(String email) {
	this.email = email;
 }

 public String getPasswordHash() {
	return passwordHash;
 }

 public void setPasswordHash(String passwordHash) {
	this.passwordHash = passwordHash;
 }

 public PlatformRole getPlatformRole() {
	return platformRole;
 }

 public void setPlatformRole(PlatformRole platformRole) {
	this.platformRole = platformRole;
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