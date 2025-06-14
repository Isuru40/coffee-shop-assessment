package com.coffeeshop.auth_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String userType; // customer, shop_owner, shop_operator
    private String mobile;
    private String name;
    private String address; // For customers and shop operators
    private String shopRegistrationNumber; // For shop owners
    private String location; // For shop owners
    private String contactDetails; // For shop owners

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getShopRegistrationNumber() { return shopRegistrationNumber; }
    public void setShopRegistrationNumber(String shopRegistrationNumber) { this.shopRegistrationNumber = shopRegistrationNumber; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getContactDetails() { return contactDetails; }
    public void setContactDetails(String contactDetails) { this.contactDetails = contactDetails; }
}