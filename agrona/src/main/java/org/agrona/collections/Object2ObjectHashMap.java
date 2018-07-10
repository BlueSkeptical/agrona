/*
 * Copyright 2014-2018 Real Logic Ltd.
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
package org.agrona.collections;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;
import static org.agrona.BitUtil.findNextPositivePowerOfTwo;
import static org.agrona.collections.CollectionUtil.validateLoadFactor;

/**
 * A open addressing with linear probing hash map, same algo as Int2Int.
 */
public class Object2ObjectHashMap<K, V> implements Map<K, V>, Serializable
{
    static final int MIN_CAPACITY = 8;

    private final float loadFactor;
    private int resizeThreshold;
    private int size = 0;
    private final boolean shouldAvoidAllocation;

    private Object[] entries;
    private KeySet keySet;
    private Values values;
    private EntrySet entrySet;

    public Object2ObjectHashMap()
    {
        this(MIN_CAPACITY, Hashing.DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings("unchecked")
    public Object2ObjectHashMap(
        final int initialCapacity,
        final float loadFactor)
    {
        this(initialCapacity, loadFactor, true);
    }

    /**
     * @param initialCapacity       for the map to override {@link #MIN_CAPACITY}
     * @param loadFactor            for the map to override {@link Hashing#DEFAULT_LOAD_FACTOR}.
     * @param shouldAvoidAllocation should allocation be avoided by caching iterators and map entries.
     */
    public Object2ObjectHashMap(
        final int initialCapacity,
        final float loadFactor,
        final boolean shouldAvoidAllocation)
    {
        validateLoadFactor(loadFactor);

        this.loadFactor = loadFactor;
        this.shouldAvoidAllocation = shouldAvoidAllocation;

        capacity(findNextPositivePowerOfTwo(Math.max(MIN_CAPACITY, initialCapacity)));
    }

    /**
     * Get the load factor applied for resize operations.
     *
     * @return the load factor applied for resize operations.
     */
    public float loadFactor()
    {
        return loadFactor;
    }

    /**
     * Get the actual threshold which when reached the map will resize.
     * This is a function of the current capacity and load factor.
     *
     * @return the threshold when the map will resize.
     */
    public int resizeThreshold()
    {
        return resizeThreshold;
    }

    /**
     * {@inheritDoc}
     */
    public int size()
    {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
        return size == 0;
    }

    @SuppressWarnings("unchecked")
    public V get(final Object key)
    {
        Objects.requireNonNull(key);

        final Object[] entries = this.entries;
        final int mask = entries.length - 1;
        int index = Hashing.evenHash(key.hashCode(), mask);

        Object value = null;
        while (entries[index + 1] != null)
        {
            if (entries[index] == key || entries[index].equals(key))
            {
                value = entries[index + 1];
                break;
            }

            index = next(index, mask);
        }

        return (V)value;
    }

    /**
     * Put a key value pair into the map.
     *
     * @param key   lookup key
     * @param value new value, must not be null
     * @return current value associated with key, or null if none found
     * @throws IllegalArgumentException if value is null
     */
    @SuppressWarnings("unchecked")
    public V put(final Object key, final Object value)
    {
        requireNonNull(value, "Value cannot be null");

        final Object[] entries = this.entries;
        final int mask = entries.length - 1;
        int index = Hashing.evenHash(key.hashCode(), mask);
        Object oldValue = null;

        while (entries[index + 1] != null)
        {
            if (entries[index] == key || entries[index].equals(key))
            {
                oldValue = entries[index + 1];
                break;
            }

            index = next(index, mask);
        }

        if (oldValue == null)
        {
            ++size;
            entries[index] = key;
        }

        entries[index + 1] = value;

        increaseCapacity();

        return (V)oldValue;
    }

    private void increaseCapacity()
    {
        if (size > resizeThreshold)
        {
            // entries.length = 2 * capacity
            final int newCapacity = entries.length;
            rehash(newCapacity);
        }
    }

    private void rehash(final int newCapacity)
    {
        final Object[] oldEntries = entries;
        final int length = entries.length;

        capacity(newCapacity);

        for (int keyIndex = 0; keyIndex < length; keyIndex += 2)
        {
            if (oldEntries[keyIndex + 1] != null)
            {
                put(oldEntries[keyIndex], oldEntries[keyIndex + 1]);
            }
        }
    }

    /**
     * Does the map contain the value.
     *
     * @param value to be tested against contained values.
     * @return true if contained otherwise value.
     */
    public boolean containsValue(final Object value)
    {
        boolean found = false;
        if (value != null)
        {
            final Object[] entries = this.entries;
            final int length = entries.length;

            for (int valueIndex = 1; valueIndex < length; valueIndex += 2)
            {
                if (value == entries[valueIndex] || value.equals(entries[valueIndex]))
                {
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        if (size > 0)
        {
            Arrays.fill(entries, null);
            size = 0;
        }
    }

    /**
     * Compact the backing arrays by rehashing with a capacity just larger than current size
     * and giving consideration to the load factor.
     */
    public void compact()
    {
        final int idealCapacity = (int)Math.round(size() * (1.0d / loadFactor));
        rehash(findNextPositivePowerOfTwo(Math.max(MIN_CAPACITY, idealCapacity)));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void forEach(final BiConsumer<? super K, ? super V> consumer)
    {
        final Object[] entries = this.entries;
        final int length = entries.length;

        for (int keyIndex = 0; keyIndex < length; keyIndex += 2)
        {
            if (entries[keyIndex + 1] != null)
            {
                consumer.accept((K)entries[keyIndex], (V)entries[keyIndex + 1]);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(final Object key)
    {
        return get(key) != null;
    }

    /**
     * {@inheritDoc}
     */
    public void putAll(final Map<? extends K, ? extends V> map)
    {
        for (final Entry<? extends K, ? extends V> entry : map.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    public KeySet keySet()
    {
        if (null == keySet)
        {
            keySet = new KeySet();
        }

        return keySet;
    }

    /**
     * {@inheritDoc}
     */
    public Values values()
    {
        if (null == values)
        {
            values = new Values();
        }

        return values;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Entry<K, V>> entrySet()
    {
        if (null == entrySet)
        {
            entrySet = new EntrySet();
        }

        return entrySet;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public V remove(final Object key)
    {
        final Object[] entries = this.entries;
        final int mask = entries.length - 1;
        int keyIndex = Hashing.evenHash(key.hashCode(), mask);

        Object oldValue = null;
        while (entries[keyIndex + 1] != null)
        {
            if (entries[keyIndex] == key || entries[keyIndex].equals(key))
            {
                oldValue = entries[keyIndex + 1];
                entries[keyIndex] = null;
                entries[keyIndex + 1] = null;
                size--;

                compactChain(keyIndex);

                break;
            }

            keyIndex = next(keyIndex, mask);
        }

        return (V)oldValue;
    }

    @SuppressWarnings("FinalParameters")
    private void compactChain(int deleteKeyIndex)
    {
        final Object[] entries = this.entries;
        final int mask = entries.length - 1;
        int keyIndex = deleteKeyIndex;

        while (true)
        {
            keyIndex = next(keyIndex, mask);
            if (entries[keyIndex + 1] == null)
            {
                break;
            }

            final int hash = Hashing.evenHash(entries[keyIndex].hashCode(), mask);

            if ((keyIndex < hash && (hash <= deleteKeyIndex || deleteKeyIndex <= keyIndex)) ||
                (hash <= deleteKeyIndex && deleteKeyIndex <= keyIndex))
            {
                entries[deleteKeyIndex] = entries[keyIndex];
                entries[deleteKeyIndex + 1] = entries[keyIndex + 1];

                entries[keyIndex + 1] = null;
                deleteKeyIndex = keyIndex;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        if (isEmpty())
        {
            return "{}";
        }

        final EntryIterator entryIterator = new EntryIterator();
        entryIterator.reset();

        final StringBuilder sb = new StringBuilder().append('{');
        while (true)
        {
            entryIterator.next();
            sb.append(entryIterator.getKey()).append('=').append(entryIterator.getValue());
            if (!entryIterator.hasNext())
            {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Map))
        {
            return false;
        }

        final Map<K, V> that = (Map<K, V>)o;

        return size == that.size() && entrySet().equals(that.entrySet());

    }

    public int hashCode()
    {
        return entrySet().hashCode();
    }

    private static int next(final int index, final int mask)
    {
        return (index + 2) & mask;
    }

    private void capacity(final int newCapacity)
    {
        final int entriesLength = newCapacity * 2;
        if (entriesLength < 0)
        {
            throw new IllegalStateException("Max capacity reached at size=" + size);
        }

        /*@DoNotSub*/ resizeThreshold = (int)(newCapacity * loadFactor);
        entries = new Object[entriesLength];
        size = 0;
        Arrays.fill(entries, null);
    }

    // ---------------- Utility Classes ----------------

    abstract class AbstractIterator implements Serializable
    {
        protected boolean isPositionValid = false;
        private int remaining;
        private int positionCounter;
        private int stopCounter;

        final void reset()
        {
            isPositionValid = false;
            remaining = Object2ObjectHashMap.this.size;
            final Object[] entries = Object2ObjectHashMap.this.entries;
            final int capacity = entries.length;

            int keyIndex = capacity;
            if (entries[capacity - 1] != null)
            {
                keyIndex = 0;
                for (; keyIndex < capacity; keyIndex += 2)
                {
                    if (entries[keyIndex + 1] == null)
                    {
                        break;
                    }
                }
            }

            stopCounter = keyIndex;
            positionCounter = keyIndex + capacity;
        }

        final int keyPosition()
        {
            return positionCounter & entries.length - 1;
        }

        public int remaining()
        {
            return remaining;
        }

        public boolean hasNext()
        {
            return remaining > 0;
        }

        protected final void findNext()
        {
            if (!hasNext())
            {
                throw new NoSuchElementException();
            }

            final Object[] entries = Object2ObjectHashMap.this.entries;
            final int mask = entries.length - 1;

            for (int keyIndex = positionCounter - 2; keyIndex >= stopCounter; keyIndex -= 2)
            {
                final int index = keyIndex & mask;
                if (entries[index + 1] != null)
                {
                    isPositionValid = true;
                    positionCounter = keyIndex;
                    --remaining;
                    return;
                }
            }

            isPositionValid = false;
            throw new IllegalStateException();
        }

        public void remove()
        {
            if (isPositionValid)
            {
                final int position = keyPosition();
                entries[position + 1] = null;
                --size;

                compactChain(position);

                isPositionValid = false;
            }
            else
            {
                throw new IllegalStateException();
            }
        }
    }

    final class KeyIterator extends AbstractIterator implements Iterator<K>
    {
        @SuppressWarnings("unchecked")
        public K next()
        {
            findNext();

            return (K)entries[keyPosition()];
        }
    }

    public final class ValueIterator extends AbstractIterator implements Iterator<V>
    {
        @SuppressWarnings("unchecked")
        public V next()
        {
            findNext();

            return (V)entries[keyPosition() + 1];
        }
    }

    public final class EntryIterator
        extends AbstractIterator
        implements Iterator<Entry<K, V>>, Entry<K, V>
    {
        @SuppressWarnings("unchecked")
        public K getKey()
        {
            return (K)entries[keyPosition()];
        }

        @SuppressWarnings("unchecked")
        public V getValue()
        {
            return (V)entries[keyPosition() + 1];
        }

        @SuppressWarnings("unchecked")
        public V setValue(final V value)
        {
            if (!isPositionValid)
            {
                throw new IllegalStateException();
            }

            if (null == value)
            {
                throw new IllegalArgumentException();
            }

            final int keyPosition = keyPosition();
            final Object prevValue = entries[keyPosition + 1];
            entries[keyPosition + 1] = value;
            return (V)prevValue;
        }

        public Entry<K, V> next()
        {
            findNext();

            if (shouldAvoidAllocation)
            {
                return this;
            }

            return allocateDuplicateEntry();
        }

        private Entry<K, V> allocateDuplicateEntry()
        {
            final K k = getKey();
            final V v = getValue();

            return new Entry<K, V>()
            {
                public K getKey()
                {
                    return k;
                }

                public V getValue()
                {
                    return v;
                }

                public V setValue(final V value)
                {
                    return Object2ObjectHashMap.this.put(k, value);
                }

                public int hashCode()
                {
                    return getKey().hashCode() ^ getValue().hashCode();
                }

                public boolean equals(final Object o)
                {
                    if (!(o instanceof Entry))
                    {
                        return false;
                    }

                    final Entry e = (Entry)o;

                    return (e.getKey() != null && e.getValue() != null) &&
                        (e.getKey().equals(k) && e.getValue().equals(v));
                }

                public String toString()
                {
                    return k + "=" + v;
                }
            };
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode()
        {
            return getKey().hashCode() ^ getValue().hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof Entry))
            {
                return false;
            }

            final Entry that = (Entry)o;

            return Objects.equals(getKey(), that.getKey()) && Objects.equals(getValue(), that.getValue());
        }
    }

    public final class KeySet extends AbstractSet<K> implements Serializable
    {
        private final KeyIterator keyIterator = shouldAvoidAllocation ? new KeyIterator() : null;

        /**
         * {@inheritDoc}
         */
        public KeyIterator iterator()
        {
            KeyIterator keyIterator = this.keyIterator;
            if (null == keyIterator)
            {
                keyIterator = new KeyIterator();
            }

            keyIterator.reset();
            return keyIterator;
        }

        /**
         * {@inheritDoc}
         */
        public int size()
        {
            return Object2ObjectHashMap.this.size();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isEmpty()
        {
            return Object2ObjectHashMap.this.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        public void clear()
        {
            Object2ObjectHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        public boolean contains(final Object o)
        {
            return containsKey(o);
        }
    }

    public final class Values extends AbstractCollection<V>
    {
        private final ValueIterator valueIterator = shouldAvoidAllocation ? new ValueIterator() : null;

        /**
         * {@inheritDoc}
         */
        public ValueIterator iterator()
        {
            ValueIterator valueIterator = this.valueIterator;
            if (null == valueIterator)
            {
                valueIterator = new ValueIterator();
            }

            valueIterator.reset();
            return valueIterator;
        }

        /**
         * {@inheritDoc}
         */
        public int size()
        {
            return Object2ObjectHashMap.this.size();
        }

        /**
         * {@inheritDoc}
         */
        public boolean contains(final Object o)
        {
            return containsValue(o);
        }
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> implements Serializable
    {
        private final EntryIterator entryIterator = shouldAvoidAllocation ? new EntryIterator() : null;

        /**
         * {@inheritDoc}
         */
        public EntryIterator iterator()
        {
            EntryIterator entryIterator = this.entryIterator;
            if (null == entryIterator)
            {
                entryIterator = new EntryIterator();
            }

            entryIterator.reset();
            return entryIterator;
        }

        /**
         * {@inheritDoc}
         */
        public int size()
        {
            return Object2ObjectHashMap.this.size();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isEmpty()
        {
            return Object2ObjectHashMap.this.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        public void clear()
        {
            Object2ObjectHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        public boolean contains(final Object o)
        {
            final Entry entry = (Entry)o;
            final V val = get(entry.getKey());
            return val != null && val.equals(entry.getValue());
        }
    }
}
