package ReactMadeleine.Garden.repository;




import ReactMadeleine.Garden.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Méthodes de recherche de base
    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);
    List<Booking> findByStatus(String status);
    List<Booking> findByBookingType(String bookingType);
    List<Booking> findByTotalPriceBetween(Double minPrice, Double maxPrice);
    List<Booking> findByEmailIgnoreCase(String email);

    // Recherche avancée avec tous les critères
    @Query("SELECT b FROM Booking b WHERE " +
            "(LOWER(b.firstName) LIKE LOWER(CONCAT('%', :customerName, '%')) OR " +
            "LOWER(b.lastName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:startDate IS NULL OR b.checkInDate >= :startDate) AND " +
            "(:endDate IS NULL OR b.checkOutDate <= :endDate) AND " +
            "(:minPrice IS NULL OR b.totalPrice >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.totalPrice <= :maxPrice)")
    List<Booking> searchBookings(
            @Param("customerName") String customerName,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );

    // Recherche des chevauchements
    @Query("SELECT b FROM Booking b WHERE " +
            "b.status != 'Cancelled' AND " +
            "((b.checkInDate BETWEEN :checkIn AND :checkOut) OR " +
            "(b.checkOutDate BETWEEN :checkIn AND :checkOut) OR " +
            "(:checkIn BETWEEN b.checkInDate AND b.checkOutDate) OR " +
            "(:checkOut BETWEEN b.checkInDate AND b.checkOutDate))")
    List<Booking> findOverlappingBookings(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    // Recherche des réservations à venir
    @Query("SELECT b FROM Booking b WHERE b.checkInDate >= :date AND b.status = 'Confirmed'")
    List<Booking> findUpcomingBookings(@Param("date") LocalDate date);

    // Recherche par plage de dates et statut
    @Query("SELECT b FROM Booking b WHERE " +
            "b.checkInDate >= :startDate AND b.checkOutDate <= :endDate AND " +
            "(:status IS NULL OR b.status = :status)")
    List<Booking> findByDateRangeAndStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") String status
    );
}