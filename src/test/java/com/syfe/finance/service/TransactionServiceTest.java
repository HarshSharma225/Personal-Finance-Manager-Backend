package com.syfe.finance.service;

import com.syfe.finance.dto.request.TransactionRequest;
import com.syfe.finance.dto.request.UpdateTransactionRequest;
import com.syfe.finance.dto.response.TransactionResponse;
import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.Category.CategoryType;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.BadRequestException;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.repository.CategoryRepository;
import com.syfe.finance.repository.TransactionRepository;
import com.syfe.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category category;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        category = new Category();
        category.setId(1L);
        category.setName("Salary");
        category.setType(CategoryType.INCOME);

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("5000"));
        transaction.setDate(LocalDate.now().minusDays(1));
        transaction.setCategory(category);
        transaction.setUser(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void createTransaction_success() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("5000"));
        request.setDate(LocalDate.now().minusDays(1));
        request.setCategory("Salary");

        when(categoryRepository.findByNameAndUser("Salary", user)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(request);
        assertEquals(new BigDecimal("5000"), response.getAmount());
    }

    @Test
    void createTransaction_futureDate_throwsException() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("5000"));
        request.setDate(LocalDate.now().plusDays(1));
        request.setCategory("Salary");

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void createTransaction_categoryNotFound_throwsException() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("5000"));
        request.setDate(LocalDate.now());
        request.setCategory("Unknown");

        when(categoryRepository.findByNameAndUser("Unknown", user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void getTransactions_noFilters_returnsAll() {
        when(transactionRepository.findByUserWithFilters(user, null, null, null))
                .thenReturn(List.of(transaction));

        Map<String, List<TransactionResponse>> result = transactionService.getTransactions(null, null, null);
        assertEquals(1, result.get("transactions").size());
    }

    @Test
    void updateTransaction_success() {
        UpdateTransactionRequest request = new UpdateTransactionRequest();
        request.setAmount(new BigDecimal("6000"));

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        transaction.setAmount(new BigDecimal("6000"));
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        TransactionResponse response = transactionService.updateTransaction(1L, request);
        assertEquals(new BigDecimal("6000"), response.getAmount());
    }

    @Test
    void updateTransaction_notFound_throwsException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.updateTransaction(99L, new UpdateTransactionRequest()));
    }

    @Test
    void deleteTransaction_success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Map<String, String> result = transactionService.deleteTransaction(1L);
        assertEquals("Transaction deleted successfully", result.get("message"));
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_notFound_throwsException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(99L));
    }
}
