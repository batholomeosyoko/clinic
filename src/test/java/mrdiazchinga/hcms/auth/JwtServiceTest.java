package mrdiazchinga.hcms.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mrdiazchinga.hcms.user.entity.Role;
import mrdiazchinga.hcms.user.entity.User;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String BASE64_SECRET =
            "c3VwZXItc2VjdXJlLWRldmVsb3BtZW50LWp3dC1zZWNyZXQta2V5LTIwMjY=";

    @Test
    void generatesAValidTokenForTheUserEmail() {
        JwtService jwtService = new JwtService(BASE64_SECRET, 3600);
        User user = User.builder()
                .email("patient@example.com")
                .role(Role.PATIENT)
                .build();

        String token = jwtService.generateToken(user);

        assertTrue(jwtService.isTokenValid(token));
        assertEquals(user.getEmail(), jwtService.extractEmail(token));
    }
}
