package com.coffeeshop.shop_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Time;

@Entity
@Table(name = "shops", schema = "shop_schema")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToOne(mappedBy = "shop", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Menu menu;

    private Integer queueCount;

    private Integer maxQueueSize;

    @Column(columnDefinition = "time without time zone")
    private Time openingTime;

    @Column(columnDefinition = "time without time zone")
    private Time closingTime;

    @Version
    private Long version;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Menu getMenu() { return menu; }
    public void setMenu(Menu menu) { this.menu = menu; }
    public Integer getQueueCount() { return queueCount; }
    public void setQueueCount(Integer queueCount) { this.queueCount = queueCount; }
    public Integer getMaxQueueSize() { return maxQueueSize; }
    public void setMaxQueueSize(Integer maxQueueSize) { this.maxQueueSize = maxQueueSize; }
    public Time getOpeningTime() { return openingTime; }
    public void setOpeningTime(Time openingTime) { this.openingTime = openingTime; }
    public Time getClosingTime() { return closingTime; }
    public void setClosingTime(Time closingTime) { this.closingTime = closingTime; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}