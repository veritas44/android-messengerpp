/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.common.collections.multimap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;

import org.solovyev.android.messenger.entities.Entity;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

@ThreadSafe
public final class ThreadSafeMultimap<K, V> {

	@Nonnull
	public static final List<?> NO_VALUE = emptyList();

	@Nonnull
	private volatile Map<K, List<V>> map;

	private ThreadSafeMultimap(@Nonnull Map<K, List<V>> map) {
		this.map = map;
	}

	@Nonnull
	public static <K, V> ThreadSafeMultimap<K, V> newThreadSafeMultimap() {
		return newThreadSafeMultimap(new HashMap<K, List<V>>());
	}

	@Nonnull
	public static <K, V> ThreadSafeMultimap<K, V> newThreadSafeMultimap(@Nonnull Map<K, List<V>> map) {
		return new ThreadSafeMultimap<K, V>(map);
	}

	@Nonnull
	public List<V> get(@Nonnull K key) {
		final List<V> values = map.get(key);
		if (values == null) {
			return (List<V>) NO_VALUE;
		} else {
			return unmodifiableList(values);
		}
	}

	public synchronized boolean update(@Nonnull K key, @Nonnull ListUpdater<V> updater) {
		final List<V> newValue = updater.update(get(key));
		if (newValue != null) {
			map.put(key, newValue);
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean update(@Nonnull MapUpdater<K, V> updater) {
		Map<K, List<V>> newMap = updater.update(unmodifiableMap(map));
		if (newMap != null) {
			map = newMap;
			return true;
		} else {
			return false;
		}
	}

	// for tests
	@Nonnull
	Map<K, List<V>> asMap() {
		return map;
	}

	public void remove(@Nonnull K key) {
		map.remove(key);
	}

	/*
	**********************************************************************
    *
    *                           STATIC/INNER CLASSES
    *
    **********************************************************************
    */

	public static interface ListUpdater<V> {
		@Nullable
		List<V> update(@Nonnull List<V> values);
	}

	public static interface MapUpdater<K, V> {
		@Nullable
		Map<K, List<V>> update(@Nonnull Map<K, List<V>> map);
	}

	public static <K, V> Map<K, List<V>> copy(@Nonnull Map<K, List<V>> map) {
		final Map<K, List<V>> copy = new HashMap<K, List<V>>(map.size());
		for (Map.Entry<K, List<V>> entry : map.entrySet()) {
			copy.put(entry.getKey(), copy(entry.getValue()));
		}
		return copy;
	}

	public static <V> List<V> copy(@Nonnull List<V> values) {
		return new ArrayList<V>(values);
	}
}
