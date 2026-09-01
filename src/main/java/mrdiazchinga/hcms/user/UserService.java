package mrdiazchinga.hcms.user;

import mrdiazchinga.hcms.user.dto.CreateUserDto;
import mrdiazchinga.hcms.user.dto.UpdateUserDto;
import mrdiazchinga.hcms.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new UserAlreadyExistsException(dto.email());
        }

        User user = User.builder()
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .role(dto.role())
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    public Page<User> getUsers(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User updateUser(Long id, UpdateUserDto dto) {
        User user = getUser(id);

        if (dto.email() != null && !dto.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(dto.email())) {
                throw new UserAlreadyExistsException(dto.email());
            }
            user.setEmail(dto.email());
        }
        if (dto.password() != null) {
            user.setPasswordHash(passwordEncoder.encode(dto.password()));
        }
        if (dto.role() != null) {
            user.setRole(dto.role());
        }
        if (dto.enabled() != null) {
            user.setEnabled(dto.enabled());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.delete(getUser(id));
    }
}
