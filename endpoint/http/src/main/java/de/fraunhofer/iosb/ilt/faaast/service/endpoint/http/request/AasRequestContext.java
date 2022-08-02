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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Contextualization for supporting requests in the AAS context
 * /shells/{aasIdentifier}/aas
 */
public class AasRequestContext implements RequestContext {

    private static final String AAS_ID = "aasId";
    private ServiceContext serviceContext;
    private Pattern pattern;

    @Override
    public String decontextualize(String url) {
        return url.substring(url.indexOf("/aas") + 5);
    }


    @Override
    public void init(ServiceContext serviceContext, String baseUrlPattern) {
        this.serviceContext = serviceContext;
        String temp = baseUrlPattern;
        if (temp.startsWith(PATTERN_LINE_START)) {
            temp = temp.substring(PATTERN_LINE_START.length());
        }
        if (temp.endsWith(PATTERN_LINE_END)) {
            temp = temp.substring(0, temp.length() - PATTERN_LINE_END.length());
        }
        if (!temp.startsWith("/")) {
            temp = "/" + temp;
        }
        pattern = Pattern.compile(String.format("%sshells/(?<%s>.*?)/aas%s%s", PATTERN_LINE_START, AAS_ID, temp, PATTERN_LINE_END));
    }


    @Override
    public boolean matches(String url) {
        return pattern.matcher(url).matches();
    }


    @Override
    public void validate(HttpRequest httpRequest) throws InvalidRequestException {
        Matcher matcher = pattern.matcher(httpRequest.getPath());
        matcher.matches();
        String aasId = EncodingHelper.base64Decode(matcher.group(AAS_ID));
        if (serviceContext.getAASEnvironment().getAssetAdministrationShells().stream()
                .noneMatch(x -> Objects.equals(aasId, x.getIdentification().getIdentifier()))) {
            throw new InvalidRequestException(String.format("invalid aasId '%s'", aasId));
        }
    }

}
