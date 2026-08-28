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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator;

import com.prosysopc.ua.StatusException;
import opc.ua.aas.datatypes.AASAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;


/**
 * Helper class to create AdministrativeInformations and integrate them into the
 * OPC UA address space.
 */
public class AdministrativeInformationCreator {

    private AdministrativeInformationCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds the AdminInformation Properties to the given node (if they don't
     * exist).
     *
     * @param info The corresponding AAS AdministrativeInformation object
     * @return The corresponding AASAdministrativeInformation.
     * @throws StatusException If an error occurs
     */
    public static AASAdministrativeInformation getAdminInformation(AdministrativeInformation info)
            throws StatusException {
        AASAdministrativeInformation retval = null;
        if (info != null) {
            retval = new AASAdministrativeInformation();
            if (info.getVersion() != null) {
                retval.setVersion(info.getVersion());
            }

            if (info.getRevision() != null) {
                retval.setRevision(info.getRevision());
            }

            if (info.getTemplateId() != null) {
                retval.setTemplateId(info.getTemplateId());
            }

            if (info.getCreator() != null) {
                retval.setCreator(ReferenceCreator.getAasReference(info.getCreator()));
            }
        }
        return retval;
    }

}
