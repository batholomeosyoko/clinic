package mrdiazchinga.hcms.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mrdiazchinga.hcms.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
