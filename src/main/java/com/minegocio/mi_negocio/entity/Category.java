package com.minegocio.mi_negocio.entity;


import jakarta.persistence.*;
import java.util.UUID;

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