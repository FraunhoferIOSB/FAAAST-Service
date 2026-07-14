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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.postgres;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.BasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.Capability;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Direction;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.EntityType;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ModellingKind;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.StateOfEvent;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Integer codes for AAS model enums as stored in the database. The codes follow the declaration order of the enums in
 * the AAS metamodel (V3.2). They are persisted, so existing codes must never be renumbered; new enum literals must be
 * appended with new codes.
 */
final class EnumCodes {

    /** {@code ModelType} code of AssetAdministrationShell. */
    static final int MODEL_TYPE_AAS = 3;
    /** {@code ModelType} code of AssetInformation. */
    static final int MODEL_TYPE_ASSET_INFORMATION = 4;
    /** {@code ModelType} code of Submodel. */
    static final int MODEL_TYPE_SUBMODEL = 7;

    static final int MODEL_TYPE_RELATIONSHIP_ELEMENT = 8;
    static final int MODEL_TYPE_SUBMODEL_ELEMENT_LIST = 9;
    static final int MODEL_TYPE_SUBMODEL_ELEMENT_COLLECTION = 10;
    static final int MODEL_TYPE_PROPERTY = 11;
    static final int MODEL_TYPE_MULTI_LANGUAGE_PROPERTY = 12;
    static final int MODEL_TYPE_RANGE = 13;
    static final int MODEL_TYPE_REFERENCE_ELEMENT = 14;
    static final int MODEL_TYPE_BLOB = 15;
    static final int MODEL_TYPE_FILE = 16;
    static final int MODEL_TYPE_ANNOTATED_RELATIONSHIP_ELEMENT = 17;
    static final int MODEL_TYPE_ENTITY = 18;
    static final int MODEL_TYPE_BASIC_EVENT_ELEMENT = 20;
    static final int MODEL_TYPE_OPERATION = 21;
    static final int MODEL_TYPE_CAPABILITY = 23;

    /** Code used for absent values in NOT NULL integer columns. */
    static final int NO_VALUE = -1;

    private static final Map<KeyTypes, Integer> KEY_TYPES = new EnumMap<>(KeyTypes.class);
    private static final Map<Integer, KeyTypes> KEY_TYPES_REVERSE = new HashMap<>();
    private static final Map<DataTypeDefXsd, Integer> DATA_TYPES = new EnumMap<>(DataTypeDefXsd.class);
    private static final Map<Integer, DataTypeDefXsd> DATA_TYPES_REVERSE = new HashMap<>();
    private static final Map<AasSubmodelElements, Integer> AAS_SUBMODEL_ELEMENTS = new EnumMap<>(AasSubmodelElements.class);
    private static final Map<Integer, AasSubmodelElements> AAS_SUBMODEL_ELEMENTS_REVERSE = new HashMap<>();

