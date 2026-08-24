package com.cinefercho.repository;

import com.cinefercho.entity.ConcessionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConcessionItemRepository extends JpaRepository<ConcessionItem, Long> {

    boolean existsByProduct_Id(Long productId);
}
