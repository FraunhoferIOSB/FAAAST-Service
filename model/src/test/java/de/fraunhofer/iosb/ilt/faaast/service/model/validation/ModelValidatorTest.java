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
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.junit.Test;


public class ModelValidatorTest {

    private static final Property PROPRERTY_1 = new DefaultProperty.Builder()
            .idShort("Property1")
            .valueType(DataTypeDefXsd.STRING)
            .build();
    private static final Property PROPRERTY_2 = new DefaultProperty.Builder()
            .idShort("Property3")
            .valueType(DataTypeDefXsd.STRING)
            .build();
    private static final String ID_1 = "Identifier1";
    private static final String ID_2 = "Identifier2";

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
                .submodelElements(PROPRERTY_1)
                .submodelElements(PROPRERTY_1)
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
                .submodelElements(PROPRERTY_1)
                .submodelElements(PROPRERTY_1)
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
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
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
                .id(ID_1)
                .build();
        Environment input = new DefaultEnvironment.Builder()
                .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                        .id(ID_2)
                        .submodels(AasUtils.toReference(submodel))
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
                .id(ID_1)
                .build();
        Environment input = new DefaultEnvironment.Builder()
                .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                        .id(ID_1)
                        .submodels(AasUtils.toReference(submodel))
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
    public void testIdentifierNotUniqueObjectCopy() throws ValidationException {
        ConceptDescription conceptDescription = new DefaultConceptDescription.Builder()
                .id(ID_1)
                .build();
        Environment input = new DefaultEnvironment.Builder()
                .conceptDescriptions(conceptDescription)
                .conceptDescriptions(conceptDescription)
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
                .id(ID_1)
                .build();
        Environment input = new DefaultEnvironment.Builder()
                .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                        .id(ID_1)
                        .submodels(AasUtils.toReference(submodel))
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
}
