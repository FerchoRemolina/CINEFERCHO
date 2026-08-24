package com.cinefercho.controller;

import com.cinefercho.dto.CreatePurchaseRequest;
import com.cinefercho.dto.InvoiceResponse;
import com.cinefercho.security.UserPrincipal;
import com.cinefercho.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client")
@CrossOrigin(origins = "*")
public class ClientController {

    private final PurchaseService purchaseService;

    public ClientController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse purchase(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePurchaseRequest request) {
        return purchaseService.checkout(principal, request);
    }

    @GetMapping("/invoices")
    public List<InvoiceResponse> invoices(@AuthenticationPrincipal UserPrincipal principal) {
        return purchaseService.findMyInvoices(principal);
    }
}
