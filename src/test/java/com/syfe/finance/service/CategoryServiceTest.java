package com.syfe.finance.service;

import com.syfe.finance.dto.request.CategoryRequest;
import com.syfe.finance.dto.response.CategoryResponse;
import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.Category.CategoryType;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.BadRequestException;
import com.syfe.finance.exception.DuplicateResourceException;
import com.syfe.finance.exception.ForbiddenException;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.repository.CategoryRepository;
import com.syfe.finance.repository.TransactionRepository;
import com.syfe.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User user;
    private Category defaultCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        defaultCategory = new Category();
        defaultCategory.setId(1L);
        defaultCategory.setName("Salary");
        defaultCategory.setType(CategoryType.INCOME);
        defaultCategory.setUser(null);

        customCategory = new Category();
        customCategory.setId(2L);
        customCategory.setName("Freelance");
        customCategory.setType(CategoryType.INCOME);
        customCategory.setUser(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void getAllCategories_returnsList() {
        when(categoryRepository.findAllAvailableForUser(user)).thenReturn(List.of(defaultCategory, customCategory));

        Map<String, List<CategoryResponse>> result = categoryService.getAllCategories();

        assertEquals(2, result.get("categories").size());
    }

    @Test
    void createCategory_success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Freelance");
        request.setType(CategoryType.INCOME);

        when(categoryRepository.existsByNameAndUser("Freelance", user)).thenReturn(false);
        when(categoryRepository.findByNameAndUserIsNull("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertEquals("Freelance", response.getName());
        assertTrue(response.getIsCustom());
    }

    @Test
    void createCategory_duplicateForUser_throwsException() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Freelance");
        request.setType(CategoryType.INCOME);

        when(categoryRepository.existsByNameAndUser("Freelance", user)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void createCategory_sameAsDefault_throwsException() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Salary");
        request.setType(CategoryType.INCOME);

        when(categoryRepository.existsByNameAndUser("Salary", user)).thenReturn(false);
        when(categoryRepository.findByNameAndUserIsNull("Salary")).thenReturn(Optional.of(defaultCategory));

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void deleteCategory_defaultCategory_throwsForbidden() {
        when(categoryRepository.findByNameAndUserIsNull("Salary")).thenReturn(Optional.of(defaultCategory));

        assertThrows(ForbiddenException.class, () -> categoryService.deleteCategory("Salary"));
    }

    @Test
    void deleteCategory_notFound_throwsException() {
        when(categoryRepository.findByNameAndUserIsNull("Unknown")).thenReturn(Optional.empty());
        when(categoryRepository.findAllAvailableForUser(user)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategory("Unknown"));
    }

    @Test
    void deleteCategory_usedByTransaction_throwsBadRequest() {
        when(categoryRepository.findByNameAndUserIsNull("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findAllAvailableForUser(user)).thenReturn(List.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory("Freelance"));
    }

    @Test
    void deleteCategory_success() {
        when(categoryRepository.findByNameAndUserIsNull("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findAllAvailableForUser(user)).thenReturn(List.of(customCategory));
        when(transactionRepository.existsByCategory(customCategory)).thenReturn(false);

        Map<String, String> result = categoryService.deleteCategory("Freelance");
        assertEquals("Category deleted successfully", result.get("message"));
        verify(categoryRepository).delete(customCategory);
    }
}
