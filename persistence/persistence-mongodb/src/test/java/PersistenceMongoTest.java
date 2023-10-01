import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AbstractPersistenceTest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.persistence_mongodb.PersistenceMongo;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.persistence_mongodb.PersistenceMongoConfig;
import org.apache.jena.base.Sys;
import org.bson.Document;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertThat;

public class PersistenceMongoTest extends AbstractPersistenceTest<PersistenceMongo, PersistenceMongoConfig> {

    @Override
    public PersistenceMongoConfig getPersistenceConfig(File initialModelFile, Environment initialModel) throws ConfigurationInitializationException {
        Transitions transitions = Mongod.instance().transitions(Version.Main.PRODUCTION);
        TransitionWalker.ReachedState<RunningMongodProcess> running = transitions.walker()
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
}
