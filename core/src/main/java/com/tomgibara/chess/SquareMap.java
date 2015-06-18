package com.tomgibara.chess;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

//TODO add cons from coll.
public final class SquareMap<V> extends AbstractMap<Square, V> {

	public interface Store<V> {
		
		int size();
		
		V get(int index);
		
		default V set(int index, V value) { throw new UnsupportedOperationException(); }
		
		default void clear() { throw new UnsupportedOperationException(); }
		
		//TODO use to check calls early?
		default boolean isMutable() { return false; }
		
		default Store<V> immutable() { return isMutable() ? newImmutableStore(this) : this; }
	}

	private static <V> int countNonNulls(V[] vs) {
		int sum = 0;
		for (V v : vs) { sum++; }
		return sum;
	}
	
	private static <V> Store<V> newImmutableStore(Store<V> store) {
		return new Store<V>() {

			@Override
			public int size() {
				return store.size();
			}
			
			@Override
			public V get(int index) {
				return store.get(index);
			}
		};
	}
	
	private static class ArrayStore<V> implements Store<V> {

		private final V[] values;
		private int size;
		
		ArrayStore(V[] values) {
			this.values = values;
			size = countNonNulls(values);
		}

		ArrayStore(V[] values, int size) {
			this.values = values;
			this.size = size;
		}

		@Override
		public int size() {
			return size;
		}
		
		@Override
		public V get(int index) {
			return values[index];
		}

		@Override
		public V set(int index, V value) {
			V old = values[index];
			values[index] = value;
			if (old != null) size --;
			if (value != null) size ++;
			return old;
		}

		@Override
		public void clear() {
			Arrays.fill(values, null);
			size = 0;
		}

		@Override
		public boolean isMutable() {
			return true;
		}
		
		@Override
		public Store<V> immutable() {
			return immutableArrayStore(this);
		}
	}
	
	private static <V> Store<V> immutableArrayStore(ArrayStore<V> store) {
		return new Store<V>() {

			private final V[] values = store.values.clone();
			private final int size = store.size;

			@Override
			public int size() { return size; }

			@Override
			public V get(int index) { return values[index]; }

		};
	}

	private final Store<V> store;
	private EntrySet entrySet = null;
	private Squares squares = null;

	public SquareMap(Class<? extends V> type) {
		if (type == null) throw new IllegalArgumentException("null type");
		V[] newArray = (V[]) Array.newInstance(type, 64);
		store = new ArrayStore<V>(newArray, 0);
	}
	
	public SquareMap(Store<V> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		this.store = store;
	}
	
	// must be length 64
	SquareMap(V[] values, int size) {
		store = new ArrayStore<V>(values, size);
	}
	
	public SquareMap<V> immutable() {
		return store.isMutable() ? new SquareMap<>(store.immutable()) : this;
	}
	
	@Override
	public V get(Object key) {
		if (!(key instanceof Square)) return null;
		return get( ((Square) key).ordinal );
	}
	
	@Override
	public boolean containsKey(Object key) {
		if (!(key instanceof Square)) return false;
		return get( ((Square) key).ordinal ) != null;
	}
	
	@Override
	public boolean containsValue(Object value) {
		for (int i = 0; i < 64; i++) {
			V v = store.get(i);
			if (v != null && v.equals(value)) return true;
		}
		return false;
	}
	
	@Override
	public V put(Square key, V value) {
		if (key == null) throw new IllegalArgumentException("null key");
		if (value == null) throw new IllegalArgumentException("null value");
		return set(key.ordinal, value);
	}
	
	@Override
	public V remove(Object key) {
		if (!(key instanceof Square)) return null;
		return set(((Square) key).ordinal, null);
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		if (value == null) return false;
		if (!(key instanceof Square)) return false;
		Square sqr = (Square) key;
		Object old = get(sqr.ordinal);
		if (old == null || !old.equals(value)) return false;
		set(sqr.ordinal, null);
		return true;
	}
	
	@Override
	public void forEach(BiConsumer<? super Square, ? super V> action) {
		for (int i = 0; i < 64; i++) {
			V v = get(i);
			if (v != null) action.accept(Square.at(i), v);
		}
	}
	
	@Override
	public int size() {
		return store.size();
	}
	
	@Override
	public boolean isEmpty() {
		return store.size() == 0;
	}
	
	@Override
	public void clear() {
		store.clear();
	}
	
	@Override
	//TODO can we support mutability?
	public Squares keySet() {
		if (store.isMutable()) return squares();
		if (squares == null) squares = squares();
		return squares;
	}
	
	@Override
	public Set<Map.Entry<Square, V>> entrySet() {
		return entrySet == null ? entrySet = new EntrySet() : entrySet;
	}

	private Squares squares() {
		long squares = 0L;
		for (int i = 0; i < 64; i++) {
			if (get(i) != null) squares |= 1L << i;
		}
		return new Squares(squares);

	}

	private V get(int ordinal) {
		return store.get(ordinal);
	}
	
	private V set(int ordinal, V value) {
		return store.set(ordinal, value);
	}
	
	private class EntrySet extends AbstractSet<Map.Entry<Square, V>> {

		@Override
		public Iterator<Map.Entry<Square, V>> iterator() {
			return new Iterator<Map.Entry<Square,V>>() {
				
				private final Iterator<Square> i = keySet().iterator();
				private Entry previous = null;
				
				@Override
				public boolean hasNext() {
					return i.hasNext();
				}
				
				@Override
				public Map.Entry<Square, V> next() {
					return new Entry(i.next());
				}
				
				@Override
				public void remove() {
					if (previous == null) throw new IllegalStateException();
					previous.remove();
					previous = null;
				}
				
			};
		}
		
		@Override
		public boolean contains(Object o) {
			if (!(o instanceof SquareMap.Entry)) return false;
			return get(((Entry) o).getKey().ordinal) != null;
		}
		
		@Override
		public boolean remove(Object o) {
			if (!(o instanceof SquareMap.Entry)) return false;
			((Entry) o).remove();
			return true;
		}

		@Override
		public void clear() {
			SquareMap.this.clear();
		}
		
		@Override
		public int size() {
			return SquareMap.this.size();
		}
		
	}
	
	private class Entry implements Map.Entry<Square, V> {

		final Square key;
		
		Entry(Square key) {
			this.key = key;
		}
		
		@Override
		public Square getKey() {
			return key;
		}
		
		@Override
		public V getValue() {
			V value = get(key.ordinal);
			if (value == null) throw new IllegalStateException("removed");
			return value;
		}

		@Override
		public V setValue(V value) {
			if (value == null) throw new IllegalArgumentException("null value");
			return set(key.ordinal, value);
		}
		
		public boolean equals(Object o) {
			if (o == this) return true;
			if (!(o instanceof SquareMap.Entry)) return false;
			Entry that = (Entry) o;
			return this.getMap() == that.getMap() && this.key == that.key;
		}
		
		@Override
		public int hashCode() {
			return key.hashCode() ^ Objects.hashCode(get(key.ordinal));
		}
		
		private SquareMap<V> getMap() {
			return SquareMap.this;
		}
		
		void remove() {
			set(key.ordinal, null);
		}

	}
	
	
}