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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.postgres;

import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;


/**
 * Externalization of Blob element values. Blob content is not stored inside the submodel JSONB documents (where it
 * would be rewritten on every update of any element in the same submodel and transferred on every read) but in the
 * separate blob store table. In the document, the Blob's value is replaced by a small unique placeholder of the form
 * {@code faaast-blob:&lt;uuid&gt;}. The blob id under which the content is stored is the base64 form of the
 * placeholder, i.e. exactly the string that appears as the Blob's value in the JSON document - this allows the
 * database trigger to match blob store rows against the document for orphan cleanup.
 *
 * <p>Placeholders never leak to clients: reads with {@code Extent.WITHOUT_BLOB_VALUE} (the default) strip all Blob
 * values via {@code QueryModifierHelper}, and reads with {@code Extent.WITH_BLOB_VALUE} resolve the placeholders back
 * to the stored content first.
 */
public final class BlobExternalization {

    private static final String PLACEHOLDER_PREFIX = "faaast-blob:";
    private static final int PLACEHOLDER_LENGTH = PLACEHOLDER_PREFIX.length() + 36;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
            Pattern.quote(PLACEHOLDER_PREFIX) + "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}");

    private BlobExternalization() {}


    /**
     * Checks whether the element tree contains at least one Blob value that should be externalized.
     *
     * @param root the root of the element tree, e.g. a Submodel or SubmodelElement
     * @return true if a Blob with externalizable value is present
     */
    static boolean containsExternalizableBlobValue(Referable root) {
        boolean[] found = new boolean[1];
        walkBlobs(root, blob -> found[0] |= isExternalizable(blob));
        return found[0];
    }


    /**
     * Replaces all externalizable Blob values in the element tree with fresh placeholders. The caller is responsible
     * for passing a copy if the original object must not be modified, and for persisting the returned content in the
     * same transaction as the document.
     *
     * @param root the root of the element tree, e.g. a Submodel or SubmodelElement
     * @return the externalized content by blob id
     */
    static Map<String, byte[]> externalizeBlobValues(Referable root) {
        Map<String, byte[]> result = new LinkedHashMap<>();
        walkBlobs(root, blob -> {
            if (isExternalizable(blob)) {
                byte[] placeholder = (PLACEHOLDER_PREFIX + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8);
                result.put(blobId(placeholder), blob.getValue());
                blob.setValue(placeholder);
            }
        });
        return result;
    }


    /**
     * Finds all Blobs in the element tree whose value is an externalization placeholder, grouped by blob id, so the
     * caller can resolve them against the blob store.
     *
     * @param root the root of the element tree, e.g. a Submodel or SubmodelElement
     * @return the placeholder Blobs by blob id
     */
    static Map<String, List<Blob>> findBlobPlaceholders(Referable root) {
        Map<String, List<Blob>> result = new LinkedHashMap<>();
        walkBlobs(root, blob -> {
            if (isPlaceholder(blob.getValue())) {
                result.computeIfAbsent(blobId(blob.getValue()), x -> new ArrayList<>()).add(blob);
            }
        });
        return result;
    }


    private static boolean isExternalizable(Blob blob) {
        return blob.getValue() != null
                && blob.getValue().length > 0
                && !isPlaceholder(blob.getValue());
    }


    private static boolean isPlaceholder(byte[] value) {
        return value != null
                && value.length == PLACEHOLDER_LENGTH
                && PLACEHOLDER_PATTERN.matcher(new String(value, StandardCharsets.UTF_8)).matches();
    }


    /**
     * The blob id of a placeholder: its base64 form, exactly as the placeholder appears as Blob value in the JSON
     * document (and in the value_text column of the submodel element index).
     */
    private static String blobId(byte[] placeholder) {
        return Base64.getEncoder().encodeToString(placeholder);
    }


    private static void walkBlobs(Referable root, Consumer<Blob> consumer) {
        AssetAdministrationShellElementWalker.builder()
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Blob blob) {
                        consumer.accept(blob);
                    }
                })
                .build()
                .walk(root);
    }
}
