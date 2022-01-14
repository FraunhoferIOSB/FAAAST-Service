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
package de.fraunhofer.iosb.ilt.faaast.service;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.DataElementValueMapper;
import io.adminshell.aas.v3.dataformat.core.AASFull;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;


public class DataElementValueMapperTest {

    AssetAdministrationShellEnvironment environment = AASFull.createEnvironment();

    @Test
    public void testPropertyValueMapping() {
        SubmodelElement submodelElement = new DefaultProperty.Builder()
                .category("Test")
                .idShort("TestProperty")
                .value("TestValue")
                .build();
        DataElementValue dataElementValue = DataElementValueMapper.toDataElement(submodelElement);
        Assert.assertTrue(dataElementValue.getClass() == PropertyValue.class);
        Assert.assertTrue(((PropertyValue) dataElementValue).getValue().equalsIgnoreCase("TestValue"));

        ((PropertyValue) dataElementValue).setValue("newTestValue");
        DataElementValueMapper.setDataElementValue(submodelElement, dataElementValue);
        Assert.assertTrue(submodelElement.getCategory().equalsIgnoreCase("Test"));
        Assert.assertTrue(((Property) submodelElement).getValue().equalsIgnoreCase("newTestValue"));
    }


    @Test
    public void testRangeValueMapping() {
        SubmodelElement submodelElement = new DefaultRange.Builder()
                .category("Test")
                .idShort("TestRange")
                .min("2.3")
                .max("5.1")
                .build();

        DataElementValue dataElementValue = DataElementValueMapper.toDataElement(submodelElement);
        Assert.assertTrue(dataElementValue.getClass() == RangeValue.class);
        Assert.assertTrue(((RangeValue) dataElementValue).getMax() == 5.1);
        Assert.assertTrue(((RangeValue) dataElementValue).getMin() == 2.3);

        ((RangeValue) dataElementValue).setMin(1.0);
        ((RangeValue) dataElementValue).setMax(2.0);
        DataElementValueMapper.setDataElementValue(submodelElement, dataElementValue);
        Assert.assertTrue(submodelElement.getCategory().equalsIgnoreCase("Test"));
        Assert.assertTrue(((Range) submodelElement).getMax().equalsIgnoreCase("2.0"));
        Assert.assertTrue(((Range) submodelElement).getMin().equalsIgnoreCase("1.0"));
    }


