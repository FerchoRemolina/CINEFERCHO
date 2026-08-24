package com.cinefercho.controller;

import com.cinefercho.dto.CinemaHallRequest;
import com.cinefercho.dto.CinemaHallResponse;
import com.cinefercho.dto.MovieRequest;
import com.cinefercho.dto.MovieResponse;
import com.cinefercho.dto.ProductRequest;
import com.cinefercho.dto.ProductResponse;
import com.cinefercho.dto.RecurringScreeningRequest;
import com.cinefercho.dto.RecurringScreeningResponse;
import com.cinefercho.dto.ScreeningRequest;
import com.cinefercho.dto.ScreeningResponse;
import com.cinefercho.dto.TheaterResponse;
import com.cinefercho.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/movies")
    public List<MovieResponse> movies() {
        return adminService.findMovies();
    }

    @GetMapping("/screenings")
    public List<ScreeningResponse> screenings() {
        return adminService.findScreenings();
    }

    @GetMapping("/halls")
    public List<CinemaHallResponse> halls() {
        return adminService.findHalls();
    }

    @GetMapping("/theaters")
    public List<TheaterResponse> theaters() {
        return adminService.findTheaters();
    }

    @GetMapping("/products")
    public List<ProductResponse> products() {
        return adminService.findProducts();
    }

    @PostMapping("/movies")
    @ResponseStatus(HttpStatus.CREATED)
    public MovieResponse createMovie(@Valid @RequestBody MovieRequest request) {
        return adminService.createMovie(request);
    }

    @PutMapping("/movies/{id}")
    public MovieResponse updateMovie(@PathVariable Long id, @Valid @RequestBody MovieRequest request) {
        return adminService.updateMovie(id, request);
    }

    @DeleteMapping("/movies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMovie(@PathVariable Long id) {
        adminService.deleteMovie(id);
    }

    @PostMapping("/screenings")
    @ResponseStatus(HttpStatus.CREATED)
    public ScreeningResponse createScreening(@Valid @RequestBody ScreeningRequest request) {
        return adminService.createScreening(request);
    }

    @PostMapping("/screenings/recurring")
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringScreeningResponse createRecurringScreenings(
            @Valid @RequestBody RecurringScreeningRequest request) {
        return adminService.createRecurringScreenings(request);
    }

    @PutMapping("/screenings/{id}")
    public ScreeningResponse updateScreening(@PathVariable Long id, @Valid @RequestBody ScreeningRequest request) {
        return adminService.updateScreening(id, request);
    }

    @DeleteMapping("/screenings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScreening(@PathVariable Long id) {
        adminService.deleteScreening(id);
    }

    @PostMapping("/halls")
    @ResponseStatus(HttpStatus.CREATED)
    public CinemaHallResponse createHall(@Valid @RequestBody CinemaHallRequest request) {
        return adminService.createHall(request);
    }

    @PutMapping("/halls/{id}")
    public CinemaHallResponse updateHall(@PathVariable Long id, @Valid @RequestBody CinemaHallRequest request) {
        return adminService.updateHall(id, request);
    }

    @DeleteMapping("/halls/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHall(@PathVariable Long id) {
        adminService.deleteHall(id);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return adminService.createProduct(request);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return adminService.updateProduct(id, request);
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
    }
}
