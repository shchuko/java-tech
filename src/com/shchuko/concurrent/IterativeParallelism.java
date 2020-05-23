package com.shchuko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ScalarIP {
    ParallelMapper parallelMapper;

    public IterativeParallelism() {

    }

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @return maximum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if not values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        Function<List<? extends T>, T> function = maxList -> maxList.stream().max(comparator).get();
        return function.apply(runJob(threads, values, function));
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @return minimum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if not values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    /**
     * Returns whether all values satisfies predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether all values satisfies predicate or {@code true}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, Boolean> function = val -> val.stream().allMatch(predicate);
        return runJob(threads, values, function).stream().allMatch(val -> val);
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private <T> List<List<? extends T>> splitValues(int threads, List<? extends T> values) {
        if (threads < 1) {
            throw new IllegalArgumentException("Illegal threads number " + threads);
        }

        int chunkSize = threads < values.size() ? values.size() / threads : 1;
        List<List<? extends T>> result = new ArrayList<>();

        for (int chunkBegin = 0; chunkBegin < values.size(); chunkBegin += chunkSize) {
            int chunkEnd = Math.min(chunkBegin + chunkSize, values.size());
            result.add(values.subList(chunkBegin, chunkEnd));
        }

        return result;
    }

    private <T, R> List<R> runJob(int threadCount, List<? extends T> values, Function<List<? extends T>, R> function) throws InterruptedException {
        List<List<? extends T>> jobsInput = splitValues(threadCount, values);

        if (parallelMapper != null) {
            return parallelMapper.map(function, jobsInput);
        }

        List<ListWorker<T, R>> workers = jobsInput.stream().map(val -> new ListWorker<>(function, val)).collect(Collectors.toList());
        List<Thread> threads = workers.stream().map(Thread::new).collect(Collectors.toList());
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        return workers.stream().map(ListWorker::getResult).collect(Collectors.toList());
    }

    static class ListWorker<T, R> implements Runnable {
        private final Function<List<? extends T>, R> function;
        private final List<? extends T> list;
        private R result;

        public ListWorker(Function<List<? extends T>, R> function, List<? extends T> list) {
            this.function = function;
            this.list = list;
        }

        @Override
        public void run() {
            result = function.apply(list);
        }

        public R getResult() {
            return result;
        }
    }
}
