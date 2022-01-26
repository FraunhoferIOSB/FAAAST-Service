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
package opc.i4aas;

import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.UaNodeId;
import com.prosysopc.ua.stack.builtintypes.Enumeration;
import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.typedictionary.EnumerationSpecification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Generated on 2022-01-26 16:50:24
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3012")
public enum AASKeyElementsDataType implements Enumeration {
    AccessPermissionRule(0),

    AnnotatedRelationshipElement(1),

    Asset(2),

    AssetAdministrationShell(3),

    Blob(4),

    Capability(5),

    ConceptDescription(6),

    ConceptDictionary(7),

    DataElement(8),

    Entity(9),

    Event(10),

    File(11),

    FragmentReference(12),

    GlobalReference(13),

    MultiLanguageProperty(14),

    Operation(15),

    Property(16),

    Range(17),

    ReferenceElement(18),

    RelationshipElement(19),

    Submodel(20),

    SubmodelElement(21),

    SubmodelElementCollection(22),

    View(23);

    public static final EnumerationSpecification SPECIFICATION;

    public static final EnumSet<AASKeyElementsDataType> NONE = EnumSet.noneOf(AASKeyElementsDataType.class);

    public static final EnumSet<AASKeyElementsDataType> ALL = EnumSet.allOf(AASKeyElementsDataType.class);

    private static final Map<Integer, AASKeyElementsDataType> map;

    static {
        EnumerationSpecification.Builder b = EnumerationSpecification.builder();
        b.setName("AASKeyElementsDataType");
        b.setJavaClass(AASKeyElementsDataType.class);
        b.setTypeId(UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3012")));
        b.addMapping(0, "AccessPermissionRule");
        b.addMapping(1, "AnnotatedRelationshipElement");
        b.addMapping(2, "Asset");
        b.addMapping(3, "AssetAdministrationShell");
        b.addMapping(4, "Blob");
        b.addMapping(5, "Capability");
        b.addMapping(6, "ConceptDescription");
        b.addMapping(7, "ConceptDictionary");
        b.addMapping(8, "DataElement");
        b.addMapping(9, "Entity");
        b.addMapping(10, "Event");
        b.addMapping(11, "File");
        b.addMapping(12, "FragmentReference");
        b.addMapping(13, "GlobalReference");
        b.addMapping(14, "MultiLanguageProperty");
        b.addMapping(15, "Operation");
        b.addMapping(16, "Property");
        b.addMapping(17, "Range");
        b.addMapping(18, "ReferenceElement");
        b.addMapping(19, "RelationshipElement");
        b.addMapping(20, "Submodel");
        b.addMapping(21, "SubmodelElement");
        b.addMapping(22, "SubmodelElementCollection");
        b.addMapping(23, "View");
        b.setBuilderSupplier(new EnumerationSpecification.EnumerationBuilderSupplier() {
            @Override
            public Enumeration.Builder get() {
                return AASKeyElementsDataType.builder();
            }
        });
        SPECIFICATION = b.build();
    }
    static {
        map = new HashMap<Integer, AASKeyElementsDataType>();
        for (AASKeyElementsDataType i: AASKeyElementsDataType.values()) {
            map.put(i.value, i);
        }
    }

    private final int value;

    AASKeyElementsDataType(int value) {
        this.value = value;
    }


    @Override
    public EnumerationSpecification specification() {
        return SPECIFICATION;
    }


    public static AASKeyElementsDataType valueOf(int value) {
        return map.get(value);
    }


    public static AASKeyElementsDataType valueOf(Integer value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASKeyElementsDataType valueOf(UnsignedInteger value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASKeyElementsDataType[] valueOf(int[] value) {
        AASKeyElementsDataType[] result = new AASKeyElementsDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASKeyElementsDataType[] valueOf(Integer[] value) {
        AASKeyElementsDataType[] result = new AASKeyElementsDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASKeyElementsDataType[] valueOf(UnsignedInteger[] value) {
        AASKeyElementsDataType[] result = new AASKeyElementsDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static UnsignedInteger getMask(AASKeyElementsDataType... list) {
        int result = 0;
        for (AASKeyElementsDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static UnsignedInteger getMask(Collection<AASKeyElementsDataType> list) {
        int result = 0;
        for (AASKeyElementsDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static EnumSet<AASKeyElementsDataType> getSet(UnsignedInteger mask) {
        return getSet(mask.intValue());
    }


    public static EnumSet<AASKeyElementsDataType> getSet(int mask) {
        List<AASKeyElementsDataType> res = new ArrayList<AASKeyElementsDataType>();
        for (AASKeyElementsDataType l: AASKeyElementsDataType.values()) {
            if ((mask & l.value) == l.value) {
                res.add(l);
            }
        }
        return EnumSet.copyOf(res);
    }


    @Override
    public int getValue() {
        return value;
    }


    public static Builder builder() {
        return new Builder();
    }


    @Override
    public Builder toBuilder() {
        Builder b = builder();
        b.setValue(this.getValue());
        return b;
    }

    public static class Builder implements Enumeration.Builder {
        private AASKeyElementsDataType value;

        private Builder() {}


        @Override
        public AASKeyElementsDataType build() {
            return value;
        }


        @Override
        public Builder setValue(int value) {
            this.value = AASKeyElementsDataType.valueOf(value);
            if (this.value == null) {
                throw new IllegalArgumentException("Unknown enum AASKeyElementsDataType int value: " + value);
            }
            return this;
        }
    }
}
