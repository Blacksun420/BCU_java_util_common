package common.pack;

import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unchecked")
public class SortedPackSet<T extends Comparable<? super T>> implements Set<T>, Cloneable, java.io.Serializable {

    private class Itr implements Iterator<T> {
        private int ind = 0;
        @Override
        public boolean hasNext() {
            return ind < size;
        }
        @Override
        public T next() {
            return (T)arr[ind++];
        }
    }

    public Object[] arr;
    public int size = 0;

    public SortedPackSet() {
        this(1);
    }

    public SortedPackSet(int siz) {
        arr = new Object[siz];
    }

    public SortedPackSet(T t) {
        arr = new Object[]{t};
    }

    public SortedPackSet(Collection<T> col) {
        arr = new Object[Math.max(col.size(), 1)];
        addAll(col);
    }

    @Override
    public int size() {
        return size;
    }

    public int capacity() {
        return arr.length;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    public int indexOf(Object o) {
        if (size == 0)
            return -1;

        int f = 0, l = size;
        while (f < l) {
            int mid = f + (l - f) / 2;
            if (arr[mid] == o) {
                return mid;
            } else {
                int c = ((Comparable<T>)o).compareTo((T)arr[mid]);
                if (c > 0)
                    f = mid + 1;
                else if (c < 0)
                    l = mid - 1;
            }
        }
        return -1;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @Override
    public Itr iterator() {
        return new Itr();
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(arr, size);
    }

    @NotNull
    @Override
    public<R> R[] toArray(@NotNull R[] a) {
        return (R[]) Arrays.copyOf(arr, size, a.getClass());
    }

    public void sort() {
        Object[] narr = new Object[size];
        System.arraycopy(arr, 0, narr, 0, size);
        Arrays.sort(narr);
        System.arraycopy(narr, 0, arr, 0, size);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void sort(Comparator<? super T> c) {
        Object[] a = toArray();
        Arrays.sort(a, (Comparator) c);
        System.arraycopy(a, 0, arr, 0, size);
    }

    @Override
    public boolean add(T t) {
        if (contains(t))
            return false;
        if (size == arr.length)
            arr = Arrays.copyOf(arr, arr.length * 2);

        arr[size++] = t;
        if (size > 1 && t.compareTo(get(size - 2)) < 0)
            sort();

        return true;
    }

    public void set(int ind, T t) {
        remove(ind);
        add(t);
    }

    public T get(int ind) {
        return (T)arr[ind];
    }

    @Override
    public boolean remove(Object o) {
        int ind = indexOf(o);
        if (ind == -1)
            return false;
        remove(ind);
        return true;
    }

    public void remove(int ind) {
        if (ind >= size || ind < 0)
            throw new ArrayIndexOutOfBoundsException("Index:" + ind + ", Size:" + size);

        int mov = size - 1 - ind;
        if (mov >= 0)
            System.arraycopy(arr, ind + 1, arr, ind, mov);
        arr[--size] = null;
    }

    @Override
    public boolean removeAll(@NotNull Collection c) {
        boolean rem = false;
        for (T t : (Iterable<T>) c) rem |= remove(t);
        return rem;
    }

    @Override
    public boolean addAll(Collection c) {
        if (c.size() + size >= arr.length) {
            int mul = 1;
            while (++mul * arr.length <= c.size() + size);

            arr = Arrays.copyOf(arr, arr.length * mul);
        }
        boolean ch = false;
        for (Object elem : c) {
            if (contains(elem))
                continue;
            ch = true;

            arr[size++] = elem;
        }
        if (ch)
            sort();
        return ch;
    }

    @Override
    public void clear() {
        while (!isEmpty())
            remove(0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Set))
            return false;
        Set<?> c = (Set<?>) o;
        if (c.size() != size)
            return false;

        try {
            return containsAll(c);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (T obj : this)
            if (obj != null)
                h += obj.hashCode();

        return h;
    }

    /**
     * Returns a list containing only the elements these 2 lists both contain
     * @param col The other list
     * @return A list containing only the elements these 2 lists both contain
     */
    public SortedPackSet<T> inCommon(Collection<T> col) {
        SortedPackSet<T> np = new SortedPackSet<>();
        for (T item : col) {
            if (contains(item))
                np.add(item);
            if (np.size() == size)
                break;
        }

        return np;
    }

    @Override
    public boolean retainAll(@NotNull Collection c) {
        boolean s = false;
        for (int i = 0; i < size; i++)
            if (!c.contains(arr[i])) {
                remove(i);
                i--;
                s = true;
            }
        return s;
    }

    @Override
    public boolean containsAll(@NotNull Collection c) {
        for (Object o : c)
            if (!contains(o))
                return false;
        return true;
    }

    @Override
    public SortedPackSet<T> clone() {
        return new SortedPackSet<>(this);
    }
}
