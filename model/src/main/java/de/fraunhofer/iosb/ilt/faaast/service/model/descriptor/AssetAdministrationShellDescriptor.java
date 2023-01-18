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

import java.io.Serializable;
import java.util.List;


/**
 * Registry Descriptor interface for AssetAdministrationShell.
 */
public interface AssetAdministrationShellDescriptor extends Serializable {

    public String getIdShort();


    public void setIdShort(String idShort);


    public List<EndpointDescriptor> getEndpoints();


    public void setEndpoints(List<EndpointDescriptor> endpoints);


    public AdministrationDescriptor getAdministration();


    public void setAdministrationDescriptor(AdministrationDescriptor administration);


    public List<DescriptionDescriptor> getDescriptions();


    public void setDescriptions(List<DescriptionDescriptor> descriptions);


    public ReferenceDescriptor getGlobalAssetId();


    public void setGlobalAssetId(ReferenceDescriptor globalAssetId);


    public IdentificationDescriptor getIdentification();


    public void setIdentification(IdentificationDescriptor identification);


    public List<IdentifierKeyValuePairDescriptor> getSpecificAssetIds();


    public void setSpecificAssetIds(List<IdentifierKeyValuePairDescriptor> specificAssetIds);


    public List<SubmodelDescriptor> getSubmodels();


    public void setSubmodels(List<SubmodelDescriptor> submodels);
}
