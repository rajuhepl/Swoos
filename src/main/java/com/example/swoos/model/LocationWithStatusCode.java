package com.example.swoos.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="location_with_status_code")
public class LocationWithStatusCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;
    @Column(name = "ahmedabad")
    private String Ahmedabad;
    @Column(name = "bangalore")
    private String Bangalore;
    @Column(name = "chennai")
    private String Chennai;
    @Column(name = "delhi")
    private String Delhi;
    @Column(name = "hyderabad")
    private String Hyderabad;
    @Column(name = "indore")
    private String Indore;
    @Column(name = "kolkata")
    private String Kolkata;
    @Column(name = "mumbai")
    private String Mumbai;
    @Column(name = "nagpur")
    private String Nagpur;
    @Column(name = "patna")
    private String Patna;
    @Column(name = "pune")
    private String Pune;
}
