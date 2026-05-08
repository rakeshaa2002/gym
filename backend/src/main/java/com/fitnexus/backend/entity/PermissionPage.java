package com.fitnexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permission_pages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_key", nullable = false, unique = true, length = 100)
    private String pageKey;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(name = "route_path", nullable = false, length = 150)
    private String routePath;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
