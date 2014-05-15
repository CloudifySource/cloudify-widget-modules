/*
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloudify.widget.common;

import com.google.common.collect.Lists;

import java.util.*;

/**
 * User: guym
 * Date: 12/20/12
 * Time: 12:52 PM
 */
public class CollectionUtils extends org.apache.commons.collections.CollectionUtils {

    // another flavor of "add all" array elements to collection.
    // this has the benefit of returning the list. so code is shorter.
    public static <T extends Collection> T addTo(Collection l, Object... os) {
        Collections.addAll(l, os);
        return (T) l;
    }

    public static boolean isEmpty(Collection c) {
        return c == null || size(c) == 0;
    }

    public static boolean isEmpty(Object[] values) {
        return values == null || size(values) == 0;
    }

    public static int size(Object[] vals) {
        return vals == null ? 0 : vals.length;
    }

    public static int size(Collection c) {
        return c == null ? 0 : c.size();
    }

    public static <T> T last(List<T> list) {
        if (size(list) > 0) {
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            return list.get(size(list) - 1);
        }
        return null;
    }

    public static <T> T first(Collection<T> collection) {
        if (size(collection) > 0) {
            return collection.iterator().next();
        }
        return null;
    }

    public static boolean isEmpty(Map metadata) {
        return metadata == null || metadata.isEmpty();
    }

    public static <T> T firstBy(Iterable<? extends T> collection, final Predicate<T> predicate) {

        LinkedList<T> output = Lists.newLinkedList(collection);
        CollectionUtils.filter(output, new org.apache.commons.collections.Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return predicate.evaluate((T) object);
            }
        });

        return CollectionUtils.first(output);
    }

    /**
     * A typed functor to wrap the of apache commons' untyped functors.
     *
     * @param <T> The desired type to evaluate.
     */
    public static interface Predicate<T> {
        public boolean evaluate(T object);
    }

}
