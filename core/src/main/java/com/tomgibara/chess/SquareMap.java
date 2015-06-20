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

public class SquareMap<V> extends AbstractMap<Square, V> {

	public interface Store<V> {
		
		Class<? extends V> valueType();
		
		int size();
		
		V get(int index);
		
		default V set(int index, V value) { throw new UnsupportedOperationException(); }
		
		default void clear() { throw new UnsupportedOperationException(); }
		
		//TODO use to check calls early?
		default boolean isMutable() { return false; }
		
		default Store<V> immutable() { return isMutable() ? newImmutableStore(this) : this; }
		
		default Store<V> mutableCopy() {
			V[] vs = (V[]) Array.newInstance(valueType(), 64);
			for (int i = 0; i < 64; i++) {
				vs[i] = get(i);
			}
			return new ArrayStore<V>(vs, size());
		}
	}

	private static <V> int countNonNulls(V[] vs) {
		int sum = 0;
		for (V v : vs) { sum++; }
		return sum;
	}
	
	private static Squares squares(Store<?> store) {
		// common special case
		if (store.size() == 0) {
			return store.isMutable() ? new MutableSquares() : Squares.empty();
		}
		
		long squares = 0L;
		for (int i = 0; i < 64; i++) {
			if (store.get(i) != null) squares |= 1L << i;
		}
		return store.isMutable() ? new MutableSquares(squares) : new Squares(squares);
	}

