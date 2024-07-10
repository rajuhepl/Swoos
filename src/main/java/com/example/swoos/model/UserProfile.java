package com.example.swoos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_profile")
@Data
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean sNo;
    private boolean date;
    private boolean channel;
    private boolean pname;
    private boolean asin;
    private boolean revenue;
    private boolean daySales;
    private boolean division;
    private boolean brand;
    private boolean category;
    private boolean subCategory;
    private boolean indore;
    private boolean delhi;
    private boolean mumbai;
    private boolean nagpur;
    private boolean patna;
    private boolean pune;
    private boolean ahmedabad;
    private boolean bangalore;
    private boolean chennai;
    private boolean hyderabad;
    private boolean calcutta;
    private boolean reason;
    private boolean remarks;
    private boolean swooscontribution;
    private boolean swoosPercentage;
    private boolean other;
    private boolean valueLoss;
    private boolean download;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
