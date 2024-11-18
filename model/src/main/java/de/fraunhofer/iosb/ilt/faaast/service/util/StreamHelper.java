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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Utility class helping with streams.
 */
public class StreamHelper {

    private StreamHelper() {}


    /**
     * Creates a stream of the given elements or an empty stream if agument is null or empty.
     *
     * @param <T> type of the elements
     * @param values values to convert to stream
     * @return a stream of the given elements
     */
    public static <T> Stream<T> toStream(T... values) {
        return Arrays.asList(values).stream();
    }


    /**
     * Creates a stream of a given enumeration.
     *
     * @param <T> type of the elements
     * @param enumeration the enumeration to convert to stream
     * @return a stream of the given elements
     */
    public static <T> Stream<T> toStream(Enumeration<T> enumeration) {
        return StreamSupport.stream(new EnumerationSpliterator<>(Long.MAX_VALUE, Spliterator.ORDERED, enumeration), false);
    }


    /**
     * Concacts a number of streams.
     *
     * @param <T> the type of the concatted stream
     * @param streams the stream to concat
     * @return A stream containing all elements of all streams
     */
    public static <T> Stream<T> concat(Stream<? extends T>... streams) {
        return Stream.of(streams)
                .reduce(Stream.empty(), Stream::concat)
                .map(Function.identity());
    }

    private static class EnumerationSpliterator<T> extends AbstractSpliterator<T> {

        private final Enumeration<T> enumeration;

        public EnumerationSpliterator(long est, int additionalCharacteristics, Enumeration<T> enumeration) {
            super(est, additionalCharacteristics);
            this.enumeration = enumeration;
        }


        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (enumeration.hasMoreElements()) {
                action.accept(enumeration.nextElement());
                return true;
            }
            return false;
        }


        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            while (enumeration.hasMoreElements())
                action.accept(enumeration.nextElement());
        }
    }
}
