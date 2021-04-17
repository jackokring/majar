package uk.co.kring.generic;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

public abstract class BulkStream<E> implements Stream<E>, Iterable<E> {

    @Override
    public Stream<E> filter(Predicate<? super E> predicate) {
        return null;
    }

    @Override
    public <R> Stream<R> map(Function<? super E, ? extends R> function) {
        return null;
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super E> toIntFunction) {
        return null;
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super E> toLongFunction) {
        return null;
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super E> toDoubleFunction) {
        return null;
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> function) {
        return null;
    }

    @Override
    public IntStream flatMapToInt(Function<? super E, ? extends IntStream> function) {
        return null;
    }

    @Override
    public LongStream flatMapToLong(Function<? super E, ? extends LongStream> function) {
        return null;
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> function) {
        return null;
    }

    @Override
    public Stream<E> distinct() {
        return null;
    }

    @Override
    public Stream<E> sorted() {
        return null;
    }

    @Override
    public Stream<E> sorted(Comparator<? super E> comparator) {
        return null;
    }

    @Override
    public Stream<E> peek(Consumer<? super E> consumer) {
        return null;
    }

    @Override
    public Stream<E> limit(long l) {
        return null;
    }

    @Override
    public Stream<E> skip(long l) {
        return null;
    }

    @Override
    public void forEach(Consumer<? super E> consumer) {

    }

    @Override
    public void forEachOrdered(Consumer<? super E> consumer) {

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
    public E reduce(E e, BinaryOperator<E> binaryOperator) {
        return null;
    }

    @Override
    public Optional<E> reduce(BinaryOperator<E> binaryOperator) {
        return Optional.empty();
    }

    @Override
    public <U> U reduce(U u, BiFunction<U, ? super E, U> biFunction, BinaryOperator<U> binaryOperator) {
        return null;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> biConsumer, BiConsumer<R, R> biConsumer1) {
        return null;
    }

    @Override
    public <R, A> R collect(Collector<? super E, A, R> collector) {
        return null;
    }

    @Override
    public Optional<E> min(Comparator<? super E> comparator) {
        return Optional.empty();
    }

    @Override
    public Optional<E> max(Comparator<? super E> comparator) {
        return Optional.empty();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public boolean anyMatch(Predicate<? super E> predicate) {
        return false;
    }

    @Override
    public boolean allMatch(Predicate<? super E> predicate) {
        return false;
    }

    @Override
    public boolean noneMatch(Predicate<? super E> predicate) {
        return false;
    }

    @Override
    public Optional<E> findFirst() {
        return Optional.empty();
    }

    @Override
    public Optional<E> findAny() {
        return Optional.empty();
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public Spliterator<E> spliterator() {
        return null;
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public Stream<E> sequential() {
        return null;
    }

    @Override
    public Stream<E> parallel() {
        return null;
    }

    @Override
    public Stream<E> unordered() {
        return null;
    }

    @Override
    public Stream<E> onClose(Runnable runnable) {
        return null;
    }

    @Override
    public void close() {

    }
}
