package smart_system.banking_academy.request_for_financing.controllers;

import smart_system.banking_academy.request_for_financing.models.User;
import smart_system.banking_academy.request_for_financing.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
  @Autowired
  private UserDetailsServiceImpl userService;
  @GetMapping("/all")
  public String allAccess() {
    return "Public Content.";
  }

  @GetMapping("/user")
  @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
  public String userAccess() {
    return "User Content.";
  }

  @GetMapping("/mod")
  @PreAuthorize("hasRole('MODERATOR')")
  public String moderatorAccess() {
    return "Moderator Board.";
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public String adminAccess() {
    return "Admin Board.";
  }

  @PutMapping("/toggle-approval/{id}")
  @PreAuthorize("hasRole('ROLE_ADMIN')") // Correct usage with ROLE_ prefix
  public ResponseEntity<?> toggleUserApproval(@PathVariable Long id) {
    User updatedUser = userService.toggleUserApproval(id);
    return ResponseEntity.ok(updatedUser);
  }
}
