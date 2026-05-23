package com.syfe.finance.service;

import com.syfe.finance.dto.request.GoalRequest;
import com.syfe.finance.dto.request.UpdateGoalRequest;
import com.syfe.finance.dto.response.GoalResponse;
import com.syfe.finance.entity.SavingsGoal;
import com.syfe.finance.entity.User;
import com.syfe.finance.exception.BadRequestException;
import com.syfe.finance.exception.ForbiddenException;
import com.syfe.finance.exception.ResourceNotFoundException;
import com.syfe.finance.repository.SavingsGoalRepository;
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
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository goalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SavingsGoalService goalService;

    private User user;
    private SavingsGoal goal;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test@example.com");

        goal = new SavingsGoal();
        goal.setId(1L);
        goal.setGoalName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("5000"));
        goal.setTargetDate(LocalDate.now().plusMonths(6));
        goal.setStartDate(LocalDate.now());
        goal.setUser(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void createGoal_success() {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("5000"));
        request.setTargetDate(LocalDate.now().plusMonths(6));

        when(goalRepository.save(any(SavingsGoal.class))).thenReturn(goal);
        when(transactionRepository.findByUserAndDateAfterOrEqual(any(), any())).thenReturn(List.of());

        GoalResponse response = goalService.createGoal(request);
        assertEquals("Emergency Fund", response.getGoalName());
    }

    @Test
    void createGoal_pastTargetDate_throwsException() {
        GoalRequest request = new GoalRequest();
        request.setGoalName("Test");
        request.setTargetAmount(new BigDecimal("1000"));
        request.setTargetDate(LocalDate.now().minusDays(1));

        assertThrows(BadRequestException.class, () -> goalService.createGoal(request));
    }

    @Test
    void getAllGoals_returnsList() {
        when(goalRepository.findByUser(user)).thenReturn(List.of(goal));
        when(transactionRepository.findByUserAndDateAfterOrEqual(any(), any())).thenReturn(List.of());

        Map<String, List<GoalResponse>> result = goalService.getAllGoals();
        assertEquals(1, result.get("goals").size());
    }

    @Test
    void getGoal_success() {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(transactionRepository.findByUserAndDateAfterOrEqual(any(), any())).thenReturn(List.of());

        GoalResponse response = goalService.getGoal(1L);
        assertEquals("Emergency Fund", response.getGoalName());
    }

    @Test
    void getGoal_notFound_throwsException() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> goalService.getGoal(99L));
    }

    @Test
    void getGoal_differentUser_throwsForbidden() {
        User otherUser = new User();
        otherUser.setId(2L);
        goal.setUser(otherUser);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        assertThrows(ForbiddenException.class, () -> goalService.getGoal(1L));
    }

    @Test
    void updateGoal_success() {
        UpdateGoalRequest request = new UpdateGoalRequest();
        request.setTargetAmount(new BigDecimal("6000"));
        request.setTargetDate(LocalDate.now().plusMonths(8));

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(goal)).thenReturn(goal);
        when(transactionRepository.findByUserAndDateAfterOrEqual(any(), any())).thenReturn(List.of());

        GoalResponse response = goalService.updateGoal(1L, request);
        assertNotNull(response);
    }

    @Test
    void deleteGoal_success() {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        Map<String, String> result = goalService.deleteGoal(1L);
        assertEquals("Goal deleted successfully", result.get("message"));
        verify(goalRepository).delete(goal);
    }

    @Test
    void deleteGoal_differentUser_throwsForbidden() {
        User otherUser = new User();
        otherUser.setId(2L);
        goal.setUser(otherUser);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        assertThrows(ForbiddenException.class, () -> goalService.deleteGoal(1L));
    }
}
