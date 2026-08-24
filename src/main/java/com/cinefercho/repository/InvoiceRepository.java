package com.cinefercho.repository;

import com.cinefercho.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(UUID invoiceNumber);

    @Query("""
            select i from Invoice i
            join fetch i.user
            join fetch i.theater t
            join fetch t.city
            where i.user.id = :userId
            order by i.createdAt desc
            """)
    List<Invoice> findDetailedByUserId(@Param("userId") Long userId);
}