	private static <V> Store<V> newImmutableStore(Store<V> store) {
		return new Store<V>() {
			
			@Override
			public Class<? extends V> valueType() {
				return store.valueType();
			}

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
		public Class<? extends V> valueType() {
			return (Class<? extends V>) values.getClass().getComponentType();
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
			return new ImmutableArrayStore<V>(this);
		}
		
		@Override
		public Store<V> mutableCopy() {
			return new ArrayStore<V>(values.clone(), size);
		}
	}
	
	private static final class ImmutableArrayStore<V> implements Store<V> {

		private final V[] values;
		private final int size;

		ImmutableArrayStore(ArrayStore<V> store) {
			values = store.values.clone();
			size = store.size;
		}

		@Override
		public Class<? extends V> valueType() {
			return (Class<? extends V>) values.getClass().getComponentType();
		}

		@Override
		public int size() { return size; }

		@Override
		public V get(int index) { return values[index]; }

		@Override
		public Store<V> mutableCopy() { return new ArrayStore<V>(values.clone(), size); }
		
	}
	
	private final Store<V> store;
	private EntrySet entrySet = null;
	private final Squares squares;

	public SquareMap(Class<? extends V> type) {
		if (type == null) throw new IllegalArgumentException("null type");
		V[] newArray = (V[]) Array.newInstance(type, 64);
		store = new ArrayStore<V>(newArray, 0);
		squares = squares(store);
	}
	
	public SquareMap(Store<V> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		this.store = store;
		squares = squares(store);
	}
	
	// must be length 64
	SquareMap(V[] values, int size) {
		store = new ArrayStore<V>(values, size);
		squares = squares(store);
	}
	
	// must be length 64
	SquareMap(V[] values) {
		store = new ArrayStore<V>(values);
		squares = squares(store);
	}

	public int[] partitionSizes() {
		int[] counts = new int[enumSize()];
		for (int i = 0; i < 64; i++) {
			V v = store.get(i);
			if (v == null) continue;
			counts[((Enum) v).ordinal()]++;
		}
		return counts;
	}
	
	public Squares[] partition() {
		int length = enumSize();
		Squares[] partition = new Squares[length];
		for (int i = 0; i < length; i++) {
			partition[i] = new MutableSquares();
		}
		for (int i = 0; i < 64; i++) {
			V v = store.get(i);
			if (v == null) continue;
			partition[ ((Enum<?>)store.get(i)).ordinal() ].add(Square.at(i));
		}
		for (int i = 0; i < length; i++) {
			partition[i] = Squares.immutable( partition[i] );
		}
		return partition;
	}
	
	public SquareMap<V> immutable() {
		return store.isMutable() ? newInstance(store.immutable()) : this;
	}
	
	public SquareMap<V> mutableCopy() {
		return newInstance(store.mutableCopy());
	}
	
	@Override
	public final V get(Object key) {
		if (!(key instanceof Square)) return null;
		Square sqr = (Square) key;
		return get(sqr.ordinal);
	}
	
	@Override
	public final boolean containsKey(Object key) {
		if (!(key instanceof Square)) return false;
		return get( ((Square) key).ordinal ) != null;
	}
	
	@Override
	public final boolean containsValue(Object value) {
		for (int i = 0; i < 64; i++) {
			V v = store.get(i);
			if (v != null && v.equals(value)) return true;
		}
		return false;
	}
	
	@Override
	public final V put(Square key, V value) {
		if (key == null) throw new IllegalArgumentException("null key");
		if (value == null) throw new IllegalArgumentException("null value");
		return set(key.ordinal, value);
	}
	
	@Override
	public final V remove(Object key) {
		if (!(key instanceof Square)) return null;
		return set(((Square) key).ordinal, null);
	}
	
	@Override
	public final boolean remove(Object key, Object value) {
		if (value == null) return false;
		if (!(key instanceof Square)) return false;
		Square sqr = (Square) key;
		Object old = get(sqr.ordinal);
		if (old == null || !old.equals(value)) return false;
		set(sqr.ordinal, null);
		return true;
	}
	
	@Override
	public final void forEach(BiConsumer<? super Square, ? super V> action) {
		long bits = squares.mask();
		for (int ordinal = 0; ordinal < 64 && bits != 0; ordinal++, bits >>>= 1) {
			if ((bits & 1L) == 1L) {
				action.accept(Square.at(ordinal), get(ordinal));
			}
		}
	}
	
	@Override
	public final int size() {
		return store.size();
	}
	
	@Override
	public final boolean isEmpty() {
		return store.size() == 0;
	}
	
	@Override
	public final void clear() {
		store.clear();
	}
	
	@Override
	public final Squares keySet() {
		return squares;
	}
	
	@Override
	public final Set<Map.Entry<Square, V>> entrySet() {
		return entrySet == null ? entrySet = new EntrySet() : entrySet;
	}
	
	//TODO implement efficient values()?

	@Override
	public final boolean equals(Object o) {
		if (o == this) return true;
		// optimize for comparison with other square maps - a common case
		if (o instanceof SquareMap<?>) {
			SquareMap<?> that = (SquareMap<?>) o;
			if (this.store.valueType() != that.store.valueType()) return false;
			if (this.squares.mask() != that.squares.mask()) return false;
			for (int i = 0; i < 64; i++) {
				if (!Objects.equals(this.store.get(i), that.store.get(i))) {
					return false;
				}
			}
			return true;
		}
		return super.equals(o);
	}

	//TODO yuck
	// want a specific piece map extension - replaces Arrangement?
	public Squares[] colourPartition() {
		if (store.valueType() != Piece.class) throw new IllegalStateException();
		Squares[] partition = new Squares[2];
		partition[0] = new MutableSquares();
		partition[1] = new MutableSquares();
		for (int i = 0; i < 64; i++) {
			V v = store.get(i);
			if (v == null) continue;
			Piece piece = (Piece) store.get(i);
			partition[ piece.colour.ordinal() ].add(Square.at(i));
		}
		partition[0] = Squares.immutable( partition[0] );
		partition[1] = Squares.immutable( partition[1] );
		return partition;
	}
	
	SquareMap<V> newInstance(Store<V> store) {
		return new SquareMap<V>(store);
	}
	
	private V get(int ordinal) {
		if (store.isMutable() && !squares.contains(ordinal)) return null;
		return store.get(ordinal);
	}
	
	private V set(int ordinal, V value) {
		V old = store.set(ordinal, value);
		if ((old == null) != (value == null)) {
			MutableSquares squares = (MutableSquares) this.squares;
			if (value == null) {
				squares.remove(ordinal);
			} else {
				squares.add(ordinal);
			}
		}
		return old;
	}
	
	private int enumSize() {
		Class<?> type = store.valueType();
		if (type == Piece.class) return Piece.COUNT;
		if (!type.isEnum()) throw new IllegalStateException();
		return type.getEnumConstants().length;
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
