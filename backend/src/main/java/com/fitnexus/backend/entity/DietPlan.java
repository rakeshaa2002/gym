package com.fitnexus.backend.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diet_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DietPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 10)
    private String eatTime;

    private Integer prepTime;
    private Integer cookTime;

    @Column(length = 30)
    private String difficulty;

    private Integer totalSteps;
    private Integer healthScore;
    private Integer calories;
    private Integer protein;
    private Integer carbs;
    private Integer fats;
    private Integer cholesterol;
    private Integer sodium;
    private Integer potassium;
    private Integer vitaminA;
    private Integer vitaminC;
    private Integer calcium;
    private Integer iron;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "diet_plan_ingredients", joinColumns = @JoinColumn(name = "diet_plan_id"))
    @Column(name = "ingredient", columnDefinition = "TEXT")
    private List<String> ingredients = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "diet_plan_directions", joinColumns = @JoinColumn(name = "diet_plan_id"))
    @Column(name = "direction", columnDefinition = "TEXT")
    private List<String> directions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "diet_plan_tools", joinColumns = @JoinColumn(name = "diet_plan_id"))
    @Column(name = "tool", columnDefinition = "TEXT")
    private List<String> tools = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String mainImage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "diet_plan_gallery_images", joinColumns = @JoinColumn(name = "diet_plan_id"))
    @Column(name = "image_path", columnDefinition = "TEXT")
    private List<String> galleryImages = new ArrayList<>();

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
