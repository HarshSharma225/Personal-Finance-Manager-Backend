package com.syfe.finance.repository;

import com.syfe.finance.entity.Category;
import com.syfe.finance.entity.Transaction;
import com.syfe.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:category IS NULL OR t.category = :category) " +
           "ORDER BY t.date DESC")
    List<Transaction> findByUserWithFilters(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") Category category
    );

    List<Transaction> findByUserOrderByDateDesc(User user);

    boolean existsByCategory(Category category);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date >= :startDate")
    List<Transaction> findByUserAndDateAfterOrEqual(@Param("user") User user, @Param("startDate") LocalDate startDate);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND YEAR(t.date) = :year AND MONTH(t.date) = :month")
    List<Transaction> findByUserAndYearAndMonth(@Param("user") User user, @Param("year") int year, @Param("month") int month);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND YEAR(t.date) = :year")
    List<Transaction> findByUserAndYear(@Param("user") User user, @Param("year") int year);
}
