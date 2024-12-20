package ReactMadeleine.Garden.repository;

import ReactMadeleine.Garden.model.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);

    Page<Admin> findByEmailContaining(String email, Pageable pageable);

    Page<Admin> findByFirstNameContainingOrLastNameContaining(
            String firstName, String lastName, Pageable pageable
    );

    Page<Admin> findByEmailContainingAndFirstNameContainingOrLastNameContaining(
            String email, String firstName, String lastName, Pageable pageable
    );
}