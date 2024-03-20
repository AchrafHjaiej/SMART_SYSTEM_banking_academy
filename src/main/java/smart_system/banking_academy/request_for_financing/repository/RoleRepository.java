package smart_system.banking_academy.request_for_financing.repository;

import smart_system.banking_academy.request_for_financing.models.ERole;
import smart_system.banking_academy.request_for_financing.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}
