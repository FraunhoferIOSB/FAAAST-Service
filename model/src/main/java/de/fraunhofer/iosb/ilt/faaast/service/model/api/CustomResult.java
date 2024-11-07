package de.fraunhofer.iosb.ilt.faaast.service.model.api;

import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.Result;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ResultBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMessage;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResult;


/**
 * CustumResul class is extending the implementation of AAS4J result class.
 */
public class CustomResult extends DefaultResult {
    

    protected abstract static class CustomResultBuilder<T extends Result, B extends CustomResultBuilder<T, B>> extends ResultBuilder<T, B> {
        
        //! AAS4J DefaultMessage is diffrent to Faaast Message
        //! AAS4J Message interface defines "timestamp" variable as String, Faaast is using Date type
        public B message(MessageTypeEnum messageType, String messageText) {
            getBuildingInstance().getMessages().add(
                    new DefaultMessage.Builder()
                            .messageType(messageType)
                            .text(messageText)
                            .build());
            return getSelf();
        }
    
    }

    public static class Builder extends CustomResultBuilder<CustomResult, Builder> {
        
        @Override
        protected Builder getSelf() {
            return this;
        }

        @Override
        protected CustomResult newBuildingInstance() {
            return new CustomResult();
        }

    }

}