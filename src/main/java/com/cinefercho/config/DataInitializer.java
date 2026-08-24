package com.cinefercho.config;

import com.cinefercho.entity.CinemaHall;
import com.cinefercho.entity.City;
import com.cinefercho.entity.MembershipPlan;
import com.cinefercho.entity.Movie;
import com.cinefercho.entity.Product;
import com.cinefercho.entity.Screening;
import com.cinefercho.entity.Theater;
import com.cinefercho.entity.User;
import com.cinefercho.entity.enums.HallType;
import com.cinefercho.entity.enums.MembershipType;
import com.cinefercho.entity.enums.MovieStatus;
import com.cinefercho.entity.enums.ProductCategory;
import com.cinefercho.entity.enums.ScreeningFormat;
import com.cinefercho.entity.enums.UserRole;
import com.cinefercho.repository.CityRepository;
import com.cinefercho.repository.MembershipPlanRepository;
import com.cinefercho.repository.MovieRepository;
import com.cinefercho.repository.ProductRepository;
import com.cinefercho.repository.ScreeningRepository;
import com.cinefercho.repository.TheaterRepository;
import com.cinefercho.repository.UserRepository;
import com.cinefercho.util.SeatFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final HallType[] HALL_TYPES = {
            HallType.STANDARD,
            HallType.STANDARD,
            HallType.STANDARD,
            HallType.STANDARD,
            HallType.XD,
            HallType.XD,
            HallType.D_BOX
    };

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;
    private final MovieRepository movieRepository;
    private final ScreeningRepository screeningRepository;
    private final ProductRepository productRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            CityRepository cityRepository,
            TheaterRepository theaterRepository,
            MovieRepository movieRepository,
            ScreeningRepository screeningRepository,
            ProductRepository productRepository,
            MembershipPlanRepository membershipPlanRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.theaterRepository = theaterRepository;
        this.movieRepository = movieRepository;
        this.screeningRepository = screeningRepository;
        this.productRepository = productRepository;
        this.membershipPlanRepository = membershipPlanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        seedUsers();
        List<Theater> theaters = seedCitiesAndTheaters();
        List<Movie> movies = seedMovies();
        seedScreenings(theaters, movies);
        seedProducts();
        seedMembershipPlans();
    }

    private void seedUsers() {
        userRepository.save(User.builder()
                .fullName("Administrador Cinefercho")
                .email("admin@cinefercho.com")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ROLE_ADMIN)
                .membershipType(MembershipType.NONE)
                .build());
        userRepository.save(User.builder()
                .fullName("Cliente Demo")
                .email("cliente@gmail.com")
                .password(passwordEncoder.encode("cliente123"))
                .role(UserRole.ROLE_CLIENT)
                .membershipType(MembershipType.CINE_FAN_GOLD)
                .build());
    }

    private List<Theater> seedCitiesAndTheaters() {
        City bogota = cityRepository.save(City.builder().name("Bogotá").department("Cundinamarca").build());
        City medellin = cityRepository.save(City.builder().name("Medellín").department("Antioquia").build());
        City cali = cityRepository.save(City.builder().name("Cali").department("Valle del Cauca").build());
        City cucuta = cityRepository.save(City.builder().name("Cúcuta").department("Norte de Santander").build());
        City barranquilla = cityRepository.save(City.builder().name("Barranquilla").department("Atlántico").build());

        return List.of(
                saveTheater(bogota, "Cinemark Atlantis", "Centro Comercial Atlantis Plaza, Cl. 81 #13-05"),
                saveTheater(cucuta, "Cinemark Ventura Plaza", "Centro Comercial Ventura Plaza, Av. 0 #15N-21"),
                saveTheater(medellin, "Cinemark Premium Plaza", "Centro Comercial Premium Plaza, Cl. 30A #82A-26"),
                saveTheater(cali, "Cinemark Chipichape", "Centro Comercial Chipichape, Cl. 38N #6N-35"),
                saveTheater(barranquilla, "Cinemark Portal del Prado", "Centro Comercial Portal del Prado, Cra. 46 #53-46")
        );
    }

    private Theater saveTheater(City city, String name, String address) {
        Theater theater = Theater.builder()
                .city(city)
                .name(name)
                .address(address)
                .active(true)
                .build();
        for (int index = 0; index < HALL_TYPES.length; index++) {
            CinemaHall hall = CinemaHall.builder()
                    .name("Sala " + (index + 1))
                    .hallType(HALL_TYPES[index])
                    .totalCapacity(30)
                    .totalRows(5)
                    .totalColumns(6)
                    .build();
            SeatFactory.fillHall(hall);
            theater.addHall(hall);
        }
        return theaterRepository.save(theater);
    }

    private List<Movie> seedMovies() {
        Movie dune = movieRepository.save(Movie.builder()
                .title("Dune: Part Two")
                .synopsis("Paul Atreides se une a los Fremen y busca venganza contra los conspiradores que destruyeron a su familia, mientras intenta evitar un futuro oscuro.")
                .durationMinutes(166)
                .genre("Ciencia ficción")
                .rating("PG-13")
                .posterUrl("https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg")
                .status(MovieStatus.NOW_SHOWING)
                .build());
        Movie insideOut = movieRepository.save(Movie.builder()
                .title("Inside Out 2")
                .synopsis("Riley entra a la adolescencia y nuevas emociones llegan a su mente, desafiando el equilibrio de Alegría, Tristeza, Furia, Miedo y Desagrado.")
                .durationMinutes(96)
                .genre("Animación")
                .rating("PG")
                .posterUrl("https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg")
                .status(MovieStatus.NOW_SHOWING)
                .build());
        Movie wicked = movieRepository.save(Movie.builder()
                .title("Wicked")
                .synopsis("La historia no contada de Elphaba, una joven incomprendida por su piel verde, y su amistad con Glinda en la Tierra de Oz.")
                .durationMinutes(160)
                .genre("Musical")
                .rating("PG")
                .posterUrl("https://image.tmdb.org/t/p/w500/c5Tqxeo1UpBvnAc3csUm7j3dzTE.jpg")
                .status(MovieStatus.COMING_SOON)
                .build());
        return List.of(dune, insideOut, wicked);
    }

    private void seedScreenings(List<Theater> theaters, List<Movie> movies) {
        Theater atlantis = theaters.get(0);
        Theater ventura = theaters.get(1);
        List<CinemaHall> atlantisHalls = atlantis.getHalls();
        List<CinemaHall> venturaHalls = ventura.getHalls();
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        screeningRepository.save(buildScreening(
                movies.get(0), atlantisHalls.get(0), today.atTime(16, 0), ScreeningFormat.STANDARD_2D, "18000"));
        screeningRepository.save(buildScreening(
                movies.get(0), atlantisHalls.get(4), today.atTime(19, 30), ScreeningFormat.XD_2D, "25000"));
        screeningRepository.save(buildScreening(
                movies.get(1), venturaHalls.get(0), tomorrow.atTime(17, 0), ScreeningFormat.STANDARD_3D, "20000"));
        screeningRepository.save(buildScreening(
                movies.get(2), atlantisHalls.get(6), tomorrow.atTime(20, 15), ScreeningFormat.D_BOX, "32000"));
    }

    private Screening buildScreening(
            Movie movie, CinemaHall hall, LocalDateTime startTime, ScreeningFormat format, String price) {
        return Screening.builder()
                .movie(movie)
                .hall(hall)
                .startTime(startTime)
                .endTime(startTime.plusMinutes(movie.getDurationMinutes() + 15L))
                .ticketPrice(new BigDecimal(price))
                .format(format)
                .build();
    }

    private void seedProducts() {
        productRepository.save(Product.builder()
                .name("Crispetas Grandes")
                .description("Crispetas de maíz recién hechas, tamaño grande, con mantequilla.")
                .price(new BigDecimal("18000"))
                .category(ProductCategory.POPCORN)
                .stock(120)
                .imageUrl("https://images.unsplash.com/photo-1585647347483-22b0495b5c8e?auto=format&fit=crop&w=800&q=80")
                .build());
        productRepository.save(Product.builder()
                .name("Gaseosa 1L")
                .description("Bebida gaseosa de 1 litro para acompañar la función.")
                .price(new BigDecimal("8000"))
                .category(ProductCategory.BEVERAGE)
                .stock(200)
                .imageUrl("https://images.unsplash.com/photo-1629203851122-3726ecdf080e?auto=format&fit=crop&w=800&q=80")
                .build());
        productRepository.save(Product.builder()
                .name("Combo Pareja")
                .description("Crispetas extra grandes, dos gaseosas y una golosina para compartir.")
                .price(new BigDecimal("42000"))
                .category(ProductCategory.COMBO)
                .stock(80)
                .imageUrl("https://images.unsplash.com/photo-1595769812594-db4f990d25c9?auto=format&fit=crop&w=800&q=80")
                .build());
        productRepository.save(Product.builder()
                .name("Nachos")
                .description("Nachos crujientes con queso cheddar caliente.")
                .price(new BigDecimal("16000"))
                .category(ProductCategory.CANDY)
                .stock(90)
                .imageUrl("https://images.unsplash.com/photo-1513456852971-30c0b8199d4d?auto=format&fit=crop&w=800&q=80")
                .build());
    }

    private void seedMembershipPlans() {
        membershipPlanRepository.save(MembershipPlan.builder()
                .name("CINE_FAN")
                .monthlyPrice(new BigDecimal("24900"))
                .discountPercentageTickets(new BigDecimal("10.00"))
                .discountPercentageConcession(new BigDecimal("10.00"))
                .build());
        membershipPlanRepository.save(MembershipPlan.builder()
                .name("CINE_FAN_GOLD")
                .monthlyPrice(new BigDecimal("44900"))
                .discountPercentageTickets(new BigDecimal("20.00"))
                .discountPercentageConcession(new BigDecimal("20.00"))
                .build());
    }
}
