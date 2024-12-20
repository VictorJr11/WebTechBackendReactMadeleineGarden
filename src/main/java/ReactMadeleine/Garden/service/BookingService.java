package ReactMadeleine.Garden.service;


import ReactMadeleine.Garden.exception.BookingNotFoundException;
import ReactMadeleine.Garden.exception.InvalidBookingStateException;
import ReactMadeleine.Garden.model.Booking;
import ReactMadeleine.Garden.repository.BookingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository bookingRepository;

    private static final String[] VALID_STATUSES = {"Pending", "Confirmed", "Cancelled"};

    @Transactional
    public Booking createBooking(@Valid Booking booking) {
        logger.info("Creating new booking for {} {}", booking.getFirstName(), booking.getLastName());
        validateNewBooking(booking);

        booking.setDefaultValues();
        booking.setStatus("Pending");

        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Created booking with ID: {}", savedBooking.getId());

        return savedBooking;
    }

    public Optional<Booking> getBookingById(Long id) {
        logger.debug("Fetching booking with ID: {}", id);
        return bookingRepository.findById(id);
    }

    public List<Booking> getAllBookings() {
        logger.debug("Fetching all bookings");
        return bookingRepository.findAll();
    }

    @Transactional
    public Booking updateBookingStatus(Long id, String newStatus) {
        logger.info("Attempting to update booking status: ID={}, newStatus={}", id, newStatus);

        validateStatus(newStatus);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        validateStatusTransition(booking.getStatus(), newStatus);

        String oldStatus = booking.getStatus();
        booking.setStatus(newStatus);

        Booking updatedBooking = bookingRepository.save(booking);
        logger.info("Successfully updated booking {} status from {} to {}",
                id, oldStatus, newStatus);

        return updatedBooking;
    }

    @Transactional
    public Booking updateBooking(Long id, @Valid Booking bookingDetails) {
        logger.info("Updating booking with ID: {}", id);

        Booking existingBooking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        // Vérifier si la réservation peut être mise à jour
        if ("Cancelled".equals(existingBooking.getStatus())) {
            throw new InvalidBookingStateException("Cannot update cancelled booking");
        }

        // Mise à jour des champs
        updateBookingFields(existingBooking, bookingDetails);

        // Validation de la mise à jour
        validateBookingUpdate(existingBooking);

        Booking updatedBooking = bookingRepository.save(existingBooking);
        logger.info("Successfully updated booking with ID: {}", id);

        return updatedBooking;
    }

    @Transactional
    public void deleteBooking(Long id) {
        logger.info("Attempting to delete booking with ID: {}", id);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        // Vérifier si la réservation peut être supprimée
        if ("Confirmed".equals(booking.getStatus())) {
            throw new InvalidBookingStateException("Cannot delete confirmed booking");
        }

        bookingRepository.deleteById(id);
        logger.info("Successfully deleted booking with ID: {}", id);
    }

    public List<Booking> searchBookings(String customerName, String status,
                                        LocalDate startDate, LocalDate endDate) {
        logger.debug("Searching bookings with criteria: customerName={}, status={}, startDate={}, endDate={}",
                customerName, status, startDate, endDate);

        // Implémentation de la recherche (à adapter selon vos besoins)
        return bookingRepository.findAll();
    }

    // Méthodes de validation privées
    private void validateNewBooking(Booking booking) {
        if (booking.getCheckOutDate().isBefore(booking.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (booking.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        // Vérifier les chevauchements de dates
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );

        if (!overlappingBookings.isEmpty()) {
            throw new IllegalArgumentException("Selected dates overlap with existing bookings");
        }
    }

    private void validateStatus(String status) {
        if (status == null || !java.util.Arrays.asList(VALID_STATUSES).contains(status)) {
            throw new IllegalArgumentException(
                    "Invalid status. Must be one of: " + String.join(", ", VALID_STATUSES)
            );
        }
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if ("Cancelled".equals(currentStatus)) {
            throw new InvalidBookingStateException("Cannot change status of cancelled booking");
        }

        if ("Confirmed".equals(currentStatus) && "Pending".equals(newStatus)) {
            throw new InvalidBookingStateException("Cannot change confirmed booking back to pending");
        }
    }

    private void validateBookingUpdate(Booking booking) {
        if (booking.getCheckOutDate().isBefore(booking.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (booking.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
    }

    private void updateBookingFields(Booking existing, Booking details) {
        existing.setFirstName(details.getFirstName());
        existing.setLastName(details.getLastName());
        existing.setEmail(details.getEmail());
        existing.setPhone(details.getPhone());
        existing.setBookingType(details.getBookingType());
        existing.setCountry(details.getCountry());
        existing.setCity(details.getCity());
        existing.setAddress(details.getAddress());
        existing.setCheckInDate(details.getCheckInDate());
        existing.setCheckOutDate(details.getCheckOutDate());
        existing.setArrival(details.getArrival());
        existing.setTotalPrice(details.getTotalPrice());
    }
}