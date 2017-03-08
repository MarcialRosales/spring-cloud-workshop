package com.example;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Flight {

	@Id
	@GeneratedValue
	private Long id;
	
	@Column
	private String origin;
	@Column
	private String destination;
	
	
	Flight() {
		
	}
	Flight(String name) {
		origin = name.split("/")[0];
		destination = name.split("/")[1];
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
}
