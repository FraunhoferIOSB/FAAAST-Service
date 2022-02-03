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
package de.fraunhofer.iosb.ilt.faaast.service.typing;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueMapper;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.ArrayList;
import java.util.List;


public class TypeExtractor {

    public static TypeContext getTypeContext(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("obj must be non-null");
        }
        if (ElementValue.class.isAssignableFrom(obj.getClass())) {
            return fromValueType((ElementValue) obj);
        }
        TypeExtractor visitor = new TypeExtractor();
        visitor.walk(obj);
        return visitor.context;
    }


    private static TypeContext fromValueType(ElementValue value) {
        // TODO implement
        throw new UnsupportedOperationException("resolving TypeContext from value elements not supported yet");
    }

    private final TypeContext context;
    private boolean isRoot = true;
    private final List<String> path = new ArrayList<>();

    private TypeExtractor() {
        context = new TypeContext();
    }


    private void addType(Object obj) {
        if (!isRoot) {
            context.getTypeInfos().add(getTypeInfo(obj));
        }
        isRoot = false;
    }


    private TypeInfo getTypeInfo(Object obj) {
        TypeInfo.Builder result = TypeInfo.builder()
                .idShortPath(new ArrayList<>(path));
        if (obj != null && SubmodelElement.class.isAssignableFrom(obj.getClass())) {
            Datatype datatype = null;
            if (Property.class.isAssignableFrom(obj.getClass())) {
                datatype = Datatype.fromName(((Property) obj).getValueType());
            }
            else if (Range.class.isAssignableFrom(obj.getClass())) {
                datatype = Datatype.fromName(((Range) obj).getValueType());
            }
            Class<? extends ElementValue> valueType = null;
            try {
                valueType = ElementValueMapper.getValueClass((Class<? extends SubmodelElement>) obj.getClass());
            }
            catch (Exception ex) {}
            result = result
                    .datatype(datatype)
                    .valueType(valueType);
        }
        return result.build();
    }


    private void levelDown(String idShort) {
        if (!isRoot) {
            path.add(idShort);
        }
    }


    private void levelUp() {
        isRoot = false;
        if (path.size() > 0) {
            path.remove(path.size() - 1);
        }
    }


    private void walk(Object obj) {
        context.setRootInfo(getTypeInfo(obj));

        AssetAdministrationShellElementWalker.builder()
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {

                    @Override
                    public void visit(DataElement element) {
                        addType(element);
                    }


                    @Override
                    public void visit(Submodel submodel) {
                        isRoot = false;
                    }


                    @Override
                    public void visit(SubmodelElementCollection submodelElementCollection) {
                        addType(submodelElementCollection);
                    }


                    @Override
                    public void visit(ReferenceElement referenceElement) {
                        addType(referenceElement);
                    }


                    @Override
                    public void visit(RelationshipElement relationshipElement) {
                        addType(relationshipElement);
                    }


                    @Override
                    public void visit(Entity entity) {
                        addType(entity);
                    }
                })
                .before(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(SubmodelElement submodelElement) {
                        levelDown(submodelElement.getIdShort());
                    }
                })
                .after(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(SubmodelElement submodelElement) {
                        levelUp();
                    }
                })
                .build()
                .walk(obj);

    }
}