    static {
        // aas-core KeyTypes declaration order
        KeyTypes[] keyTypesOrder = {
                KeyTypes.ANNOTATED_RELATIONSHIP_ELEMENT,
                KeyTypes.ASSET_ADMINISTRATION_SHELL,
                KeyTypes.BASIC_EVENT_ELEMENT,
                KeyTypes.BLOB,
                KeyTypes.CAPABILITY,
                KeyTypes.CONCEPT_DESCRIPTION,
                KeyTypes.DATA_ELEMENT,
                KeyTypes.ENTITY,
                KeyTypes.EVENT_ELEMENT,
                KeyTypes.FILE,
                KeyTypes.FRAGMENT_REFERENCE,
                KeyTypes.GLOBAL_REFERENCE,
                KeyTypes.IDENTIFIABLE,
                KeyTypes.MULTI_LANGUAGE_PROPERTY,
                KeyTypes.OPERATION,
                KeyTypes.PROPERTY,
                KeyTypes.RANGE,
                KeyTypes.REFERABLE,
                KeyTypes.REFERENCE_ELEMENT,
                KeyTypes.RELATIONSHIP_ELEMENT,
                KeyTypes.SUBMODEL,
                KeyTypes.SUBMODEL_ELEMENT,
                KeyTypes.SUBMODEL_ELEMENT_COLLECTION,
                KeyTypes.SUBMODEL_ELEMENT_LIST
        };
        for (int i = 0; i < keyTypesOrder.length; i++) {
            KEY_TYPES.put(keyTypesOrder[i], i);
            KEY_TYPES_REVERSE.put(i, keyTypesOrder[i]);
        }

        // aas-core DataTypeDefXSD declaration order
        DataTypeDefXsd[] dataTypesOrder = {
                DataTypeDefXsd.ANY_URI,
                DataTypeDefXsd.BASE64BINARY,
                DataTypeDefXsd.BOOLEAN,
                DataTypeDefXsd.BYTE,
                DataTypeDefXsd.DATE,
                DataTypeDefXsd.DATE_TIME,
                DataTypeDefXsd.DECIMAL,
                DataTypeDefXsd.DOUBLE,
                DataTypeDefXsd.DURATION,
                DataTypeDefXsd.FLOAT,
                DataTypeDefXsd.GDAY,
                DataTypeDefXsd.GMONTH,
                DataTypeDefXsd.GMONTH_DAY,
                DataTypeDefXsd.GYEAR,
                DataTypeDefXsd.GYEAR_MONTH,
                DataTypeDefXsd.HEX_BINARY,
                DataTypeDefXsd.INT,
                DataTypeDefXsd.INTEGER,
                DataTypeDefXsd.LONG,
                DataTypeDefXsd.NEGATIVE_INTEGER,
                DataTypeDefXsd.NON_NEGATIVE_INTEGER,
                DataTypeDefXsd.NON_POSITIVE_INTEGER,
                DataTypeDefXsd.POSITIVE_INTEGER,
                DataTypeDefXsd.SHORT,
                DataTypeDefXsd.STRING,
                DataTypeDefXsd.TIME,
                DataTypeDefXsd.UNSIGNED_BYTE,
                DataTypeDefXsd.UNSIGNED_INT,
                DataTypeDefXsd.UNSIGNED_LONG,
                DataTypeDefXsd.UNSIGNED_SHORT
        };
        for (int i = 0; i < dataTypesOrder.length; i++) {
            DATA_TYPES.put(dataTypesOrder[i], i);
            DATA_TYPES_REVERSE.put(i, dataTypesOrder[i]);
        }

        // aas-core AASSubmodelElements declaration order (note: List before Collection)
        AasSubmodelElements[] aasSubmodelElementsOrder = {
                AasSubmodelElements.ANNOTATED_RELATIONSHIP_ELEMENT,
                AasSubmodelElements.BASIC_EVENT_ELEMENT,
                AasSubmodelElements.BLOB,
                AasSubmodelElements.CAPABILITY,
                AasSubmodelElements.DATA_ELEMENT,
                AasSubmodelElements.ENTITY,
                AasSubmodelElements.EVENT_ELEMENT,
                AasSubmodelElements.FILE,
                AasSubmodelElements.MULTI_LANGUAGE_PROPERTY,
                AasSubmodelElements.OPERATION,
                AasSubmodelElements.PROPERTY,
                AasSubmodelElements.RANGE,
                AasSubmodelElements.REFERENCE_ELEMENT,
                AasSubmodelElements.RELATIONSHIP_ELEMENT,
                AasSubmodelElements.SUBMODEL_ELEMENT,
                AasSubmodelElements.SUBMODEL_ELEMENT_LIST,
                AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION
        };
        for (int i = 0; i < aasSubmodelElementsOrder.length; i++) {
            AAS_SUBMODEL_ELEMENTS.put(aasSubmodelElementsOrder[i], i);
            AAS_SUBMODEL_ELEMENTS_REVERSE.put(i, aasSubmodelElementsOrder[i]);
        }
    }

    private EnumCodes() {}


