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
package de.fraunhofer.iosb.ilt.faaast.service.model.descriptor;

import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;


/**
 * Registry Descriptor interface for AssetAdministrationShell.
 */
public interface AssetAdministrationShellDescriptor {

    public AdministrativeInformation getAdministration();


    public void setAdministration(AdministrativeInformation administration);


    public AssetKind getAssetKind();


    public void setAssetKind(AssetKind assetKind);


    public String getAssetType();


    public void setAssetType(String assetType);


    public List<Endpoint> getEndpoints();


    public void setEndpoints(List<Endpoint> endpoints);


    public String getGlobalAssetId();


    public void setGlobalAssetId(String globalAssetId);


    public String getIdShort();


    public void setIdShort(String idShort);


    public String getId();


    public void setId(String identification);


    public List<SpecificAssetID> getSpecificAssetIds();


    public void setSpecificAssetIds(List<SpecificAssetID> specificAssetIds);


    public List<SubmodelDescriptor> getSubmodels();


    public void setSubmodels(List<SubmodelDescriptor> submodels);


    public List<LangStringTextType> getDescriptions();


    public void setDescriptions(List<LangStringTextType> descriptions);


    public List<LangStringNameType> getDisplayNames();


    public void setDisplayNames(List<LangStringNameType> displayNames);


    public List<Extension> getExtensions();


    public void setExtensions(List<Extension> extensions);
}
