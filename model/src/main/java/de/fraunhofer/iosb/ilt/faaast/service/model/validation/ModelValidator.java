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
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Checks if a model or model element fulfills all constraints defined in the specification.
 */
public class ModelValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelValidator.class);

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
        Map<String, Identifiable> identifiers = new HashMap<>();
        Deque<String> path = new LinkedList<>();
        AssetAdministrationShellElementWalker.builder()
                .before(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Identifiable identifiable) {
                        path.addLast(identifiable.getId());
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

                    private void validateIdentifierUniqueness(String identifier, Identifiable identifiable) {
                        if (config.getIdentifierUniqueness()) {
                            if (identifiers.containsKey(identifier)) {
                                Identifiable existingIdentifiable = identifiers.get(identifier);
                                if (Objects.equals(existingIdentifiable, identifiable)) {
                                    LOGGER.debug(String.format("Duplicate identifier '%s' - the same object is present multiple times", identifier));
                                }
                                else {
                                    errors.add(String.format("Duplicate identifier '%s' - identifiers must be globally unique.", identifier));
                                }
                            }
                            else {
                                identifiers.put(identifier, identifiable);
                            }
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


                    @Override
                    public void visit(Identifiable identifiable) {
                        validateIdentifierUniqueness(identifiable.getId(), identifiable);
                    }


                    @Override
                    public void visit(Submodel submodel) {
                        validateIdShortUniqueness(submodel.getSubmodelElements());
                    }


                    @Override
                    public void visit(SubmodelElementCollection submodelElementCollection) {
                        validateIdShortUniqueness(submodelElementCollection.getValue());
                    }
                }).build().walk(obj);
        if (config.getValidateConstraints()) {
            LOGGER.info("Constraint validation currently not available - waiting for support in AAS4j library");
            //try {
            //    ValidationReport report = ShaclValidator.getInstance().validateGetReport(obj);
            //    if (!report.conforms()) {
            //        report.getEntries().forEach(x -> errors.add(x.message()));
            //    }
            //}
            //catch (IOException e) {
            //    errors.add(String.format("error executing basic validation (reason: %s)", e.getMessage()));
            //}
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
