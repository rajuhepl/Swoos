package com.example.swoos.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="location_with_status_code")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationWithStatusCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Long locationId;
    @Column(name = "ahmedabad")
    private String ahmedabad;
    @Column(name = "bangalore")
    private String bangalore;
    @Column(name = "chennai")
    private String chennai;
    @Column(name = "delhi")
    private String delhi;
    @Column(name = "hyderabad")
    private String hyderabad;
    @Column(name = "indore")
    private String indore;
    @Column(name = "mumbai")
    private String mumbai;
    @Column(name = "nagpur")
    private String nagpur;
    @Column(name = "patna")
    private String patna;
    @Column(name = "pune")
    private String pune;
    @Column(name = "others")
    private String otherCities;
}
