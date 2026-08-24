package com.cinefercho.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_seat_hall_row_number",
                columnNames = {"hall_id", "row_letter", "seat_number"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private CinemaHall hall;

    @NotBlank
    @Size(max = 5)
    @Column(name = "row_letter", nullable = false, length = 5)
    private String rowLetter;

    @Min(1)
    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Builder.Default
    @Column(name = "is_vip", nullable = false)
    private boolean isVip = false;

    @OneToMany(mappedBy = "seat")
    @Builder.Default
    private List<TicketItem> ticketItems = new ArrayList<>();
}
