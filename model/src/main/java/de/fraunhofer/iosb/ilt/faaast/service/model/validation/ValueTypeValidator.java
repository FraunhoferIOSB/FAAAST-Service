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
import io.adminshell.aas.v3.model.Extension;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Qualifier;
import io.adminshell.aas.v3.model.Range;
import java.util.ArrayList;
import java.util.List;


/**
 * Validator to check if an AAS-model object contains valid datatype/value type
 * definitions.
 */
public class ValueTypeValidator {

    private ValueTypeValidator() {}


    /**
     * Validates datatypes used in a given AAS-model object recursively.
     *
     * @param obj the object to check the datatypes of
     * @throws ValidationException if an invalid value type definition is found
     */
    public static void validate(Object obj) throws ValidationException {
        List<String> errors = new ArrayList<>();
        AssetAdministrationShellElementWalker.builder().visitor(
                new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Extension extension) {
                        if (extension != null && !Datatype.isValid(extension.getValueType())) {
                            errors.add(
                                    String.format("Unsupported datatype '%s' found on element of type 'Extension' with name '%s'", extension.getValueType(), extension.getName()));
                        }
                    }


                    @Override
                    public void visit(Property property) {
                        if (property != null && !Datatype.isValid(property.getValueType())) {
                            errors.add(String.format("Unsupported datatype '%s' found on element of type 'Property' with idShort '%s'", property.getValueType(),
                                    property.getIdShort()));
                        }
                    }


                    @Override
                    public void visit(Qualifier qualifier) {
                        if (qualifier != null && !Datatype.isValid(qualifier.getValueType())) {
                            errors.add(String.format("Unsupported datatype '%s' found on element of type 'Qualifier'", qualifier.getValueType()));
                        }
                    }


                    @Override
                    public void visit(Range range) {
                        if (range != null && !Datatype.isValid(range.getValueType())) {
                            errors.add(String.format("Unsupported datatype '%s' found on element of type 'Range' with idShort '%s'", range.getValueType(), range.getIdShort()));
                        }
                    }
                }).build().walk(obj);

        if (!errors.isEmpty()) {
            throw new ValidationException(String.format(
                    "Found %d value type violation(s):%s%s",
                    errors.size(),
                    System.lineSeparator(),
                    String.join(System.lineSeparator(), errors)));
        }
    }

}
