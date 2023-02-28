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

import io.adminshell.aas.v3.model.AdministrativeInformation;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.Reference;
import java.util.List;


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


    public List<LangString> getDescriptions();


    public void setDescriptions(List<LangString> descriptions);


    public Identifier getIdentification();


    public void setIdentification(Identifier identification);


    public Reference getSemanticId();


    public void setSemanticId(Reference semanticId);
}
