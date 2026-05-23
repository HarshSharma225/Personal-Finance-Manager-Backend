package com.syfe.finance.service;

import com.syfe.finance.dto.request.CategoryRequest;
import com.syfe.finance.dto.response.CategoryResponse;
import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.BadRequestException;
import com.syfe.finance.exception.DuplicateResourceException;
import com.syfe.finance.exception.ForbiddenException;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.repository.CategoryRepository;
import com.syfe.finance.repository.TransactionRepository;
import com.syfe.finance.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, TransactionRepository transactionRepository,
                           UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Map<String, List<CategoryResponse>> getAllCategories() {
        User user = getCurrentUser();
        List<Category> categories = categoryRepository.findAllAvailableForUser(user);
        List<CategoryResponse> responses = categories.stream().map(this::toResponse).collect(Collectors.toList());
        return Map.of("categories", responses);
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        User user = getCurrentUser();

        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new DuplicateResourceException("Category with this name already exists");
        }
        if (categoryRepository.findByNameAndUserIsNull(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Category with this name already exists as a default category");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setUser(user);

        return toResponse(categoryRepository.save(category));
    }

    public Map<String, String> deleteCategory(String name) {
        User user = getCurrentUser();

        Category category = categoryRepository.findByNameAndUserIsNull(name).orElse(null);
        if (category != null) {
            throw new ForbiddenException("Cannot delete default categories");
        }

        Category userCategory = categoryRepository.findAllAvailableForUser(user).stream()
                .filter(c -> c.getName().equals(name) && c.isCustom())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!userCategory.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You cannot delete this category");
        }

        if (transactionRepository.existsByCategory(userCategory)) {
            throw new BadRequestException("Cannot delete category that is used by transactions");
        }

        categoryRepository.delete(userCategory);
        return Map.of("message", "Category deleted successfully");
    }

    private CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setName(category.getName());
        response.setType(category.getType());
        response.setIsCustom(category.isCustom());
        return response;
    }
}
