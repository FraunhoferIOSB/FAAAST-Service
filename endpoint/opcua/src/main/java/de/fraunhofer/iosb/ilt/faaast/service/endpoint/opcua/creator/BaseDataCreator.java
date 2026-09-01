/*
 * Copyright 2026 Fraunhofer IOSB.
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
import java.util.List;
import opc.ua.aas.datatypes.AASHasSemantics;
import opc.ua.aas.datatypes.AASIdentifiable;
import opc.ua.aas.datatypes.AASReference;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;


/**
 * Creator class for common base data.
 */
public class BaseDataCreator {

    private BaseDataCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Gets the AAS Identifiable information for the given node.
     *
     * @param identifiable The Identifiable.
     * @return The corresponding AASIdentifiable.
     * @throws StatusException if an error occurs
     */
    public static AASIdentifiable getIdentifiable(Identifiable identifiable)
            throws StatusException {
        if (identifiable == null) {
            return null;
        }
        AASIdentifiable retval = new AASIdentifiable();

        //Ensure.requireNonNull(identifiableNode);
        //if (identifier != null) {
        retval.setId(identifiable.getId());
        //}

        retval.setAdministration(AdministrativeInformationCreator.getAdminInformation(identifiable.getAdministration()));

        //if (identifiableNode.getReferable() == null) {
        //    identifiableNode.setReferable(new AASReferable());
        //}
        //identifiableNode.getReferable().setCategory(category != null ? category : "");
        retval.setReferable(ReferableCreator.getReferableData(identifiable));

        //if (AasServiceNodeManager.VALUES_READ_ONLY) {
        //    identifiableNode.getIdNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        //    identifiableNode.getCategoryNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        //}

        return retval;
    }


    public static AASHasSemantics getHasSemantics(HasSemantics semantics) {
        if (semantics == null) {
            return null;
        }
        AASHasSemantics retval = new AASHasSemantics();
        if (semantics.getSemanticId() != null) {
            retval.setSemanticId(ReferenceCreator.getAasReference(semantics.getSemanticId()));
        }

        if (semantics.getSupplementalSemanticIds() != null) {
            List<AASReference> refs = ReferenceCreator.getAasReferences(semantics.getSupplementalSemanticIds());
            retval.setSupplementalSemanticId(refs.toArray(AASReference[]::new));
        }
        return retval;
    }
}
