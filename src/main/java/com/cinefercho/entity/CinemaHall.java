package com.cinefercho.entity;

import com.cinefercho.entity.enums.HallType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "cinema_halls")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaHall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "hall_type", nullable = false, length = 20)
    private HallType hallType;

    @Min(1)
    @Column(name = "total_capacity", nullable = false)
    private int totalCapacity;

    @Min(1)
    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Min(1)
    @Column(name = "total_columns", nullable = false)
    private int totalColumns;

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "hall", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Screening> screenings = new ArrayList<>();

    public void addSeat(Seat seat) {
        seats.add(seat);
        seat.setHall(this);
    }

    public void addScreening(Screening screening) {
        screenings.add(screening);
        screening.setHall(this);
    }
}
