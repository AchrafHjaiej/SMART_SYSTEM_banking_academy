package smart_system.banking_academy.request_for_financing.security.services;

import smart_system.banking_academy.request_for_financing.models.User;
import smart_system.banking_academy.request_for_financing.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  @Autowired
  UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmailAndIsApproved(email, true)
        .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));

    return UserDetailsImpl.build(user);
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }
  @Transactional
  public User toggleUserApproval(Long id) {
    // Log the user ID
    log.info("Toggling approval status for user with ID: " + id);

    User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

    boolean newApprovalStatus = !user.isApproved();
    userRepository.updateUserApprovalStatus(id, newApprovalStatus);

    // Log the new approval status
    log.info("User with ID: " + id + " is now " + (newApprovalStatus ? "approved" : "not approved"));

    user.setApproved(newApprovalStatus);
    return userRepository.save(user);
  }


}
