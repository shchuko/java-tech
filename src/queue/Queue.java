package queue;

public interface Queue {

    void enqueue(Object object);
    Object element();
    Object dequeue();
    int size();
    boolean isEmpty();
    void clear();

    Object[] toArray();
}
