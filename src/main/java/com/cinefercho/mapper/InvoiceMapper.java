package com.cinefercho.mapper;

import com.cinefercho.dto.InvoiceResponse;
import com.cinefercho.dto.InvoiceResponse.ClientResponse;
import com.cinefercho.dto.InvoiceResponse.ConcessionLineResponse;
import com.cinefercho.dto.InvoiceResponse.TicketLineResponse;
import com.cinefercho.entity.ConcessionItem;
import com.cinefercho.entity.Invoice;
import com.cinefercho.entity.TicketItem;
import com.cinefercho.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceMapper {

    private final CatalogMapper catalogMapper;

    public InvoiceMapper(CatalogMapper catalogMapper) {
        this.catalogMapper = catalogMapper;
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        User client = invoice.getUser();
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getCreatedAt(),
                new ClientResponse(
                        client.getId(),
                        client.getFullName(),
                        client.getEmail(),
                        client.getMembershipType()),
                catalogMapper.toResponse(invoice.getTheater()),
                toTicketLines(invoice.getTicketItems()),
                toConcessionLines(invoice.getConcessionItems()),
                invoice.getSubtotal(),
                invoice.getDiscountAmount(),
                invoice.getTotalAmount());
    }

    private List<TicketLineResponse> toTicketLines(List<TicketItem> items) {
        return items.stream()
                .map(item -> new TicketLineResponse(
                        item.getId(),
                        item.getScreening().getId(),
                        item.getScreening().getMovie().getTitle(),
                        item.getScreening().getStartTime(),
                        item.getSeat().getId(),
                        item.getSeat().getRowLetter(),
                        item.getSeat().getSeatNumber(),
                        item.getSeat().isVip(),
                        item.getPrice()))
                .toList();
    }

    private List<ConcessionLineResponse> toConcessionLines(List<ConcessionItem> items) {
        return items.stream()
                .map(item -> new ConcessionLineResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()))
                .toList();
    }
}
