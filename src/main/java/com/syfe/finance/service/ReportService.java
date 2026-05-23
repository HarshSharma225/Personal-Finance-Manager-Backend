package com.syfe.finance.service;

import com.syfe.finance.entity.Category.CategoryType;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.repository.TransactionRepository;
import com.syfe.finance.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public ReportService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public Map<String, Object> getMonthlyReport(int year, int month) {
        User user = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUserAndYearAndMonth(user, year, month);

        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();

        for (Transaction t : transactions) {
            String catName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == CategoryType.INCOME) {
                incomeByCategory.merge(catName, amount, BigDecimal::add);
            } else {
                expensesByCategory.merge(catName, amount, BigDecimal::add);
            }
        }

        BigDecimal totalIncome = incomeByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expensesByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        Map<String, Object> report = new HashMap<>();
        report.put("month", month);
        report.put("year", year);
        report.put("totalIncome", incomeByCategory);
        report.put("totalExpenses", expensesByCategory);
        report.put("netSavings", netSavings);
        return report;
    }

    public Map<String, Object> getYearlyReport(int year) {
        User user = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUserAndYear(user, year);

        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expensesByCategory = new HashMap<>();

        for (Transaction t : transactions) {
            String catName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == CategoryType.INCOME) {
                incomeByCategory.merge(catName, amount, BigDecimal::add);
            } else {
                expensesByCategory.merge(catName, amount, BigDecimal::add);
            }
        }

        BigDecimal totalIncome = incomeByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expensesByCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        Map<String, Object> report = new HashMap<>();
        report.put("year", year);
        report.put("totalIncome", incomeByCategory);
        report.put("totalExpenses", expensesByCategory);
        report.put("netSavings", netSavings);
        return report;
    }
}
