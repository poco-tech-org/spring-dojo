package com.example.blog.service.user;

import com.example.blog.repository.file.FileRepository;
import com.example.blog.repository.user.UserRepository;
import com.example.blog.security.LoggedInUser;
import com.example.blog.service.exception.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileRepository fileRepository;

    @Transactional
    public UserEntity register(String username, String rawPassword) {
        var encodedPassword = passwordEncoder.encode(rawPassword);
        var newUser = new UserEntity(null, username, encodedPassword, true);
        userRepository.insert(newUser);
        return newUser;
    }

    @Transactional
    public void delete(String username) {
        userRepository.deleteByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsUsername(String username) {
        return userRepository.selectByUsername(username).isPresent();
    }

    public ProfileImageUpload createProfileImageUploadURL(
            LoggedInUser loggedInUser,
            String fileName,
            String contentType,
            long contentLength
    ) {
        var uploadURL = fileRepository.createUploadURL(
                fileName,
                contentType,
                contentLength
        );
        var imagePath = "users/" + loggedInUser.getUserId() + "/profile-image.png";
        return new ProfileImageUpload(uploadURL, imagePath);
    }

    public UserEntity updateProfileImage(String username, @NotNull String imagePath) {
        var userToUpdate = userRepository.selectByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("User not found:" + username));
        //userToUpdate.setImagePath(); // TODO
        userRepository.update(userToUpdate);
        return userToUpdate;
    }

    public UserEntity findByUsername(String username) {
        return userRepository.selectByUsername(username)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
