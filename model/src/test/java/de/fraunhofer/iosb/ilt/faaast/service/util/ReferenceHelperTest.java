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

import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.junit.Assert;
import org.junit.Test;


public class ReferenceHelperTest {

    private static final List<ReferenceSerializationInfo> TEST_CASES = List.of(
            new ReferenceSerializationInfo(
                    new DefaultReference.Builder()
                            .type(ReferenceTypes.EXTERNAL_REFERENCE)
                            .keys(new DefaultKey.Builder()
                                    .type(KeyTypes.GLOBAL_REFERENCE)
                                    .value("0173-1#02-BAA120#008")
                                    .build())
                            .build(),
                    "[ExternalRef](GlobalReference)0173-1#02-BAA120#008",
                    "[ExternalRef](GlobalReference)0173-1#02-BAA120#008",
                    "(GlobalReference)0173-1#02-BAA120#008",
                    "(GlobalReference)0173-1#02-BAA120#008"),
            new ReferenceSerializationInfo(
                    new DefaultReference.Builder()
                            .type(ReferenceTypes.MODEL_REFERENCE)
                            .referredSemanticId(new DefaultReference.Builder()
                                    .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                    .referredSemanticId(new DefaultReference.Builder()
                                            .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                            .keys(new DefaultKey.Builder()
                                                    .type(KeyTypes.FRAGMENT_REFERENCE)
                                                    .value("ShouldNeverBeSerialized")
                                                    .build())
                                            .build())
                                    .keys(new DefaultKey.Builder()
                                            .type(KeyTypes.CONCEPT_DESCRIPTION)
                                            .value("0173-1#02-BAA120#008")
                                            .build())
                                    .build())
                            .keys(new DefaultKey.Builder()
                                    .type(KeyTypes.SUBMODEL)
                                    .value("https://example.com/aas/1/1/1234859590")
                                    .build())
                            .keys(new DefaultKey.Builder()
                                    .type(KeyTypes.SUBMODEL_ELEMENT_LIST)
                                    .value("Documents")
                                    .build())
                            .keys(new DefaultKey.Builder()
                                    .type(KeyTypes.SUBMODEL_ELEMENT_COLLECTION)
                                    .value("0")
                                    .build())
                            .keys(new DefaultKey.Builder()
                                    .type(KeyTypes.MULTI_LANGUAGE_PROPERTY)
                                    .value("Title")
                                    .build())
                            .build(),
                    "[ModelRef- [ExternalRef](ConceptDescription)0173-1#02-BAA120#008 -](Submodel)https://example.com/aas/1/1/1234859590, (SubmodelElementList)Documents, (SubmodelElementCollection)0, (MultiLanguageProperty)Title",
                    "[ModelRef](Submodel)https://example.com/aas/1/1/1234859590, (SubmodelElementList)Documents, (SubmodelElementCollection)0, (MultiLanguageProperty)Title",
                    "(Submodel)https://example.com/aas/1/1/1234859590, (SubmodelElementList)Documents, (SubmodelElementCollection)0, (MultiLanguageProperty)Title",
                    "(Submodel)https://example.com/aas/1/1/1234859590, (SubmodelElementList)Documents, (SubmodelElementCollection)0, (MultiLanguageProperty)Title"));

    @Test
    public void testEquals() {
        Assert.assertTrue(ReferenceHelper.equals(
                ReferenceHelper.parse("(GlobalReference)0173-1#01-ADS698#010, (GlobalReference)0173-1#01-ADS700#010"),
                ReferenceHelper.parse("(GlobalReference)0173-1#01-ADS698#010, (FragmentReference)0173-1#01-ADS700#010")));
        Assert.assertTrue(ReferenceHelper.equals(
                ReferenceHelper.parse("(Submodel)https://example.com/aas/1/1/1234859590"),
                ReferenceHelper.parse("(GlobalReference)https://example.com/aas/1/1/1234859590")));
        Assert.assertTrue(ReferenceHelper.equals(
                ReferenceHelper.parse("(Submodel)https://example.com/aas/1/1/1234859590, (File)Specification, (FragmentReference)Bibliography"),
                ReferenceHelper.parse("(GlobalReference)https://example.com/aas/1/1/1234859590, (FragmentReference)Specification, (FragmentReference)Bibliography")));
        // should return true but not possible in reality because of duplicate usage of idShort
        Assert.assertTrue(ReferenceHelper.equals(
                ReferenceHelper.parse("(Submodel)https://example.com/aas/1/1/1234859590, (File)Specification"),
                ReferenceHelper.parse("(Submodel)https://example.com/aas/1/1/1234859590, (Blob)Specification")));
    }


    @Test
    public void testNotEquals() {
        Assert.assertFalse(ReferenceHelper.equals(
                ReferenceHelper.parse("(GlobalReference)https://example.com/aas/1/1/1234859590, (FragmentReference)Specification, (FragmentReference)Bibliography"),
                ReferenceHelper.parse("(GlobalReference)https://example.com/aas/1/1/1234859590, (FragmentReference)Specification, (FragmentReference)Bibliographie")));
    }


