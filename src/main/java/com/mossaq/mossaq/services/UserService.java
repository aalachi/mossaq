package com.mossaq.mossaq.services;

import com.mossaq.mossaq.model.User;
import com.mossaq.mossaq.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

    public void registerUser(String email, String password, String username) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Password strength validation
        if (password.length() < 8 ||
                !password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-z].*") ||
                !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Weak password");
        }

        User user = new User();
        user.setUuid(java.util.UUID.randomUUID());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(username);
        user.setRole("USER");
        userRepository.save(user);
    }

    public void updateUser(String currentEmail, String newUsername, String newEmail, String newPassword, String newBio, boolean emailNotifications, MultipartFile avatar) throws IOException {
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getEmail().equals(newEmail) && userRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!user.getUsername().equals(newUsername) && userRepository.findByUsername(newUsername).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        user.setUsername(newUsername);
        user.setEmail(newEmail);
        user.setBio(newBio);
        user.setEmailNotifications(emailNotifications);

        if (avatar != null && !avatar.isEmpty()) {
            // Delete old avatar if exists
            if (user.getAvatarFilename() != null) {
                Files.deleteIfExists(this.fileStorageLocation.resolve(user.getAvatarFilename()));
            }
            // Save new avatar
            String fileName = System.currentTimeMillis() + "_avatar_" + avatar.getOriginalFilename();
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(avatar.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            user.setAvatarFilename(fileName);
        }

        if (newPassword != null && !newPassword.isEmpty()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(user);
    }

    public void deleteAvatar(String email) throws IOException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getAvatarFilename() != null) {
            Files.deleteIfExists(this.fileStorageLocation.resolve(user.getAvatarFilename()));
            user.setAvatarFilename(null);
            userRepository.save(user);
        }
    }

    public void deleteUser(String email) throws IOException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getAvatarFilename() != null) {
            Files.deleteIfExists(this.fileStorageLocation.resolve(user.getAvatarFilename()));
        }
        userRepository.delete(user);
    }
}