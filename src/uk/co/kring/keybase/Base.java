package uk.co.kring.keybase;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * The bulk stream for database processing data.
 */
public abstract class Base extends Key implements Stream<Key>, Iterable<Key> {

    Store store;

    /**
     * Write an operator to the base.
     * @param op the operator.
     */
    public synchronized void write(Operator<? extends Key> op) {

    }

    @Override
    public Stream<Key> filter(Predicate<? super Key> predicate) {
        return null;
    }

    @Override
    public <R> Stream<R> map(Function<? super Key, ? extends R> function) {
        return null;
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super Key> toIntFunction) {
        return null;
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super Key> toLongFunction) {
        return null;
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super Key> toDoubleFunction) {
        return null;
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super Key, ? extends Stream<? extends R>> function) {
        return null;
    }

    @Override
    public IntStream flatMapToInt(Function<? super Key, ? extends IntStream> function) {
        return null;
    }

    @Override
    public LongStream flatMapToLong(Function<? super Key, ? extends LongStream> function) {
        return null;
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super Key, ? extends DoubleStream> function) {
        return null;
    }

    @Override
    public Stream<Key> distinct() {
        return null;
    }

    @Override
    public Stream<Key> sorted() {
        return null;
    }

    @Override
    public Stream<Key> sorted(Comparator<? super Key> comparator) {
        return null;
    }

    @Override
    public Stream<Key> peek(Consumer<? super Key> consumer) {
        return null;
    }

    @Override
    public Stream<Key> limit(long l) {
        return null;
    }

    @Override
    public Stream<Key> skip(long l) {
        return null;
    }

    @Override
    public void forEach(Consumer<? super Key> consumer) {

    }

    @Override
    public void forEachOrdered(Consumer<? super Key> consumer) {

    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> intFunction) {
        return null;
    }

    @Override
    public Key reduce(Key key, BinaryOperator<Key> binaryOperator) {
        return null;
    }

    @Override
    public Optional<Key> reduce(BinaryOperator<Key> binaryOperator) {
        return Optional.empty();
    }

    @Override
    public <U> U reduce(U u, BiFunction<U, ? super Key, U> biFunction, BinaryOperator<U> binaryOperator) {
        return null;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Key> biConsumer, BiConsumer<R, R> biConsumer1) {
        return null;
    }

    @Override
    public <R, A> R collect(Collector<? super Key, A, R> collector) {
        return null;
    }

    @Override
    public Optional<Key> min(Comparator<? super Key> comparator) {
        return Optional.empty();
    }

    @Override
    public Optional<Key> max(Comparator<? super Key> comparator) {
        return Optional.empty();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public boolean anyMatch(Predicate<? super Key> predicate) {
        return false;
    }

    @Override
    public boolean allMatch(Predicate<? super Key> predicate) {
        return false;
    }

    @Override
    public boolean noneMatch(Predicate<? super Key> predicate) {
        return false;
    }

    @Override
    public Optional<Key> findFirst() {
        return Optional.empty();
    }

    @Override
    public Optional<Key> findAny() {
        return Optional.empty();
    }

    @Override
    public Iterator<Key> iterator() {
        return null;
    }

    @Override
    public Spliterator<Key> spliterator() {
        return null;
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public Stream<Key> sequential() {
        return null;
    }

    @Override
    public Stream<Key> parallel() {
        return null;
    }

    @Override
    public Stream<Key> unordered() {
        return null;
    }

    @Override
    public Stream<Key> onClose(Runnable runnable) {
        return null;
    }

    @Override
    public void close() {

    }
}