    @Test
    public void testSerialize() {
        TEST_CASES.forEach(this::asserSerialize);
    }


    @Test
    public void testParse() {
        TEST_CASES.forEach(this::assertParse);
    }


    @Test
    public void testCombineReferences() {
        assertCombine("(Submodel)1", "(Property)2", "(Submodel)1, (Property)2");
        assertCombine(null, "(Property)2", "(Property)2");
        assertCombine("(Submodel)1", null, "(Submodel)1");
    }


    @Test
    public void testListElement() {
        Reference actualReference = ReferenceHelper.parse("[ModelRef](Submodel)SubmodelId, (SubmodelElementList)List, (SubmodelElement)1");
        IdShortPath actual = IdShortPath.parse(ReferenceHelper.toPath(actualReference));
        IdShortPath expected = IdShortPath.builder().idShort("List").index(1).build();
        Assert.assertEquals(expected, actual);
    }


    private void assertCombine(String parent, String child, String expected) {
        Reference actual = ReferenceHelper.combine(
                ReferenceHelper.parse(parent),
                ReferenceHelper.parse(child));
        Assert.assertEquals(ReferenceHelper.parse(expected), actual);
    }


    private void asserSerialize(ReferenceSerializationInfo referenceSerializationInfo) {
        assertSerialize(referenceSerializationInfo, true, true);
        assertSerialize(referenceSerializationInfo, true, false);
        assertSerialize(referenceSerializationInfo, false, true);
        assertSerialize(referenceSerializationInfo, false, false);
    }


    private void assertParse(ReferenceSerializationInfo referenceSerializationInfo) {
        assertParse(referenceSerializationInfo, true, true);
        assertParse(referenceSerializationInfo, true, false);
        assertParse(referenceSerializationInfo, false, true);
        assertParse(referenceSerializationInfo, false, false);
    }


    private void assertSerialize(ReferenceSerializationInfo referenceSerializationInfo, boolean includeReferenceType, boolean includeReferredSemanticId) {
        String expected = referenceSerializationInfo.getStringRepresentation(includeReferenceType, includeReferredSemanticId);
        String actual = ReferenceHelper.toString(referenceSerializationInfo.reference, includeReferenceType, includeReferredSemanticId);
        Assert.assertEquals(expected, actual);
    }


    private void assertParse(ReferenceSerializationInfo referenceSerializationInfo, boolean includeReferenceType, boolean includeReferredSemanticId) {
        Reference expected = ReferenceHelper.clone(referenceSerializationInfo.reference);
        if (!includeReferenceType || !includeReferredSemanticId) {
            expected.setReferredSemanticId(null);
        }
        else if (Objects.nonNull(expected.getReferredSemanticId())) {
            expected.getReferredSemanticId().setReferredSemanticId(null);
        }
        Reference actual = ReferenceHelper.parse(referenceSerializationInfo.getStringRepresentation(includeReferenceType, includeReferredSemanticId));
        Assert.assertEquals(expected, actual);
    }

    private static class ReferenceSerializationInfo {

        private final Reference reference;
        private final String stringRepresentation_Reference_ReferredSemanticId;
        private final String stringRepresentation_Reference_NoReferredSemanticId;
        private final String stringRepresentation_NoReference_ReferredSemanticId;
        private final String stringRepresentation_NoReference_NoReferredSemanticId;

        private ReferenceSerializationInfo(Reference reference,
                String stringRepresentation_Reference_ReferredSemanticId,
                String stringRepresentation_Reference_NoReferredSemanticId,
                String stringRepresentation_NoReference_ReferredSemanticId,
                String stringRepresentation_NoReference_NoReferredSemanticId) {
            this.reference = reference;
            this.stringRepresentation_Reference_ReferredSemanticId = stringRepresentation_Reference_ReferredSemanticId;
            this.stringRepresentation_Reference_NoReferredSemanticId = stringRepresentation_Reference_NoReferredSemanticId;
            this.stringRepresentation_NoReference_ReferredSemanticId = stringRepresentation_NoReference_ReferredSemanticId;
            this.stringRepresentation_NoReference_NoReferredSemanticId = stringRepresentation_NoReference_NoReferredSemanticId;
        }


        private String getStringRepresentation(boolean includeReferenceType, boolean includeReferredSemanticId) {
            if (includeReferenceType && includeReferredSemanticId) {
                return stringRepresentation_Reference_ReferredSemanticId;
            }
            if (includeReferenceType && !includeReferredSemanticId) {
                return stringRepresentation_Reference_NoReferredSemanticId;
            }
            if (!includeReferenceType && includeReferredSemanticId) {
                return stringRepresentation_NoReference_ReferredSemanticId;
            }
            return stringRepresentation_NoReference_NoReferredSemanticId;
        }
    }

}
