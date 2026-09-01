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

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserDto createUserDto) {
        String email = createUserDto.email();

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(createUserDto.password()))
                .role(createUserDto.role())
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    public Page<User> findUsers(int page, int size) {
        validatePageRequest(page, size);

        Pageable pageRequest = PageRequest.of(page, size);
        return userRepository.findAll(pageRequest);
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User updateUser(Long id, UpdateUserDto updateUserDto) {
        User user = getUser(id);

        updateEmail(user, updateUserDto.email());
        updatePassword(user, updateUserDto.password());

        if (updateUserDto.role() != null) {
            user.setRole(updateUserDto.role());
        }
        if (updateUserDto.enabled() != null) {
            user.setEnabled(updateUserDto.enabled());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.delete(getUser(id));
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page must be zero or greater and size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private void updateEmail(User user, String email) {
        if (email == null || email.equalsIgnoreCase(user.getEmail())) {
            return;
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        user.setEmail(email);
    }

    private void updatePassword(User user, String password) {
        if (password != null) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }
    }
}
