package com.pharmacy.smart_pharmacy.repository;

import com.pharmacy.smart_pharmacy.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findByBarcode(String barcode);

    List<Medicine> findByActiveTrue();

    @Query("SELECT m FROM Medicine m WHERE m.quantity <= m.reorderLevel AND m.active = true")
    List<Medicine> findLowStockMedicines();

    @Query("SELECT m FROM Medicine m WHERE m.expiryDate IS NOT NULL AND m.expiryDate <= :date AND m.active = true")
    List<Medicine> findExpiringBefore(@Param("date") LocalDate date);

    @Query("SELECT m FROM Medicine m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Medicine> searchByName(@Param("keyword") String keyword);

    List<Medicine> findByCategoryId(Long categoryId);
}