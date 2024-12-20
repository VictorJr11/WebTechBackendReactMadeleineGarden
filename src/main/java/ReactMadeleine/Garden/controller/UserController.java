package ReactMadeleine.Garden.controller;


import ReactMadeleine.Garden.model.User;
import ReactMadeleine.Garden.repository.UserRepository;
import ReactMadeleine.Garden.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;




    // Create
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            // Validate user object
            if (user == null) {
                throw new IllegalArgumentException("User object is null");
            }

            // Validate required fields
            if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
                throw new IllegalArgumentException("Required fields (username, password, email) cannot be null");
            }

            // Validate email format
            if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("Invalid email format");
            }

            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Save the user entity
            User newUser = userRepository.save(user);

            // Create multilingual subject
            String subject = "Welcome to Madeleine Garden | Bienvenue à Madeleine Garden | Murakaza neza muri Madeleine Garden";

            // Create multilingual body
            String body = "Dear " + newUser.getUsername() + ",\n\n"
                    + "Welcome to Madeleine Garden! We're thrilled to have you join our community.\n"
                    + "Your account has been successfully created, and you're now ready to explore all that Madeleine Garden has to offer.\n"
                    + "If you have any questions or need assistance, please don't hesitate to reach out to our support team.\n\n"
                    + "We look forward to seeing you flourish in our garden!\n\n"
                    + "Best regards,\nThe Madeleine Garden Team\n\n"
                    + "---\n\n"
                    + "Cher(e) " + newUser.getUsername() + ",\n\n"
                    + "Bienvenue à Madeleine Garden ! Nous sommes ravis de vous accueillir dans notre communauté.\n"
                    + "Votre compte a été créé avec succès, et vous êtes maintenant prêt(e) à explorer tout ce que Madeleine Garden a à offrir.\n"
                    + "Si vous avez des questions ou besoin d'aide, n'hésitez pas à contacter notre équipe de support.\n\n"
                    + "Nous avons hâte de vous voir vous épanouir dans notre jardin !\n\n"
                    + "Cordialement,\nL'équipe de Madeleine Garden\n\n"
                    + "---\n\n"
                    + "Nshuti " + newUser.getUsername() + ",\n\n"
                    + "Murakaza neza muri Madeleine Garden! Turishimiye cyane ko wifatanyije n'umuryango wacu.\n"
                    + "Konti yawe yashyizweho neza, kandi ubu witeguye gutangira gukoresha serivisi zose Madeleine Garden itanga.\n"
                    + "Niba ufite ibibazo cyangwa ukeneye ubufasha, ntutinye guhita ubaza ikipe yacu ishinzwe gufasha abakiliya.\n\n"
                    + "Turiteguye kukubona weza muri iri busitani ryacu!\n\n"
                    + "Tubifurije ibyiza,\nIkipe ya Madeleine Garden";

            // Send the multilingual email
            emailService.sendWelcomeEmail(newUser.getEmail(), subject, body);

            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully and welcome email sent");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation error: " + e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    // Read all (including encoded passwords)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Read one
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            return ResponseEntity.ok(userRepository.save(user));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Login (géré par Spring Security)
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestParam String username, @RequestParam String password) {
        // Cette partie sera gérée par Spring Security
        return ResponseEntity.ok("Connexion réussie");
    }




    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        try {
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String resetToken = generateResetToken();
                user.setResetToken(resetToken);
                user.setResetTokenExpiration(LocalDateTime.now().plusHours(1)); // Token valid for 1 hour
                userRepository.save(user);

                // Send email with reset token
                String subject = "Password Reset Request";
                String body = "Dear " + user.getUsername() + ",\n\n"
                        + "You have requested to reset your password. Please use the following code to reset your password:\n\n"
                        + resetToken + "\n\n"
                        + "This code will expire in 1 hour.\n\n"
                        + "If you did not request a password reset, please ignore this email.\n\n"
                        + "Best regards,\nThe MadeleineGarden Team";

                emailService.sendEmail(email, subject, body);
                return ResponseEntity.ok("Reset code sent to your email.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found with this email.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing your request.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String resetToken = request.get("resetToken");
        String newPassword = request.get("newPassword");

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getResetToken() != null &&
                    user.getResetToken().equals(resetToken) &&
                    user.getResetTokenExpiration().isAfter(LocalDateTime.now())) {

                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetToken(null);
                user.setResetTokenExpiration(null);
                userRepository.save(user);

                return ResponseEntity.ok("Password reset successful.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired reset token.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found with this email.");
        }
    }

    private String generateResetToken() {
        // Generate a random 6-digit code
        return String.format("%06d", new Random().nextInt(999999));
    }



}