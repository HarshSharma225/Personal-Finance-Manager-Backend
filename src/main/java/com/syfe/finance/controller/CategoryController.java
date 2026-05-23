package com.syfe.finance.controller;

import com.syfe.finance.dto.request.CategoryRequest;
import com.syfe.finance.dto.response.CategoryResponse;
import com.syfe.finance.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, List<CategoryResponse>>> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.deleteCategory(name));
    }
}
