package com.org.pharmacy.Repository;

import com.org.pharmacy.Entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    @Query("SELECT m FROM Medicine m WHERE m.name = :medicineName")
    Medicine getMedicineDetails(@Param("medicineName") String medicineName);

}
