package ReactMadeleine.Garden.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,14}$", message = "Invalid phone number")
    @Column(nullable = false)
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false)
    private String email;

    @NotBlank(message = "Booking type is required")
    @Column(name = "booking_type", nullable = false)
    private String bookingType;

    @NotBlank(message = "Country is required")
    @Column(nullable = false)
    private String country;

    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @FutureOrPresent(message = "Check-out date must be today or in the future")
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @NotNull(message = "Arrival time is required")
    @Column(nullable = false)
    private LocalTime arrival;

    @NotNull(message = "Status is required")
    @Pattern(regexp = "^(Pending|Confirmed|Cancelled)$",
            message = "Status must be either 'Pending', 'Confirmed', or 'Cancelled'")
    @Column(nullable = false)
    private String status = "Pending";

    @NotNull(message = "Total price is required")
    @Column(name = "total_price", nullable = false)
    private Double totalPrice = 0.0;

    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;

    // Custom setters for validated fields
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        this.firstName = firstName.trim();
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        this.lastName = lastName.trim();
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.toLowerCase().trim();
    }

    public void setPhone(String phone) {
        if (phone == null || !phone.matches("^\\+?[0-9]{10,14}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.phone = phone.trim();
    }

    public void setStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (!status.matches("^(Pending|Confirmed|Cancelled)$")) {
            throw new IllegalArgumentException("Invalid status value. Must be 'Pending', 'Confirmed', or 'Cancelled'");
        }
        this.status = status;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        if (checkInDate == null) {
            throw new IllegalArgumentException("Check-in date cannot be null");
        }
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }
        this.checkInDate = checkInDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        if (checkOutDate == null) {
            throw new IllegalArgumentException("Check-out date cannot be null");
        }
        if (checkOutDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-out date cannot be in the past");
        }
        if (this.checkInDate != null && checkOutDate.isBefore(this.checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        this.checkOutDate = checkOutDate;
    }

    public void setTotalPrice(Double totalPrice) {
        if (totalPrice == null) {
            throw new IllegalArgumentException("Total price cannot be null");
        }
        if (totalPrice < 0) {
            throw new IllegalArgumentException("Total price cannot be negative");
        }
        this.totalPrice = totalPrice;
    }

    public void setArrival(LocalTime arrival) {
        if (arrival == null) {
            throw new IllegalArgumentException("Arrival time cannot be null");
        }
        this.arrival = arrival;
    }

    public void setBookingType(String bookingType) {
        if (bookingType == null || bookingType.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking type cannot be null or empty");
        }
        this.bookingType = bookingType.trim();
    }

    public void setCountry(String country) {
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
        this.country = country.trim();
    }

    public void setCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        this.city = city.trim();
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        this.address = address.trim();
    }

    public void setDefaultValues() {
        if (this.status == null) {
            this.status = "Pending";
        }
        if (this.totalPrice == null) {
            this.totalPrice = 0.0;
        }
    }

    @PrePersist
    @PreUpdate
    public void validate() {
        // Validate check-in and check-out dates
        if (checkOutDate != null && checkInDate != null && checkOutDate.isBefore(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        // Validate status
        if (status != null && !status.matches("^(Pending|Confirmed|Cancelled)$")) {
            throw new IllegalArgumentException("Invalid status value");
        }

        // Validate total price
        if (totalPrice != null && totalPrice < 0) {
            throw new IllegalArgumentException("Total price cannot be negative");
        }

        // Trim strings and validate required fields
        if (firstName != null) firstName = firstName.trim();
        if (lastName != null) lastName = lastName.trim();
        if (email != null) email = email.trim().toLowerCase();
        if (phone != null) phone = phone.trim();
        if (bookingType != null) bookingType = bookingType.trim();
        if (country != null) country = country.trim();
        if (city != null) city = city.trim();
        if (address != null) address = address.trim();

        // Validate all required fields are not empty after trimming
        if (firstName == null || firstName.isEmpty()) throw new IllegalArgumentException("First name is required");
        if (lastName == null || lastName.isEmpty()) throw new IllegalArgumentException("Last name is required");
        if (email == null || email.isEmpty()) throw new IllegalArgumentException("Email is required");
        if (phone == null || phone.isEmpty()) throw new IllegalArgumentException("Phone is required");
        if (bookingType == null || bookingType.isEmpty()) throw new IllegalArgumentException("Booking type is required");
        if (country == null || country.isEmpty()) throw new IllegalArgumentException("Country is required");
        if (city == null || city.isEmpty()) throw new IllegalArgumentException("City is required");
        if (address == null || address.isEmpty()) throw new IllegalArgumentException("Address is required");
    }
}