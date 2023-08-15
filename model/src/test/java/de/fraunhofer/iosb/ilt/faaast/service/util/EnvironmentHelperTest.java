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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author jab
 */
public class EnvironmentHelperTest {

    @Test
    public void testResolveWithAASFull() {
        Environment environment = AASFull.createEnvironment();
        AssetAdministrationShellElementWalker.builder()
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Referable referable) {
                        try {
                            assertResolve(referable, environment);
                        }
                        catch (ResourceNotFoundException e) {
                            Assert.fail();
                        }
                    }
                })
                .build()
                .walk(environment);

    }


    private void assertResolve(Referable expected, Environment environment) throws ResourceNotFoundException {
        Reference reference = EnvironmentHelper.asReference(expected, environment);
        Referable actual = EnvironmentHelper.resolve(reference, environment);
        Assert.assertEquals(expected, actual);
    }
}
