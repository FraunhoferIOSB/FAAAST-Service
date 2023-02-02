package de.fraunhofer.iosb.ilt.faaast.service.persistence;

import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;

public interface ConfigurablePersistence<T extends Config> extends Persistence, Configurable<T> { }