    static int modelType(SubmodelElement element) {
        // AnnotatedRelationshipElement extends RelationshipElement - check the subtype first
        if (element instanceof AnnotatedRelationshipElement) {
            return MODEL_TYPE_ANNOTATED_RELATIONSHIP_ELEMENT;
        }
        if (element instanceof RelationshipElement) {
            return MODEL_TYPE_RELATIONSHIP_ELEMENT;
        }
        if (element instanceof SubmodelElementList) {
            return MODEL_TYPE_SUBMODEL_ELEMENT_LIST;
        }
        if (element instanceof SubmodelElementCollection) {
            return MODEL_TYPE_SUBMODEL_ELEMENT_COLLECTION;
        }
        if (element instanceof Property) {
            return MODEL_TYPE_PROPERTY;
        }
        if (element instanceof MultiLanguageProperty) {
            return MODEL_TYPE_MULTI_LANGUAGE_PROPERTY;
        }
        if (element instanceof Range) {
            return MODEL_TYPE_RANGE;
        }
        if (element instanceof ReferenceElement) {
            return MODEL_TYPE_REFERENCE_ELEMENT;
        }
        if (element instanceof Blob) {
            return MODEL_TYPE_BLOB;
        }
        if (element instanceof File) {
            return MODEL_TYPE_FILE;
        }
        if (element instanceof Entity) {
            return MODEL_TYPE_ENTITY;
        }
        if (element instanceof BasicEventElement) {
            return MODEL_TYPE_BASIC_EVENT_ELEMENT;
        }
        if (element instanceof Operation) {
            return MODEL_TYPE_OPERATION;
        }
        if (element instanceof Capability) {
            return MODEL_TYPE_CAPABILITY;
        }
        throw new IllegalArgumentException("Unsupported submodel element type: " + element.getClass().getName());
    }


    static int of(KeyTypes value) {
        return value != null ? KEY_TYPES.get(value) : NO_VALUE;
    }


    static KeyTypes keyTypes(int code) {
        return KEY_TYPES_REVERSE.get(code);
    }


    static int of(DataTypeDefXsd value) {
        return value != null ? DATA_TYPES.get(value) : NO_VALUE;
    }


    static DataTypeDefXsd dataTypeDefXsd(Integer code) {
        return code != null ? DATA_TYPES_REVERSE.get(code) : null;
    }


    static int of(AasSubmodelElements value) {
        return value != null ? AAS_SUBMODEL_ELEMENTS.get(value) : NO_VALUE;
    }


    static AasSubmodelElements aasSubmodelElements(Integer code) {
        return code != null ? AAS_SUBMODEL_ELEMENTS_REVERSE.get(code) : null;
    }


    static int of(ReferenceTypes value) {
        if (value == null) {
            return NO_VALUE;
        }
        return switch (value) {
            case EXTERNAL_REFERENCE -> 0;
            case MODEL_REFERENCE -> 1;
        };
    }


    static ReferenceTypes referenceTypes(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 0 -> ReferenceTypes.EXTERNAL_REFERENCE;
            case 1 -> ReferenceTypes.MODEL_REFERENCE;
            default -> null;
        };
    }


    static int of(AssetKind value) {
        if (value == null) {
            return NO_VALUE;
        }
        // aas-core (metamodel 3.1): Type=0, Instance=1, Batch=2, Role=3, NotApplicable=4
        return switch (value) {
            case TYPE -> 0;
            case INSTANCE -> 1;
            case NOT_APPLICABLE -> 4;
        };
    }


    static AssetKind assetKind(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 0 -> AssetKind.TYPE;
            case 1 -> AssetKind.INSTANCE;
            case 4 -> AssetKind.NOT_APPLICABLE;
            default -> null;
        };
    }


    static int of(ModellingKind value) {
        if (value == null) {
            return NO_VALUE;
        }
        return switch (value) {
            case TEMPLATE -> 0;
            case INSTANCE -> 1;
        };
    }


    static ModellingKind modellingKind(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 0 -> ModellingKind.TEMPLATE;
            case 1 -> ModellingKind.INSTANCE;
            default -> null;
        };
    }


    static int of(EntityType value) {
        if (value == null) {
            return NO_VALUE;
        }
        return switch (value) {
            case CO_MANAGED_ENTITY -> 0;
            case SELF_MANAGED_ENTITY -> 1;
        };
    }


    static EntityType entityType(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 0 -> EntityType.CO_MANAGED_ENTITY;
            case 1 -> EntityType.SELF_MANAGED_ENTITY;
            default -> null;
        };
    }


    static int of(Direction value) {
        if (value == null) {
            return NO_VALUE;
        }
        return switch (value) {
            case INPUT -> 0;
            case OUTPUT -> 1;
        };
    }


    static Direction direction(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 0 -> Direction.INPUT;
            case 1 -> Direction.OUTPUT;
            default -> null;
        };
    }


    static int of(StateOfEvent value) {
        if (value == null) {
            return NO_VALUE;
        }
        return switch (value) {
            case ON -> 0;
            case OFF -> 1;
        };
    }


    static StateOfEvent stateOfEvent(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 0 -> StateOfEvent.ON;
            case 1 -> StateOfEvent.OFF;
            default -> null;
        };
    }
}
