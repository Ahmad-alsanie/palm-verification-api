package com.palm.repository;

import com.palm.model.PalmData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PalmDataRepository extends JpaRepository<PalmData, String> {
}
