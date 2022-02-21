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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.managers;

import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.Util;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Class to handle referable elements
 */
public class ReferablePersistenceManager extends PersistenceManager {

    public SubmodelElement getSubmodelElement(Reference reference, QueryModifier modifier) throws ResourceNotFoundException {
        if (reference == null || reference.getKeys() == null || modifier == null || this.aasEnvironment == null) {
            return null;
        }
        if (modifier.getExtend() == Extend.WithoutBLOBValue) {
            if (reference.getKeys().get(reference.getKeys().size() - 1).getType() == KeyElements.BLOB) {
                return null;
            }
        }

        try {
            SubmodelElement submodelElement = AasUtils.resolve(reference, this.aasEnvironment, SubmodelElement.class);
            return Util.deepCopy(submodelElement, submodelElement.getClass());
        }
        catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException(reference);
        }
    }


    public List<SubmodelElement> getSubmodelElements(Reference reference, Reference semanticId) {
        if (reference == null) {
            return null;
        }

        if (reference.getKeys() != null
                && reference.getKeys().size() > 0) {
            List<SubmodelElement> submodelElements = null;
            KeyElements lastKeyElementOfReference = reference.getKeys().get(reference.getKeys().size() - 1).getType();

            if (lastKeyElementOfReference == KeyElements.SUBMODEL) {
                Submodel submodel = AasUtils.resolve(reference, this.aasEnvironment, Submodel.class);
                if (submodel == null) {
                    return null;
                }
                Submodel deepCopiedSubmodel = Util.deepCopy(submodel, submodel.getClass());
                submodelElements = deepCopiedSubmodel.getSubmodelElements();

            }
            else if (lastKeyElementOfReference == KeyElements.SUBMODEL_ELEMENT_COLLECTION) {
                SubmodelElementCollection submodelElementCollection = AasUtils.resolve(reference, this.aasEnvironment, SubmodelElementCollection.class);
                if (submodelElementCollection == null) {
                    return null;
                }
                SubmodelElementCollection deepCopiedSubmodelElementCollection = Util.deepCopy(submodelElementCollection, submodelElementCollection.getClass());
                submodelElements = new ArrayList<>(deepCopiedSubmodelElementCollection.getValues());
            }

            if (semanticId != null) {
                assert submodelElements != null;
                submodelElements = submodelElements.stream().filter(x -> x.getSemanticId() != null
                        && x.getSemanticId().equals(semanticId)).collect(Collectors.toList());
            }

            return submodelElements;
        }
        return null;
    }


    public SubmodelElement putSubmodelElement(Reference parent, Reference referenceToSubmodelElement, SubmodelElement submodelElement) {
        if ((parent == null && referenceToSubmodelElement == null) || submodelElement == null) {
            return null;
        }
        KeyElements lastKeyElementOfParent;

        if (parent != null
                && parent.getKeys() != null
                && parent.getKeys().size() > 0) {
            lastKeyElementOfParent = parent.getKeys().get(parent.getKeys().size() - 1).getType();
        }
        else if (referenceToSubmodelElement != null
                && referenceToSubmodelElement.getKeys() != null
                && referenceToSubmodelElement.getKeys().size() > 1) {
            lastKeyElementOfParent = referenceToSubmodelElement.getKeys().get(referenceToSubmodelElement.getKeys().size() - 2).getType();
            parent = new DefaultReference.Builder()
                    .keys(referenceToSubmodelElement.getKeys().subList(0, referenceToSubmodelElement.getKeys().size() - 1))
                    .build();
        }
        else {
            return null;
        }

        Predicate<SubmodelElement> filter = x -> x.getIdShort().equalsIgnoreCase(submodelElement.getIdShort());

        if (lastKeyElementOfParent == KeyElements.SUBMODEL) {
            Submodel submodel = AasUtils.resolve(parent, this.aasEnvironment, Submodel.class);
            if (submodel == null) {
                return null;
            }
            submodel.getSubmodelElements().removeIf(filter);
            submodel.getSubmodelElements().add(submodelElement);

        }
        else if (lastKeyElementOfParent == KeyElements.SUBMODEL_ELEMENT_COLLECTION) {
            SubmodelElementCollection submodelElementCollection = AasUtils.resolve(parent, this.aasEnvironment, SubmodelElementCollection.class);
            if (submodelElementCollection == null) {
                return null;
            }
            submodelElementCollection.getValues().removeIf(filter);
            submodelElementCollection.getValues().add(submodelElement);
        }
        else {
            return null;
        }
        return submodelElement;
    }


    public void remove(Reference reference) {
        if (reference == null) {
            return;
        }
        if (reference.getKeys() != null
                && reference.getKeys().size() > 0) {
            KeyElements lastKeyElementOfReference = reference.getKeys().get(reference.getKeys().size() - 1).getType();
            Class clazz = AasUtils.keyTypeToClass(lastKeyElementOfReference);

            if (Identifiable.class.isAssignableFrom(clazz)) {
                //TODO: Build Identifier and forward remove reuquest to remove(Identifier id)
                return;
            }
            if (SubmodelElement.class.isAssignableFrom(clazz)) {
                clazz = SubmodelElement.class;
            }

            Referable referable = AasUtils.resolve(reference, this.aasEnvironment);

            if (reference.getKeys().size() > 1) {
                Reference parent = new DefaultReference.Builder()
                        .keys(reference.getKeys().subList(0, reference.getKeys().size() - 1))
                        .build();
                Referable parentReferable = AasUtils.resolve(parent, this.aasEnvironment);

                Method method = Util.getGetListMethod(clazz, parentReferable);
                if (method != null) {
                    try {
                        List<Referable> referableList = (List<Referable>) method.invoke(parentReferable);
                        referableList.remove(referable);
                    }
                    catch (InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
