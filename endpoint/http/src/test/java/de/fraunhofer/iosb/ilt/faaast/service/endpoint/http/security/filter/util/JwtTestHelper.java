package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.util;

import java.util.Map;


public class JwtTestHelper {

    public static JWTMock JOHN_DOE = new JWTMock("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
            + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0"
            + ".KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30",
            Map.of("sub", "1234567890",
                    "name", "John Doe",
                    "admin", "true",
                    "iat", "02:30:22"));


    public record JWTMock(String jwtString, Map<String, String> claims) {

        public String getJwt() {
            return jwtString;
        }


        public String get(String claim) {
            return claims.get(claim);
        }

    }
}
