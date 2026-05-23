package com.syfe.finance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public enum CategoryType {
        INCOME, EXPENSE
    }

    public boolean isCustom() {
        return user != null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
