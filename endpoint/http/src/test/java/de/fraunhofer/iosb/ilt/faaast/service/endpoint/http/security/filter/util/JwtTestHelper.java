/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
