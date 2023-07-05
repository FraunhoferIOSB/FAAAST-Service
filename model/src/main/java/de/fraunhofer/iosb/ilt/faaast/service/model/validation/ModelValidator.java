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
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.model.Extension;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Qualifier;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.validator.ShaclValidator;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.shacl.ValidationReport;


/**
 * Checks if a model or model element fulfills all constraints defined in the specification.
 */
public class ModelValidator {

    private ModelValidator() {}


    /**
     * Validates AAS model element.
     *
     * @param obj the object to validate
     * @throws ValidationException if element is invalid
     */
    public static void validate(Object obj) throws ValidationException {
        validate(obj, ModelValidatorConfig.DEFAULT);
    }


    /**
     * Validates AAS model element.
     *
     * @param obj the object to validate
     * @param config the validator config
     * @throws ValidationException if element is invalid
     */
    public static void validate(Object obj, ModelValidatorConfig config) throws ValidationException {
        List<String> errors = new ArrayList<>();
        Set<String> identifiers = new HashSet<>();
        Deque<String> path = new LinkedList<>();
        AssetAdministrationShellElementWalker.builder()
                .before(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Identifiable identifiable) {
                        path.addLast(identifiable.getIdentification().getIdentifier());
                    }


                    @Override
                    public void visit(SubmodelElement submodelElement) {
                        path.addLast(submodelElement.getIdShort());
                    }
                })
                .after(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Identifiable identifiable) {
                        path.removeLast();
                    }


                    @Override
                    public void visit(SubmodelElement submodelElement) {
                        path.removeLast();
                    }
                })
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {

                    private void validateIdentifierUniqueness(String identifier) {
                        if (config.getIdentifierUniqueness() && !identifiers.add(identifier)) {
                            errors.add(String.format(
                                    "Duplicate identifier '%s' - identifiers must be globally unique",
                                    identifier));
                        }
                    }


                    private void validateIdShortUniqueness(Collection<SubmodelElement> elements) {
                        if (!config.getIdShortUniqueness() || Objects.isNull(elements)) {
                            return;
                        }
                        Collection<String> idShorts = elements.stream()
                                .map(x -> x.getIdShort())
                                .collect(Collectors.toList());
                        idShorts.stream()
                                .filter(i -> Collections.frequency(idShorts, i) > 1)
                                .distinct()
                                .forEach(x -> errors.add(String.format(
                                        "Found duplicate idShort '%s' (parent element: %s)",
                                        x,
                                        String.join(".", path))));
                    }


                    private void validateDatatype(Object obj) {
                        if (!config.getValueTypeValidation() || Objects.isNull(obj)) {
                            return;
                        }
                        try {
                            // aggressive approach: if property/getter is there this means there is a datatype that needs to be validated
                            Object datatype = obj.getClass().getMethod("getValueType").invoke(obj, (Object[]) null);
                            if (Objects.nonNull(datatype) && !Datatype.isValid(datatype.toString())) {
                                errors.add(String.format(
                                        "Unsupported datatype '%s' found for element of type '%s' with path '%s'",
                                        datatype,
                                        ReflectionHelper.getAasInterface(obj.getClass()).getSimpleName(),
                                        String.join(".", path)));
                            }
                        }
                        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            // do nothing
                        }
                    }


                    @Override
                    public void visit(Identifiable identifiable) {
                        validateIdentifierUniqueness(identifiable.getIdentification().getIdentifier());
                    }


                    @Override
                    public void visit(Extension extension) {
                        validateDatatype(extension);
                    }


                    @Override
                    public void visit(Property property) {
                        validateDatatype(property);
                    }


                    @Override
                    public void visit(Qualifier qualifier) {
                        validateDatatype(qualifier);
                    }


                    @Override
                    public void visit(Range range) {
                        validateDatatype(range);
                    }


                    @Override
                    public void visit(Submodel submodel) {
                        validateIdShortUniqueness(submodel.getSubmodelElements());
                    }


                    @Override
                    public void visit(SubmodelElementCollection submodelElementCollection) {
                        validateIdShortUniqueness(submodelElementCollection.getValues());
                    }
                }).build().walk(obj);
        if (config.getValidateConstraints()) {
            try {
                ValidationReport report = ShaclValidator.getInstance().validateGetReport(obj);
                if (!report.conforms()) {
                    report.getEntries().forEach(x -> errors.add(x.message()));
                }
            }
            catch (IOException e) {
                errors.add(String.format("error executing basic validation (reason: %s)", e.getMessage()));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(String.format(
                    "Found %d violation(s):%s%s",
                    errors.size(),
                    System.lineSeparator(),
                    String.join(System.lineSeparator(), errors)));
        }
    }
}