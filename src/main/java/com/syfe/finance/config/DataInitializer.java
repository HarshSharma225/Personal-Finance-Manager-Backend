package com.syfe.finance.config;

import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.Category.CategoryType;
import com.syfe.finance.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        createDefault("Salary", CategoryType.INCOME);
        createDefault("Food", CategoryType.EXPENSE);
        createDefault("Rent", CategoryType.EXPENSE);
        createDefault("Transportation", CategoryType.EXPENSE);
        createDefault("Entertainment", CategoryType.EXPENSE);
        createDefault("Healthcare", CategoryType.EXPENSE);
        createDefault("Utilities", CategoryType.EXPENSE);
    }

    private void createDefault(String name, CategoryType type) {
        if (categoryRepository.findByNameAndUserIsNull(name).isEmpty()) {
            Category category = new Category();
            category.setName(name);
            category.setType(type);
            category.setUser(null);
            categoryRepository.save(category);
        }
    }
}