    @Test
    public void testReferenceElementValueMapping() {
        SubmodelElement submodelElement = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("TestSubmodel"))
                .findFirst().get().getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleSubmodelCollectionUnordered"))
                .findFirst().get()).getValues().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleReferenceElement")).findFirst().get();

        DataElementValue dataElementValue = DataElementValueMapper.toDataElement(submodelElement);
        Assert.assertTrue(dataElementValue.getClass() == ReferenceElementValue.class);
        Assert.assertTrue(((ReferenceElementValue) dataElementValue).getKeys().equals(((ReferenceElement) submodelElement).getValue().getKeys()));

        ((ReferenceElementValue) dataElementValue).setKeys(List.of(new DefaultKey.Builder().value("TestKey").build()));
        DataElementValueMapper.setDataElementValue(submodelElement, dataElementValue);
        Assert.assertTrue(((ReferenceElement) submodelElement).getValue().getKeys().get(0).getValue().equalsIgnoreCase("TestKey"));
    }


    @Test
    public void testRelationshipElementValueMapping() {
        SubmodelElement submodelElement = environment.getSubmodels().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("TestSubmodel"))
                .findFirst().get().getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleRelationshipElement"))
                .findFirst().get();
        DataElementValue dataElementValue = DataElementValueMapper.toDataElement(submodelElement);
        Assert.assertTrue(dataElementValue.getClass() == RelationshipElementValue.class);
        Assert.assertTrue(((RelationshipElementValue) dataElementValue).getFirst().equals(((RelationshipElement) submodelElement).getFirst().getKeys()));
        Assert.assertTrue(((RelationshipElementValue) dataElementValue).getSecond().equals(((RelationshipElement) submodelElement).getSecond().getKeys()));

        ((RelationshipElementValue) dataElementValue).setFirst(List.of(new DefaultKey.Builder().value("TestKey").build()));
        ((RelationshipElementValue) dataElementValue).setSecond(List.of(new DefaultKey.Builder().value("TestKey1").build()));
        DataElementValueMapper.setDataElementValue(submodelElement, dataElementValue);
        Assert.assertTrue(((RelationshipElement) submodelElement).getFirst().getKeys().get(0).getValue().equalsIgnoreCase("TestKey"));
        Assert.assertTrue(((RelationshipElement) submodelElement).getSecond().getKeys().get(0).getValue().equalsIgnoreCase("TestKey1"));
    }


    @Test
    public void testMultilanguagePropertyValueMapping() {
        SubmodelElement submodelElement = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase("https://acplt.org/Test_Submodel_Missing"))
                .findFirst().get().getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleSubmodelCollectionOrdered"))
                .findFirst().get()).getValues().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleMultiLanguageProperty")).findFirst().get();

        DataElementValue dataElementValue = DataElementValueMapper.toDataElement(submodelElement);
        Assert.assertTrue(dataElementValue.getClass() == MultiLanguagePropertyValue.class);
        Assert.assertTrue(((MultiLanguagePropertyValue) dataElementValue).getLangStringSet().containsAll(((MultiLanguageProperty) submodelElement).getValues()));

        ((MultiLanguagePropertyValue) dataElementValue).setLangStringSet(Set.of(new LangString("Test", "de")));
        DataElementValueMapper.setDataElementValue(submodelElement, dataElementValue);
        Assert.assertTrue(((MultiLanguageProperty) submodelElement).getValues().contains(new LangString("Test", "de")));
    }


    @Test
    public void testFileValueMapping() {
        SubmodelElement submodelElement = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase("https://acplt.org/Test_Submodel"))
                .findFirst().get().getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleSubmodelCollectionUnordered"))
                .findFirst().get()).getValues().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleFile")).findFirst().get();

        DataElementValue dataElementValue = DataElementValueMapper.toDataElement(submodelElement);
        Assert.assertTrue(dataElementValue.getClass() == FileValue.class);
        Assert.assertTrue(((FileValue) dataElementValue).getValue().equalsIgnoreCase(((File) submodelElement).getValue()));
        Assert.assertTrue(((FileValue) dataElementValue).getMimeType().equalsIgnoreCase(((File) submodelElement).getMimeType()));

        ((FileValue) dataElementValue).setValue("newTestValue");
        ((FileValue) dataElementValue).setMimeType("newMimeType");
        DataElementValueMapper.setDataElementValue(submodelElement, dataElementValue);
        Assert.assertTrue(((File) submodelElement).getMimeType().equalsIgnoreCase("newMimeType"));
        Assert.assertTrue(((File) submodelElement).getValue().equalsIgnoreCase("newTestValue"));
    }


    @Test
    public void testEntityMapping() {
        SubmodelElement submodelElement = environment.getSubmodels().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("BillOfMaterial"))
                .findFirst().get().getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleEntity"))
                .findFirst().get();

        DataElementValue dataElementValue = DataElementValueMapper.toDataElement(submodelElement);
        Assert.assertTrue(dataElementValue.getClass() == EntityValue.class);
        Assert.assertTrue(((EntityValue) dataElementValue).getEntityType().equals(((Entity) submodelElement).getEntityType()));
        Assert.assertTrue(((EntityValue) dataElementValue).getGlobalAssetId() == (((Entity) submodelElement).getGlobalAssetId()));

        //TODO
        //Assert.assertTrue(((EntityValue) dataElementValue).getStatements().equals(((Entity)submodelElement).getStatements()));

        ((EntityValue) dataElementValue).setEntityType(EntityType.SELF_MANAGED_ENTITY);
        ((EntityValue) dataElementValue).setStatements(List.of());
        ((EntityValue) dataElementValue).setGlobalAssetId(List.of());
        DataElementValueMapper.setDataElementValue(submodelElement, dataElementValue);
        Assert.assertTrue(((Entity) submodelElement).getEntityType().equals(EntityType.SELF_MANAGED_ENTITY));
        //Assert.assertTrue(((Entity) submodelElement).getStatements().equals(List.of()));
        Assert.assertTrue(((Entity) submodelElement).getGlobalAssetId().getKeys().equals(List.of()));
    }


    @Test
    public void testBlobValueMapping() {
        SubmodelElement submodelElement = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase("https://acplt.org/Test_Submodel"))
                .findFirst().get().getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleSubmodelCollectionUnordered"))
                .findFirst().get()).getValues().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleBlob")).findFirst().get();

        DataElementValue dataElementValue = DataElementValueMapper.toDataElement(submodelElement);
        Assert.assertTrue(dataElementValue.getClass() == BlobValue.class);
        Assert.assertTrue(((BlobValue) dataElementValue).getValue().equals(((Blob) submodelElement).getValue()));
        Assert.assertTrue(((BlobValue) dataElementValue).getMimeType().equalsIgnoreCase(((Blob) submodelElement).getMimeType()));

        ((BlobValue) dataElementValue).setValue("TestValue".getBytes());
        ((BlobValue) dataElementValue).setMimeType("newMimeType");
        DataElementValueMapper.setDataElementValue(submodelElement, dataElementValue);
        Assert.assertTrue(((Blob) submodelElement).getMimeType().equalsIgnoreCase("newMimeType"));
        Assert.assertTrue(Arrays.equals(((Blob) submodelElement).getValue(), "TestValue".getBytes()));
    }


    @Test
    public void testAnnotatedRelationshipElementMapping() {
        SubmodelElement submodelElement = environment.getSubmodels().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("Test_Submodel_Mandatory"))
                .findFirst().get().getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase("ExampleAnnotatedRelationshipElement"))
                .findFirst().get();

        DataElementValue dataElementValue = DataElementValueMapper.toDataElement(submodelElement);
        Assert.assertTrue(dataElementValue.getClass() == AnnotatedRelationshipElementValue.class);
        Assert.assertTrue(((AnnotatedRelationshipElementValue) dataElementValue).getFirst().equals(((AnnotatedRelationshipElement) submodelElement).getFirst().getKeys()));
        Assert.assertTrue(((AnnotatedRelationshipElementValue) dataElementValue).getSecond().equals(((AnnotatedRelationshipElement) submodelElement).getSecond().getKeys()));
        //TODO: Check annotations

        ((AnnotatedRelationshipElementValue) dataElementValue).setFirst(List.of(new DefaultKey.Builder().value("TestKey").build()));
        ((AnnotatedRelationshipElementValue) dataElementValue).setSecond(List.of(new DefaultKey.Builder().value("TestKey1").build()));
        DataElementValueMapper.setDataElementValue(submodelElement, dataElementValue);
        Assert.assertTrue(((AnnotatedRelationshipElement) submodelElement).getFirst().getKeys().get(0).getValue().equalsIgnoreCase("TestKey"));
        Assert.assertTrue(((AnnotatedRelationshipElement) submodelElement).getSecond().getKeys().get(0).getValue().equalsIgnoreCase("TestKey1"));
    }

}
