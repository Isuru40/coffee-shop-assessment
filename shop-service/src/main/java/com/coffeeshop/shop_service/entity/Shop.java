package com.coffeeshop.shop_service.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;

@Entity
@Table(name = "shops", schema = "shop_schema")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String menu; // JSONB for menu items and prices
    private Integer queueCount;
    private Integer maxQueueSize;
    @Column(columnDefinition = "time without time zone")
    private java.sql.Time openingTime;
    @Column(columnDefinition = "time without time zone")
    private java.sql.Time closingTime;
    @Version
    private Long version;
    @Column(precision = 9, scale = 6) // Use DECIMAL(9,6) for latitude
    private BigDecimal latitude; // Latitude field
    @Column(precision = 9, scale = 6) // Use DECIMAL(9,6) for longitude
    private BigDecimal longitude; // Longitude field

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMenu() { return menu; }
    public void setMenu(String menu) { this.menu = menu; }
    public Integer getQueueCount() { return queueCount; }
    public void setQueueCount(Integer queueCount) { this.queueCount = queueCount; }
    public Integer getMaxQueueSize() { return maxQueueSize; }
    public void setMaxQueueSize(Integer maxQueueSize) { this.maxQueueSize = maxQueueSize; }
    public java.sql.Time getOpeningTime() { return openingTime; }
    public void setOpeningTime(java.sql.Time openingTime) { this.openingTime = openingTime; }
    public java.sql.Time getClosingTime() { return closingTime; }
    public void setClosingTime(java.sql.Time closingTime) { this.closingTime = closingTime; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}