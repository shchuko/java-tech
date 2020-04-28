package queue;

public abstract class AbstractQueue implements Queue {
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Object[] toArray() {
        int size = size();
        Object[] result = new Object[size];

        int i = 0;
        while (size != 0) {
            Object element = dequeue();
            result[i] = element;
            size--;
            ++i;
        }

        for (Object o : result) {
            enqueue(o);
        }
        return result;
    }

}
