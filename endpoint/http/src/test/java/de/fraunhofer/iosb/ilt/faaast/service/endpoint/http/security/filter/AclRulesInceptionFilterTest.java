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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.AbstractAclRepository;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.Test;


public class AclRulesInceptionFilterTest {

    @Test
    public void testAclRulesIncepted() {
        List<AccessPermissionRule> rules = List.of(new AccessPermissionRule(), new AccessPermissionRule());

        AbstractAclRepository mockRepo = mock(AbstractAclRepository.class);
        when(mockRepo.getAccessPermissionRules()).thenReturn(rules);
        assertEquals(rules, new AclRulesInceptionFilter(mockRepo).doFilter(mock(HttpServletRequest.class), null));
    }


    @Test
    public void testAclExistingRulesThrowsException() {
        List<AccessPermissionRule> rules = List.of(new AccessPermissionRule(), new AccessPermissionRule());

        AbstractAclRepository mockRepo = mock(AbstractAclRepository.class);
        assertThrows(IllegalStateException.class, () -> new AclRulesInceptionFilter(mockRepo).doFilter(mock(HttpServletRequest.class), rules));
    }
}
