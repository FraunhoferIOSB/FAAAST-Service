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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.persistence_mongodb;
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

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AbstractPersistenceTest;
import java.io.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;


/**
 * Tests for the mongo database persistence implementation.
 */
public class PersistenceMongoTest extends AbstractPersistenceTest<PersistenceMongo, PersistenceMongoConfig> {
    private TransitionWalker.ReachedState<RunningMongodProcess> running;

    @Override
    public PersistenceMongoConfig getPersistenceConfig(File initialModelFile, Environment initialModel) throws ConfigurationInitializationException {
        Transitions transitions = Mongod.instance().transitions(Version.Main.PRODUCTION);
        running = transitions.walker()
                .initState(StateID.of(RunningMongodProcess.class));
        de.flapdoodle.embed.mongo.commands.ServerAddress serverAddress = running.current().getServerAddress();

        PersistenceMongoConfig result = PersistenceMongoConfig
                .builder()
                .initialModel(initialModel)
                .initialModelFile(initialModelFile)
                .connectionString("mongodb://" + serverAddress.getHost() + ":" + serverAddress.getPort())
                .databaseName("FaaastTest")
                .collectionName("test")
                .modelId("test1")
                .build();
        return result;
    }


    @Override
    public void close() {
        running.close();
    }
}
