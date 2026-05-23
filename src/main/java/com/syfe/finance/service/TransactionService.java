package com.syfe.finance.service;

import com.syfe.finance.dto.request.TransactionRequest;
import com.syfe.finance.dto.request.UpdateTransactionRequest;
import com.syfe.finance.dto.response.TransactionResponse;
import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.BadRequestException;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.repository.CategoryRepository;
import com.syfe.finance.repository.TransactionRepository;
import com.syfe.finance.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                               CategoryRepository categoryRepository,
                               UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = getCurrentUser();

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        Category category = categoryRepository.findByNameAndUser(request.getCategory(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategory()));

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setCategory(category);
        transaction.setDescription(request.getDescription());
        transaction.setUser(user);

        return toResponse(transactionRepository.save(transaction));
    }

    public Map<String, List<TransactionResponse>> getTransactions(LocalDate startDate, LocalDate endDate, Long categoryId) {
        User user = getCurrentUser();

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        List<Transaction> transactions = transactionRepository.findByUserWithFilters(user, startDate, endDate, category);
        List<TransactionResponse> responses = transactions.stream().map(this::toResponse).collect(Collectors.toList());
        return Map.of("transactions", responses);
    }

    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request) {
        User user = getCurrentUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findByNameAndUser(request.getCategory(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategory()));
            transaction.setCategory(category);
        }

        return toResponse(transactionRepository.save(transaction));
    }

    public Map<String, String> deleteTransaction(Long id) {
        User user = getCurrentUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        transactionRepository.delete(transaction);
        return Map.of("message", "Transaction deleted successfully");
    }

    public TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setDate(transaction.getDate());
        response.setCategory(transaction.getCategory().getName());
        response.setDescription(transaction.getDescription());
        response.setType(transaction.getCategory().getType());
        return response;
    }
}
