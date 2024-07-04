package com.example.swoos.model;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.List;
@Data
@Entity
@Table(name = "PlatformValueLoss")
public class PlatformAndValueloss {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany
    @JoinColumn(name = "national_id")
    private List<Platform> national;

    @OneToMany
    @JoinColumn(name = "beauty_id")
    private List<Platform> beauty;

    @OneToMany
    @JoinColumn(name = "grocery_id")
    private List<Platform> grocery;

    @OneToMany
    @JoinColumn(name = "quickcom_id")
    private List<Platform> quickCom;

    @Column(name = "total_flipkart_amazon")
    private double totalFlipkartAmazon;

    @Column(name = "flipkart_percentage")
    private double flipkartPercentage;

    @Column(name = "amazon_percentage")
    private double amazonPercentage;

    @Column(name = "myntra_percentage")
    private double myntraPercentage;

    @Column(name = "nykaa_percentage")
    private double nykaaPercentage;

    @Column(name = "purplle_percentage")
    private double purpllePercentage;

    @Column(name = "fk_grocery_percentage")
    private double FKGroceryPercentage;

    @Column(name = "big_basket_percentage")
    private double bigBasketPercentage;

    @Column(name = "swiggy_percentage")
    private double swiggyPercentage;

    @Column(name = "blinkit_percentage")
    private double blinkitPercentage;

    @Column(name = "zepto_percentage")
    private double zeptoPercentage;

    @Column(name = "count_amazon")
    private int countAmazon;

    @Column(name = "count_flipkart")
    private int countFlipkart;

    @Column(name = "count_zepto")
    private int countZepto;

    @Column(name = "is_submitted")
    private Boolean isSubmited;

    @Column(name = "deleted_flag")
    private boolean deletedFlag;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}

