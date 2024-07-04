package com.example.swoos.model;//package com.example.ProjectManagementTool.Model;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Data;




@Entity
@Data
@Table(name = "drop_down_model")
public class DropDownModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dropdown_id")
    private Long dropdownId;
    @Column(name = "description")
    private String description;



}
