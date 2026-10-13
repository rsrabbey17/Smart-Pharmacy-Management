package com.pharmacy.smart_pharmacy.repository;

import com.pharmacy.smart_pharmacy.entity.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {
    List<StockAlert> findByResolvedFalse();
    boolean existsByMedicineIdAndAlertTypeAndResolvedFalse(Long medicineId, String alertType);
}