package ReactMadeleine.Garden.controller;


import ReactMadeleine.Garden.exception.BookingNotFoundException;
import ReactMadeleine.Garden.exception.InvalidBookingStateException;
import ReactMadeleine.Garden.model.Booking;
import ReactMadeleine.Garden.repository.BookingRepository;
import ReactMadeleine.Garden.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody Booking booking) {
        logger.info("REST request to create Booking");
        Booking result = bookingService.createBooking(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        logger.info("REST request to get Booking : {}", id);
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        logger.info("REST request to get all Bookings");
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody Booking bookingDetails) {
        logger.info("REST request to update Booking : {}", id);
        Booking updatedBooking = bookingService.updateBooking(id, bookingDetails);
        return ResponseEntity.ok(updatedBooking);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity updateBookingStatus(
            @PathVariable Long id,
            @RequestBody BookingStatusRequest statusRequest) {
        logger.info("REST request to update Booking status : {}, new status: {}", id, statusRequest.getStatus());

        if (statusRequest.getStatus() == null || statusRequest.getStatus().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Booking updatedBooking = bookingService.updateBookingStatus(id, statusRequest.getStatus());
        return ResponseEntity.ok(updatedBooking);
    }


    public class BookingStatusRequest {
        private String status;

        // Getter and setter
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        logger.info("REST request to delete Booking : {}", id);
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<Booking> searchBookings(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        return bookingRepository.searchBookings(
                customerName,
                status,
                startDate,
                endDate,
                minPrice,
                maxPrice
        );
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<String> handleBookingNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<String> handleInvalidBookingState(InvalidBookingStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}