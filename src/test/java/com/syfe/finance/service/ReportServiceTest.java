package com.syfe.finance.service;

import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.Category.CategoryType;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.User;
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
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    private User user;
    private Transaction incomeTransaction;
    private Transaction expenseTransaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        Category incomeCategory = new Category();
        incomeCategory.setName("Salary");
        incomeCategory.setType(CategoryType.INCOME);

        Category expenseCategory = new Category();
        expenseCategory.setName("Food");
        expenseCategory.setType(CategoryType.EXPENSE);

        incomeTransaction = new Transaction();
        incomeTransaction.setAmount(new BigDecimal("3000"));
        incomeTransaction.setDate(LocalDate.of(2024, 1, 15));
        incomeTransaction.setCategory(incomeCategory);
        incomeTransaction.setUser(user);

        expenseTransaction = new Transaction();
        expenseTransaction.setAmount(new BigDecimal("500"));
        expenseTransaction.setDate(LocalDate.of(2024, 1, 20));
        expenseTransaction.setCategory(expenseCategory);
        expenseTransaction.setUser(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void getMonthlyReport_success() {
        when(transactionRepository.findByUserAndYearAndMonth(user, 2024, 1))
                .thenReturn(List.of(incomeTransaction, expenseTransaction));

        Map<String, Object> report = reportService.getMonthlyReport(2024, 1);

        assertEquals(1, report.get("month"));
        assertEquals(2024, report.get("year"));
        assertEquals(new BigDecimal("2500"), report.get("netSavings"));
    }

    @Test
    void getMonthlyReport_noTransactions_returnsZero() {
        when(transactionRepository.findByUserAndYearAndMonth(user, 2024, 6)).thenReturn(List.of());

        Map<String, Object> report = reportService.getMonthlyReport(2024, 6);

        assertEquals(BigDecimal.ZERO, report.get("netSavings"));
    }

    @Test
    void getYearlyReport_success() {
        when(transactionRepository.findByUserAndYear(user, 2024))
                .thenReturn(List.of(incomeTransaction, expenseTransaction));

        Map<String, Object> report = reportService.getYearlyReport(2024);

        assertEquals(2024, report.get("year"));
        assertEquals(new BigDecimal("2500"), report.get("netSavings"));
    }

    @Test
    void getYearlyReport_noTransactions_returnsZero() {
        when(transactionRepository.findByUserAndYear(user, 2025)).thenReturn(List.of());

        Map<String, Object> report = reportService.getYearlyReport(2025);
        assertEquals(BigDecimal.ZERO, report.get("netSavings"));
    }
}
