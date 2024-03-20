package smart_system.banking_academy.request_for_financing.controllers;

import smart_system.banking_academy.request_for_financing.security.jwt.JwtUtils;
import smart_system.banking_academy.request_for_financing.models.ERole;
import smart_system.banking_academy.request_for_financing.models.Role;
import smart_system.banking_academy.request_for_financing.models.User;
import smart_system.banking_academy.request_for_financing.payload.request.LoginRequest;
import smart_system.banking_academy.request_for_financing.payload.response.JwtResponse;
import smart_system.banking_academy.request_for_financing.payload.response.MessageResponse;
import smart_system.banking_academy.request_for_financing.repository.RoleRepository;
import smart_system.banking_academy.request_for_financing.repository.UserRepository;
import smart_system.banking_academy.request_for_financing.security.jwt.JwtUtils;
import smart_system.banking_academy.request_for_financing.security.services.UserDetailsImpl;
import smart_system.banking_academy.request_for_financing.security.services.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  private UserDetailsServiceImpl userService;
  @Autowired
  JwtUtils jwtUtils;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseEntity.ok(new JwtResponse(jwt, 
                         userDetails.getId(), 
                         userDetails.getEmail(),
                         userDetails.getEmail(), 
                         roles));
  }


  @PostMapping(value = "/signup", consumes = { "multipart/form-data" })
  public ResponseEntity<?> registerUser(@RequestParam("username") String username,
                                        @RequestParam("email") String email,
                                        @RequestParam("password") String password,
                                        @RequestParam(value = "role", required = false) Set<String> strRoles,
                                        @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {


    if (userRepository.existsByUsername(username)) {
      return ResponseEntity
              .badRequest()
              .body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(email)) {
      return ResponseEntity
              .badRequest()
              .body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(username, email, encoder.encode(password));
    user.setApproved(false);
    Set<Role> roles = new HashSet<>();

    if (strRoles == null || strRoles.isEmpty()) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
          case "admin":
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(adminRole);
            break;
          case "mod":
            Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(modRole);
            break;
          default:
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        }
      });
    }

    if (image != null && !image.isEmpty()) {
      byte[] compressedImage = compressBytes(image.getBytes());
      user.setPicByte(compressedImage);
    }

    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  public static byte[] compressBytes(byte[] data) {
    Deflater deflater = new Deflater();
    deflater.setInput(data);
    deflater.finish();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
    byte[] buffer = new byte[1024];
    while (!deflater.finished()) {
      int count = deflater.deflate(buffer);
      outputStream.write(buffer, 0, count);
    }
    try {
      outputStream.close();
    } catch (IOException e) {
    }
    System.out.println("Compressed Image Byte Size - " + outputStream.toByteArray().length);

    return outputStream.toByteArray();
  }

  public static byte[] decompressBytes(byte[] data) {
    Inflater inflater = new Inflater();
    inflater.setInput(data);

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
         InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(data), inflater)) {

      byte[] buffer = new byte[1024];
      int count;
      while ((count = inflaterInputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, count);
      }
      return outputStream.toByteArray();
    } catch (IOException e) {
      // handle exception
      e.printStackTrace();
      return null;
    } finally {
      inflater.end();
    }
  }
  @GetMapping("/getallusers")
  public ResponseEntity<List<User>> getAllUsers() {
    List<User> users = userService.getAllUsers();

    // Decompressing the image data for each user
    users.forEach(user -> {
      if (user.getPicByte() != null) {
        byte[] decompressedImage = decompressBytes(user.getPicByte());
        user.setPicByte(decompressedImage); // Assuming User class has a field for decompressed image
      }
    });

    return ResponseEntity.ok(users);
  }



}