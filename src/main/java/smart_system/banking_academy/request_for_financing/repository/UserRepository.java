package smart_system.banking_academy.request_for_financing.repository;

import smart_system.banking_academy.request_for_financing.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

  Optional<User> findByEmailAndIsApproved(String username, boolean isApproved);
  @Modifying
  @Query("UPDATE User u SET u.isApproved = :isApproved WHERE u.id = :id")
  void updateUserApprovalStatus(@Param("id") Long id, @Param("isApproved") boolean isApproved);

}
