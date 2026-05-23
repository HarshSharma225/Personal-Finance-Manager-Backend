package com.syfe.finance.controller;

import com.syfe.finance.dto.request.GoalRequest;
import com.syfe.finance.dto.request.UpdateGoalRequest;
import com.syfe.finance.dto.response.GoalResponse;
import com.syfe.finance.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalService goalService;

    public SavingsGoalController(SavingsGoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody GoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.createGoal(request));
    }

    @GetMapping
    public ResponseEntity<Map<String, List<GoalResponse>>> getAll() {
        return ResponseEntity.ok(goalService.getAllGoals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoal(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody UpdateGoalRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.deleteGoal(id));
    }
}
