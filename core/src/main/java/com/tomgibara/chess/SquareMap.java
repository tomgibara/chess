package com.tomgibara.chess;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import com.tomgibara.storage.Store;
import com.tomgibara.storage.Stores;

public class SquareMap<V> extends AbstractMap<Square, V> {

	private static Squares squares(Store<?> store) {
		// common special case
		if (store.count() == 0) {
			return store.isMutable() ? new MutableSquares() : Squares.empty();
		}

		long squares = store.population().getBits(0, 64) ;
		return store.isMutable() ? new MutableSquares(squares) : new Squares(squares);
	}

	private final Store<V> store;
	private EntrySet entrySet = null;
	private final Squares squares;

	public SquareMap(Store<V> store) {
		if (store == null) throw new IllegalArgumentException("null store");
		this.store = store;
		squares = squares(store);
	}
	
	// must be length 64
	SquareMap(V[] values, int count) {
		store = Stores.objectsAndNullCount(count, values);
		squares = squares(store);
	}
	
	// must be length 64
	SquareMap(V[] values) {
		store = Stores.objects(values);
		squares = squares(store);
	}

	public int[] partitionSizes() {
		int[] counts = new int[enumSize()];
		for (int i = 0; i < 64; i++) {
			V v = store.get(i);
			if (v == null) continue;
			counts[((Enum<?>) v).ordinal()]++;
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
		return store.count();
	}
	
	@Override
	public final boolean isEmpty() {
		return store.count() == 0;
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
