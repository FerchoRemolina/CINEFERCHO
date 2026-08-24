package com.cinefercho.repository;

import com.cinefercho.entity.Product;
import com.cinefercho.entity.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(ProductCategory category);
}
