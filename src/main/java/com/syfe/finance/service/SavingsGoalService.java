package com.syfe.finance.service;

import com.syfe.finance.dto.request.GoalRequest;
import com.syfe.finance.dto.request.UpdateGoalRequest;
import com.syfe.finance.dto.response.GoalResponse;
import com.syfe.finance.entity.Category.CategoryType;
import com.syfe.finance.entity.SavingsGoal;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.BadRequestException;
import com.syfe.finance.exception.ForbiddenException;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.repository.SavingsGoalRepository;
import com.syfe.finance.repository.TransactionRepository;
import com.syfe.finance.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public SavingsGoalService(SavingsGoalRepository goalRepository, TransactionRepository transactionRepository,
                               UserRepository userRepository) {
        this.goalRepository = goalRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public GoalResponse createGoal(GoalRequest request) {
        User user = getCurrentUser();

        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future");
        }

        SavingsGoal goal = new SavingsGoal();
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        goal.setUser(user);

        return toResponse(goalRepository.save(goal), user);
    }

    public Map<String, List<GoalResponse>> getAllGoals() {
        User user = getCurrentUser();
        List<GoalResponse> responses = goalRepository.findByUser(user).stream()
                .map(goal -> toResponse(goal, user))
                .collect(Collectors.toList());
        return Map.of("goals", responses);
    }

    public GoalResponse getGoal(Long id) {
        User user = getCurrentUser();
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        return toResponse(goal, user);
    }

    public GoalResponse updateGoal(Long id, UpdateGoalRequest request) {
        User user = getCurrentUser();
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be in the future");
            }
            goal.setTargetDate(request.getTargetDate());
        }
        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        return toResponse(goalRepository.save(goal), user);
    }

    public Map<String, String> deleteGoal(Long id) {
        User user = getCurrentUser();
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        goalRepository.delete(goal);
        return Map.of("message", "Goal deleted successfully");
    }

    private GoalResponse toResponse(SavingsGoal goal, User user) {
        List<Transaction> transactions = transactionRepository.findByUserAndDateAfterOrEqual(user, goal.getStartDate());

        BigDecimal income = transactions.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenses = transactions.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal progress = income.subtract(expenses);
        if (progress.compareTo(BigDecimal.ZERO) < 0) {
            progress = BigDecimal.ZERO;
        }

        BigDecimal remaining = goal.getTargetAmount().subtract(progress);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        double percentage = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentage = progress.multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP)
                    .doubleValue();
            if (percentage > 100.0) percentage = 100.0;
        }

        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setGoalName(goal.getGoalName());
        response.setTargetAmount(goal.getTargetAmount());
        response.setTargetDate(goal.getTargetDate());
        response.setStartDate(goal.getStartDate());
        response.setCurrentProgress(progress);
        response.setProgressPercentage(percentage);
        response.setRemainingAmount(remaining);
        return response;
    }
}
