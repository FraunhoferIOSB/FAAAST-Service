import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AbstractPersistenceTest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.persistence_mongodb.PersistenceMongo;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.persistence_mongodb.PersistenceMongoConfig;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.junit.Before;

import java.io.File;

public class PersistenceMongoTest extends AbstractPersistenceTest<PersistenceMongo, PersistenceMongoConfig> {
    @Override
    public PersistenceMongoConfig getPersistenceConfig(File initialModelFile, Environment initialModel) throws ConfigurationInitializationException {
        PersistenceMongoConfig result = PersistenceMongoConfig
                .builder()
                .initialModel(initialModel)
                .initialModelFile(initialModelFile)
                .databaseName("FaaastTest")
                .collectionName("test")
                .modelId("test1")
                .build();
        return result;
    }
}
