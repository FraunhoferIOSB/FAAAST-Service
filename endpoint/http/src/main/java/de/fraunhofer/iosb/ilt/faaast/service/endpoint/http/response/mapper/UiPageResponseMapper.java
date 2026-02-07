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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.response.mapper;

import com.google.common.net.MediaType;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.UiPageRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.UiPageResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


/**
 * Response mapper for {@link UiPageResponse}. Loads the HTML UI template from the classpath resource
 * {@code ui/index.html} and sends the result as {@code text/html}. The API prefix is derived dynamically
 * in the browser from the page URL.
 */
public class UiPageResponseMapper extends AbstractResponseMapper<UiPageResponse, UiPageRequest> {

    private static final String TEMPLATE_RESOURCE = "ui/index.html";
    private final String htmlContent;

    public UiPageResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
        this.htmlContent = loadTemplate();
    }


    @Override
    public void map(UiPageRequest apiRequest, UiPageResponse apiResponse, HttpServletResponse httpResponse) {
        HttpHelper.sendContent(
                httpResponse,
                apiResponse.getStatusCode(),
                htmlContent.getBytes(StandardCharsets.UTF_8),
                MediaType.HTML_UTF_8);
    }


    private static String loadTemplate() {
        try (InputStream is = UiPageResponseMapper.class.getClassLoader().getResourceAsStream(TEMPLATE_RESOURCE)) {
            if (is == null) {
                throw new IllegalStateException("UI template not found on classpath: " + TEMPLATE_RESOURCE);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to load UI template: " + TEMPLATE_RESOURCE, e);
        }
    }
}
