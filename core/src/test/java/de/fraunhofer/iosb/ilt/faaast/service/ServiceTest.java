package de.fraunhofer.iosb.ilt.faaast.service;

import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class ServiceTest {
    private static MessageBus MESSAGE_BUS;
    private static final Persistence PERSISTENCE = Mockito.mock(Persistence.class);
    private static Service service;

    @BeforeClass
    public static void init() throws Exception {
        MESSAGE_BUS = Mockito.mock(MessageBus.class);

        service = new Service(new CoreConfig(), PERSISTENCE, MESSAGE_BUS, null, new ArrayList<>());
        service.start();
    }

    @Test
    public void testMessagebusEvent() throws Exception {
        ChangeEventMessage event = ElementDeleteEventMessage.builder().build();
        Answer answer = new Answer() {
            public Integer answer(InvocationOnMock invocation) {
                ChangeEventMessage eventMessage = invocation.getArgument(0);
                service.handleRegistryEvent(eventMessage);
                return null;
            }
        };
        doAnswer(answer).when(MESSAGE_BUS).publish(any(ChangeEventMessage.class));

        MESSAGE_BUS.publish(event);
        verify(MESSAGE_BUS).publish(any(ChangeEventMessage.class));
    }

}
