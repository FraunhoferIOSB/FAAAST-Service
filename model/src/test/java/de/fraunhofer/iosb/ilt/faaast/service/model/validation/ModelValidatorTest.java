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
package de.fraunhofer.iosb.ilt.faaast.service.model.validation;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;


/**
 *
 * @author jab
 */
public class ModelValidatorTest {

    private static final Property PROPRERTY_1 = new DefaultProperty.Builder()
            .idShort("Property1")
            .valueType("string")
            .build();
    private static final Property PROPRERTY_2 = new DefaultProperty.Builder()
            .idShort("Property3")
            .valueType("string")
            .build();
    private static final Identifier IDENTIFIER_1 = new DefaultIdentifier.Builder()
            .idType(IdentifierType.IRI)
            .identifier("Identifier1")
            .build();
    private static final Identifier IDENTIFIER_2 = new DefaultIdentifier.Builder()
            .idType(IdentifierType.IRI)
            .identifier("Identifier2")
            .build();

    @Test(expected = ValidationException.class)
    public void testIdShortNotUniqueCollection() throws ValidationException {
        SubmodelElementCollection input = new DefaultSubmodelElementCollection.Builder()
                .value(PROPRERTY_1)
                .value(PROPRERTY_1)
                .value(PROPRERTY_1)
                .value(PROPRERTY_2)
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .build());
    }


    @Test(expected = ValidationException.class)
    public void testIdShortNotUniqueSubmodel() throws ValidationException {
        Submodel input = new DefaultSubmodel.Builder()
                .submodelElement(PROPRERTY_1)
                .submodelElement(PROPRERTY_1)
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .build());
    }


    @Test
    public void testIdShortUniquenessDisable() throws ValidationException {
        Submodel input = new DefaultSubmodel.Builder()
                .submodelElement(PROPRERTY_1)
                .submodelElement(PROPRERTY_1)
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .validateIdShortUniqueness(false)
                        .build());
    }


    @Test(expected = ValidationException.class)
    public void testIdShortNotUniqueSubmodelNested() throws ValidationException {
        Submodel input = new DefaultSubmodel.Builder()
                .submodelElement(new DefaultSubmodelElementCollection.Builder()
                        .value(PROPRERTY_1)
                        .value(PROPRERTY_1)
                        .value(PROPRERTY_1)
                        .value(PROPRERTY_2)
                        .build())
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .build());
    }


    @Test(expected = ValidationException.class)
    public void testIdShortNotUniqueNestedCollection() throws ValidationException {
        SubmodelElementCollection input = new DefaultSubmodelElementCollection.Builder()
                .value(PROPRERTY_1)
                .value(PROPRERTY_2)
                .value(new DefaultSubmodelElementCollection.Builder()
                        .value(PROPRERTY_1)
                        .value(PROPRERTY_1)
                        .value(PROPRERTY_1)
                        .value(PROPRERTY_2)
                        .build())
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .build());
    }


    @Test
    public void testIdShortUnique() throws ValidationException {
        SubmodelElementCollection input = new DefaultSubmodelElementCollection.Builder()
                .value(PROPRERTY_1)
                .value(PROPRERTY_2)
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .build());
    }


    @Test
    public void testIdentifierUnique() throws ValidationException {
        Submodel submodel = new DefaultSubmodel.Builder()
                .identification(IDENTIFIER_1)
                .build();
        AssetAdministrationShellEnvironment input = new DefaultAssetAdministrationShellEnvironment.Builder()
                .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                        .identification(IDENTIFIER_2)
                        .submodel(AasUtils.toReference(submodel))
                        .build())
                .submodels(submodel)
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .build());
    }


    @Test(expected = ValidationException.class)
    public void testIdentifierNotUnique() throws ValidationException {
        Submodel submodel = new DefaultSubmodel.Builder()
                .identification(IDENTIFIER_1)
                .build();
        AssetAdministrationShellEnvironment input = new DefaultAssetAdministrationShellEnvironment.Builder()
                .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                        .identification(IDENTIFIER_1)
                        .submodel(AasUtils.toReference(submodel))
                        .build())
                .submodels(submodel)
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .build());
    }


    @Test
    public void testIdentifierUniquenessDisabled() throws ValidationException {
        Submodel submodel = new DefaultSubmodel.Builder()
                .identification(IDENTIFIER_1)
                .build();
        AssetAdministrationShellEnvironment input = new DefaultAssetAdministrationShellEnvironment.Builder()
                .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                        .identification(IDENTIFIER_1)
                        .submodel(AasUtils.toReference(submodel))
                        .build())
                .submodels(submodel)
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .validateIdentifierUniqueness(false)
                        .build());
    }


    @Test
    public void testValidDatatypes() throws ValidationException {
        SubmodelElementCollection input = new DefaultSubmodelElementCollection.Builder()
                .values(Stream.of(Datatype.values())
                        .map(x -> new DefaultProperty.Builder()
                                .valueType(x.getName())
                                .build())
                        .collect(Collectors.toList()))
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .validateIdShortUniqueness(false)
                        .build());
    }


    @Test(expected = ValidationException.class)
    public void testInvalidDatatype() throws ValidationException {
        SubmodelElementCollection input = new DefaultSubmodelElementCollection.Builder()
                .value(new DefaultProperty.Builder()
                        .valueType("foo")
                        .build())
                .build();
        ModelValidator.validate(
                input,
                ModelValidatorConfig.builder()
                        .validateConstraints(false)
                        .validateIdShortUniqueness(false)
                        .build());
    }
}
