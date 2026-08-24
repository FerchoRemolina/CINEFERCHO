package com.cinefercho.service;

import com.cinefercho.dto.ConcessionItemRequest;
import com.cinefercho.dto.CreatePurchaseRequest;
import com.cinefercho.dto.InvoiceResponse;
import com.cinefercho.entity.ConcessionItem;
import com.cinefercho.entity.Invoice;
import com.cinefercho.entity.MembershipPlan;
import com.cinefercho.entity.Product;
import com.cinefercho.entity.Screening;
import com.cinefercho.entity.Seat;
import com.cinefercho.entity.TicketItem;
import com.cinefercho.entity.User;
import com.cinefercho.entity.enums.MembershipType;
import com.cinefercho.exception.InsufficientStockException;
import com.cinefercho.exception.ResourceNotFoundException;
import com.cinefercho.exception.SeatAlreadyReservedException;
import com.cinefercho.exception.UnauthorizedException;
import com.cinefercho.mapper.InvoiceMapper;
import com.cinefercho.repository.InvoiceRepository;
import com.cinefercho.repository.MembershipPlanRepository;
import com.cinefercho.repository.ProductRepository;
import com.cinefercho.repository.ScreeningRepository;
import com.cinefercho.repository.SeatRepository;
import com.cinefercho.repository.TicketItemRepository;
import com.cinefercho.repository.UserRepository;
import com.cinefercho.security.UserPrincipal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PurchaseService {

    private static final BigDecimal GOLD_RATE = new BigDecimal("0.10");
    private static final BigDecimal PRO_RATE = new BigDecimal("0.20");

    private final UserRepository userRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final TicketItemRepository ticketItemRepository;
    private final ProductRepository productRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final MovieCatalogPolicy movieCatalogPolicy;

    public PurchaseService(
            UserRepository userRepository,
            ScreeningRepository screeningRepository,
            SeatRepository seatRepository,
            TicketItemRepository ticketItemRepository,
            ProductRepository productRepository,
            MembershipPlanRepository membershipPlanRepository,
            InvoiceRepository invoiceRepository,
            InvoiceMapper invoiceMapper,
            MovieCatalogPolicy movieCatalogPolicy) {
        this.userRepository = userRepository;
        this.screeningRepository = screeningRepository;
        this.seatRepository = seatRepository;
        this.ticketItemRepository = ticketItemRepository;
        this.productRepository = productRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.movieCatalogPolicy = movieCatalogPolicy;
    }

    @Transactional
    public InvoiceResponse checkout(UserPrincipal principal, CreatePurchaseRequest request) {
        User user = resolveUser(principal);
        if (user.expireIfNeeded()) {
            userRepository.save(user);
        }
        Screening screening = screeningRepository.findDetailedById(request.screeningId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la función con id " + request.screeningId()));
        movieCatalogPolicy.assertPurchasable(screening);

        List<Seat> seats = validateAndLoadSeats(screening, request.seatIds());
        List<ProductLine> productLines = validateAndLoadProducts(request.concessionItems());
        MembershipPlan purchasedPlan = resolveMembershipPurchase(user, request.buyMembershipPlanId());

        if (purchasedPlan != null) {
            user.setMembershipType(toMembershipType(purchasedPlan));
            user.setMembershipExpiresAt(Instant.now().plus(Duration.ofDays(purchasedPlan.getDurationDays())));
            userRepository.save(user);
        }

        BigDecimal ticketSubtotal = screening.getTicketPrice()
                .multiply(BigDecimal.valueOf(seats.size()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal concessionSubtotal = productLines.stream()
                .map(ProductLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal membershipSubtotal = purchasedPlan != null
                ? purchasedPlan.getMonthlyPrice().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountableSubtotal = ticketSubtotal.add(concessionSubtotal);
        BigDecimal discountAmount = discountableSubtotal
                .multiply(discountRate(user))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal subtotal = discountableSubtotal.add(membershipSubtotal);
        BigDecimal totalAmount = subtotal.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);

        Invoice invoice = Invoice.builder()
                .user(user)
                .theater(screening.getHall().getTheater())
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .build();

        for (Seat seat : seats) {
            invoice.addTicketItem(TicketItem.builder()
                    .screening(screening)
                    .seat(seat)
                    .price(screening.getTicketPrice())
                    .build());
        }
        for (ProductLine line : productLines) {
            line.product().setStock(line.product().getStock() - line.quantity());
            productRepository.save(line.product());
            invoice.addConcessionItem(ConcessionItem.builder()
                    .product(line.product())
                    .quantity(line.quantity())
                    .unitPrice(line.unitPrice())
                    .subtotal(line.lineTotal())
                    .build());
        }

        try {
            Invoice saved = invoiceRepository.saveAndFlush(invoice);
            return invoiceMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw seatAlreadyReserved(seats);
        }
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> findMyInvoices(UserPrincipal principal) {
        User user = resolveUser(principal);
        List<Invoice> invoices = invoiceRepository.findDetailedByUserId(user.getId());
        invoices.forEach(this::initializeInvoiceGraph);
        return invoices.stream().map(invoiceMapper::toResponse).toList();
    }

    private User resolveUser(UserPrincipal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Se requiere autenticación para completar la compra.");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("El usuario autenticado ya no existe."));
    }

    private List<Seat> validateAndLoadSeats(Screening screening, List<Long> seatIds) {
        Set<Long> uniqueIds = new HashSet<>(seatIds);
        if (uniqueIds.size() != seatIds.size()) {
            throw new IllegalArgumentException("La solicitud contiene asientos duplicados.");
        }
        List<Seat> seats = seatRepository.findByIdInAndHall_Id(uniqueIds, screening.getHall().getId());
        if (seats.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException(
                    "Uno o más asientos no existen o no pertenecen a la sala de la función.");
        }
        for (Seat seat : seats) {
            if (ticketItemRepository.existsByScreeningIdAndSeatId(screening.getId(), seat.getId())) {
                throw seatAlreadyReserved(List.of(seat));
            }
        }
        return seats;
    }

    private List<ProductLine> validateAndLoadProducts(List<ConcessionItemRequest> requests) {
        return requests.stream().map(item -> {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No existe el producto con id " + item.productId()));
            if (product.getStock() < item.quantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para el producto '" + product.getName() + "'.");
            }
            BigDecimal unitPrice = product.getPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            return new ProductLine(product, item.quantity(), unitPrice, lineTotal);
        }).toList();
    }

    private MembershipPlan resolveMembershipPurchase(User user, Long planId) {
        if (planId == null) {
            return null;
        }
        if (user.hasActiveMembership()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya tienes una membresía activa. No puedes adquirir otra hasta que expire.");
        }
        return membershipPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el plan de membresía con id " + planId));
    }

    private MembershipType toMembershipType(MembershipPlan plan) {
        try {
            return MembershipType.valueOf(plan.getName());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("El plan '" + plan.getName() + "' no corresponde a un tipo de membresía.");
        }
    }

    private BigDecimal discountRate(User user) {
        if (!user.hasActiveMembership()) {
            return BigDecimal.ZERO;
        }
        return switch (user.getMembershipType()) {
            case GOLD -> GOLD_RATE;
            case PRO -> PRO_RATE;
            case NONE -> BigDecimal.ZERO;
        };
    }

    private void initializeInvoiceGraph(Invoice invoice) {
        invoice.getTicketItems().forEach(item -> {
            item.getScreening().getMovie().getTitle();
            item.getSeat().getRowLetter();
        });
        invoice.getConcessionItems().forEach(item -> item.getProduct().getName());
        invoice.getTheater().getCity().getName();
    }

    private SeatAlreadyReservedException seatAlreadyReserved(List<Seat> seats) {
        Seat seat = seats.get(0);
        return new SeatAlreadyReservedException(
                "El asiento " + seat.getRowLetter() + seat.getSeatNumber()
                        + " ya ha sido reservado por otro cliente");
    }

    private record ProductLine(Product product, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }
}
