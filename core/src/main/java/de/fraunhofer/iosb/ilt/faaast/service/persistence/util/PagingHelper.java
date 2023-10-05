package de.fraunhofer.iosb.ilt.faaast.service.persistence.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PagingHelper {
    private PagingHelper() {}

    private static long readCursor(String cursor) {
        return Long.parseLong(cursor);
    }


    private static String writeCursor(long index) {
        return Long.toString(index);
    }


    private static String nextCursor(PagingInfo paging, int resultCount) {
        return nextCursor(paging, paging.hasLimit() && resultCount > paging.getLimit());
    }


    private static String nextCursor(PagingInfo paging, boolean hasMoreData) {
        if (!hasMoreData) {
            return null;
        }
        if (!paging.hasLimit()) {
            throw new IllegalStateException("unable to generate next cursor for paging - there should not be more data available if previous request did not have a limit set");
        }
        if (Objects.isNull(paging.getCursor())) {
            return writeCursor(paging.getLimit());
        }
        return writeCursor(readCursor(paging.getCursor()) + paging.getLimit());
    }


    public static <T extends Referable> Page<T> preparePagedResult(Stream<T> input, QueryModifier modifier, PagingInfo paging) {
        Stream<T> result = input;
        if (Objects.nonNull(paging.getCursor())) {
            result = result.skip(readCursor(paging.getCursor()));
        }
        if (paging.hasLimit()) {
            result = result.limit(paging.getLimit() + 1);
        }
        List<T> temp = result.collect(Collectors.toList());
        return Page.<T> builder()
                .result(QueryModifierHelper.applyQueryModifier(
                        temp.stream()
                                .limit(paging.hasLimit() ? paging.getLimit() : temp.size())
                                .map(DeepCopyHelper::deepCopy)
                                .collect(Collectors.toList()),
                        modifier))
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor(paging, temp.size()))
                        .build())
                .build();
    }
}
