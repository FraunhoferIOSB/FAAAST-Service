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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;


public class SubmodelTemplateProcessorTest {

    private SubmodelTemplateProcessor processor;
    private Submodel submodel0;
    private Submodel submodel1;
    private Environment environment;
    private MessageBus messageBus;
    private Service service;

    @Before
    public void init() throws MessageBusException {
        /*
         * processor = mock(SubmodelTemplateProcessor.class);
         * mockMessageBus();
         * environment = AASFull.createEnvironment();
         * submodel0 = environment.getSubmodels().get(0);
         * submodel1 = environment.getSubmodels().get(1);
         * ArgumentMatcher<Submodel> isSubmodel0 = submodel -> Objects.equals(submodel, submodel0);
         * when(processor.accept(argThat(isSubmodel0))).thenReturn(true);
         * when(processor.add(eq(submodel0), any())).thenReturn(true);
         * when(processor.update(eq(submodel0), any())).thenReturn(true);
         * when(processor.delete(eq(submodel0), any())).thenReturn(true);
         */
    }


    @Test
    public void testStart() throws ConfigurationException, AssetConnectionException, PersistenceException, MessageBusException {
        /*
         * List<Submodel> submodels = environment.getSubmodels();
         * createService(submodels);
         * verify(processor, times(submodels.size())).accept(any());
         * verify(processor, times(1)).add(eq(submodel0), any());
         * verify(processor, times(0)).add(eq(submodel1), any());
         * verify(processor, times(0)).update(any(), any());
         * Assert.assertNotNull(service);
         */
    }


    @Test
    public void testUpdate() throws ConfigurationException, AssetConnectionException, PersistenceException, MessageBusException {
        /*
         * List<Submodel> submodels = new ArrayList<>();
         * createService(submodels);
         * // Send update event to MessageBus
         * ElementUpdateEventMessage msg = new ElementUpdateEventMessage();
         * msg.setElement(ReferenceBuilder.forSubmodel(submodel0));
         * msg.setValue(submodel0);
         * service.getMessageBus().publish(msg);
         * // called for every Submodel in createService and for the Submodel to update
         * verify(processor, times(submodels.size() + 1)).accept(any());
         * verify(processor, times(1)).update(eq(submodel0), any());
         * Assert.assertNotNull(service);
         */
    }


    @Test
    public void testDelete() throws ConfigurationException, AssetConnectionException, PersistenceException, MessageBusException {
        /*
         * List<Submodel> submodels = new ArrayList<>();
         * createService(submodels);
         * // Send delete event to MessageBus
         * ElementDeleteEventMessage msg = new ElementDeleteEventMessage();
         * msg.setElement(ReferenceBuilder.forSubmodel(submodel0));
         * msg.setValue(submodel0);
         * service.getMessageBus().publish(msg);
         * // called for every Submodel in createService and for the Submodel to delete
         * verify(processor, times(submodels.size() + 1)).accept(any());
         * verify(processor, times(1)).delete(eq(submodel0), any());
         * Assert.assertNotNull(service);
         */
    }


    @Test
    public void testCreate() throws ConfigurationException, AssetConnectionException, PersistenceException, MessageBusException {
        /*
         * List<Submodel> submodels = new ArrayList<>();
         * createService(submodels);
         * // Send create event to MessageBus
         * ElementCreateEventMessage msg = new ElementCreateEventMessage();
         * msg.setElement(ReferenceBuilder.forSubmodel(submodel0));
         * msg.setValue(submodel0);
         * service.getMessageBus().publish(msg);
         * // called for every Submodel in createService and for the Submodel to delete
         * verify(processor, times(submodels.size() + 1)).accept(any());
         * verify(processor, times(1)).add(eq(submodel0), any());
         * Assert.assertNotNull(service);
         */
    }


    private void createService(List<Submodel> submodels) throws AssetConnectionException, MessageBusException, ConfigurationException, PersistenceException {
        Persistence persistence = mock(Persistence.class);
        FileStorage fileStorage = mock(FileStorage.class);
        when(persistence.getAllSubmodels(any(), any())).thenReturn(Page.of(submodels));
        service = new Service(CoreConfig.DEFAULT, persistence, fileStorage, messageBus, List.of(), List.of(), List.of(processor));
    }


    private void mockMessageBus() throws MessageBusException {
        messageBus = mock(MessageBus.class);

        doAnswer((InvocationOnMock invocation) -> {
            ElementCreateEventMessage eventMessage = invocation.getArgument(0);
            try {
                service.handleCreateEvent(eventMessage);
            }
            catch (Exception e) {
                fail();
            }
            return null;
        }).when(messageBus).publish(any(ElementCreateEventMessage.class));
        doAnswer((InvocationOnMock invocation) -> {
            ElementUpdateEventMessage eventMessage = invocation.getArgument(0);
            try {
                service.handleUpdateEvent(eventMessage);
            }
            catch (Exception e) {
                fail();
            }
            return null;
        }).when(messageBus).publish(any(ElementUpdateEventMessage.class));

        doAnswer((InvocationOnMock invocation) -> {
            ElementDeleteEventMessage eventMessage = invocation.getArgument(0);
            try {
                service.handleDeleteEvent(eventMessage);
            }
            catch (Exception e) {
                fail();
            }
            return null;
        }).when(messageBus).publish(any(ElementDeleteEventMessage.class));
    }
}
