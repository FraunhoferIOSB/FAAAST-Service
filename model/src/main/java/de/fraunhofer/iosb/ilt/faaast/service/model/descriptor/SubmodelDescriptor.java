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
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringNameType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Registry Descriptor interface for Submodel.
 */
public interface SubmodelDescriptor {

    public String getIdShort();


    public void setIdShort(String idShort);


    public List<Endpoint> getEndpoints();


    public void setEndpoints(List<Endpoint> endpoints);


    public AdministrativeInformation getAdministration();


    public void setAdministration(AdministrativeInformation administration);


    public List<LangStringTextType> getDescriptions();


    public void setDescriptions(List<LangStringTextType> descriptions);


    public List<LangStringNameType> getDisplayNames();


    public void setDisplayNames(List<LangStringNameType> displayNames);


    public String getId();


    public void setId(String id);


    public Reference getSemanticId();


    public void setSemanticId(Reference semanticId);


    public List<Reference> getSupplementalSemanticIds();


    public void setSupplementalSemanticIds(List<Reference> suplementalSemanticIds);
}
