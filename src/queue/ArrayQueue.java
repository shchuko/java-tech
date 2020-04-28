package queue;

public class ArrayQueue extends AbstractQueue {
    private static final int DEFAULT_CAPACITANCE = 100;
    private static final int DEFAULT_CAPACITANCE_INCREASE_FACTOR = 2;

    private int capacitance;
    private Object[] data;
    private int head = 0;
    private int tail = 0;

    public ArrayQueue() {
        this.capacitance = DEFAULT_CAPACITANCE;
        data = new Object[capacitance];
    }

    public ArrayQueue(int capacitance) {
        this.capacitance = capacitance;
        data = new Object[capacitance];
    }

    @Override
    public void enqueue(Object object) {
        if (size() == capacitance - 1) {
            increaseCapacitance();
        }

        data[tail] = object;
        tail = (tail + 1) % capacitance;
    }

    @Override
    public Object element() {
        if (super.isEmpty()) {
            return null;
        }

        return data[head];
    }

    @Override
    public Object dequeue() {
        if (super.isEmpty()) {
            return null;
        }

        Object result = element();
        head = (head + 1) % capacitance;
        return result;
    }

    @Override
    public int size() {
        return head > tail ? capacitance - head + tail : tail - head;
    }

    @Override
    public void clear() {
        head = tail = 0;
    }

    private void increaseCapacitance() {
        int newCapacitance = capacitance * ArrayQueue.DEFAULT_CAPACITANCE_INCREASE_FACTOR;
        int savedSize = size();

        Object[] newData = new Object[newCapacitance];

        System.arraycopy(data, head, newData, 0, data.length - head);
        System.arraycopy(data, 0, newData, data.length - head, tail + 1);

        data = newData;
        head = 0;
        tail = savedSize;
        capacitance = newCapacitance;
    }
}
