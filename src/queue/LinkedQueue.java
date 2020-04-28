package queue;

public class LinkedQueue extends AbstractQueue {
    private Node tail = null;
    private Node head = null;
    private int size = 0;

    @Override
    public void enqueue(Object object) {
        Node savedTail = tail;
        tail = new Node(object);

        if (size == 0) {
            head = tail;
        } else {
            savedTail.next = tail;
        }
        ++size;
    }

    @Override
    public Object element() {
        return head != null ? head.o : null;
    }

    @Override
    public Object dequeue() {
        if (size == 0) {
            return null;
        }

        Object o = element();
        head = head.next;
        --size;
        return o;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        tail = head = null;
        size = 0;
    }


    private static class Node {
        Node next = null;
        Object o;

        Node(Object o) {
            this.o = o;
        }
    }
}
