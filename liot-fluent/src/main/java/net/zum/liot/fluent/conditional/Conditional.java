package net.zum.liot.fluent.conditional;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/// A container object which may or may not contain a non-`null` value.
///
/// Mirrors the public API of [java.util.Optional] and extends it with chained side effects and
/// in-chain conditions:
///
/// ```java
/// Conditional.from(myOptional)
///         .filter(myPredicate)
///         .condition(myCondition)
///             .ifTrue(myTrueConsumer)
///             .ifFalse(myFalseConsumer)
///         .map(myFunction)
///         .orElseThrow(MyException::new);
/// ```
///
/// All methods except [#isPresent()] and [#get()] are implemented as default methods on top of
/// those two.
///
/// The hierarchy is sealed: implementations are provided by this package only, which keeps
/// `equals`/`hashCode` consistent across them.
///
/// @param <T> the type of value
public sealed interface Conditional<T>
    permits ConditionalImpl, Conditional.EmptyConditional, ConditionalWithCondition {

  // ---------------------------------------------------------------------
  // Static factories
  // ---------------------------------------------------------------------

  /// Returns an empty `Conditional` instance.
  ///
  /// @param <T> the type of the non-existent value
  /// @return an empty `Conditional`
  @SuppressWarnings("unchecked")
  static <T> Conditional<T> empty() {
    return (Conditional<T>) EmptyConditional.instance();
  }

  /// Returns a `Conditional` describing the given non-`null` value.
  ///
  /// @param value the value to describe, which must be non-`null`
  /// @param <T> the type of the value
  /// @return a `Conditional` with the value present
  /// @throws NullPointerException if value is `null`
  static <T> Conditional<T> of(T value) {
    return new ConditionalImpl<>(value);
  }

  /// Returns a `Conditional` describing the given value if non-`null`, otherwise an empty
  /// `Conditional`.
  ///
  /// @param value the possibly-`null` value to describe
  /// @param <T> the type of the value
  /// @return a `Conditional` with a present value if the specified value is non-`null`, otherwise
  ///   an empty `Conditional`
  static <T> Conditional<T> ofNullable(T value) {
    return Objects.isNull(value) ? empty() : of(value);
  }

  /// Returns a `Conditional` with the same content as the given [Optional].
  ///
  /// @param optional the `Optional` to convert, which must be non-`null`
  /// @param <T> the type of the value
  /// @return a `Conditional` describing the value of `optional`, if present, otherwise an empty
  ///   `Conditional`
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  static <T> Conditional<T> ofOptional(Optional<? extends T> optional) {
    return ofNullable(optional.orElse(null));
  }

  // ---------------------------------------------------------------------
  // Primitives (the only abstract methods)
  // ---------------------------------------------------------------------

  /// @return `true` if a value is present, otherwise `false`
  boolean isPresent();

  /// If a value is present, returns the value, otherwise throws [NoSuchElementException].
  ///
  /// The preferred alternative to this method is [#orElseThrow()].
  ///
  /// @return the non-`null` value described by this `Conditional`
  /// @throws NoSuchElementException if no value is present
  T get();

  // ---------------------------------------------------------------------
  // Queries
  // ---------------------------------------------------------------------

  /// @return `true` if a value is not present, otherwise `false`
  default boolean isEmpty() {
    return !isPresent();
  }

  // ---------------------------------------------------------------------
  // Chained side effects
  // ---------------------------------------------------------------------

  /// If a value is present, performs the given action with the value.
  ///
  /// @param action the action to be performed, if a value is present
  /// @return this `Conditional`, for chaining
  /// @throws NullPointerException if a value is present and the given action is `null`
  default Conditional<T> ifPresent(Consumer<? super T> action) {
    if (isPresent()) {
      action.accept(get());
    }
    return this;
  }

  /// If a value is present, performs the given action with the value, otherwise performs the given
  /// empty-based action.
  ///
  /// @param action the action to be performed, if a value is present
  /// @param emptyAction the action to be performed, if no value is present
  /// @return this `Conditional`, for chaining
  /// @throws NullPointerException if the action that would run is `null`
  default Conditional<T> ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
    if (isPresent()) {
      action.accept(get());
    } else {
      emptyAction.run();
    }
    return this;
  }

  /// If no value is present, performs the given action.
  ///
  /// @param action the action to be performed, if no value is present
  /// @return this `Conditional`, for chaining
  /// @throws NullPointerException if no value is present and the given action is `null`
  default Conditional<T> ifEmpty(Runnable action) {
    if (isEmpty()) {
      action.run();
    }
    return this;
  }

  // ---------------------------------------------------------------------
  // Condition
  // ---------------------------------------------------------------------

  /// Evaluates the given predicate against the value, if present, and returns a
  /// [ConditionalWithCondition] carrying the result.
  ///
  /// Unlike [#filter], the value is kept regardless of the outcome. The predicate is evaluated
  /// exactly once, immediately. If no value is present, it is not evaluated at all and neither
  /// `ifTrue` nor `ifFalse` will run.
  ///
  /// Calling `condition` again replaces the previous condition.
  ///
  /// @param predicate the condition to evaluate
  /// @return a `ConditionalWithCondition` with the same value
  /// @throws NullPointerException if the predicate is `null`
  default ConditionalWithCondition<T> condition(@NonNull Predicate<? super T> predicate) {
    return new ConditionalWithCondition<>(get(), predicate);
  }

  // ---------------------------------------------------------------------
  // Transformations (these drop any condition)
  // ---------------------------------------------------------------------

  /// If a value is present and matches the given predicate, returns a `Conditional` describing the
  /// value, otherwise an empty `Conditional`.
  ///
  /// @param predicate the predicate to apply to a value, if present
  /// @return a `Conditional` describing the value if present and matching the predicate, otherwise
  ///   an empty `Conditional`
  /// @throws NullPointerException if the predicate is `null`
  default Conditional<T> filter(@NonNull Predicate<? super T> predicate) {
    if (isEmpty()) {
      return empty();
    }
    return predicate.test(get()) ? this : empty();
  }

  /// If a value is present, returns a `Conditional` describing (as if by [#ofNullable]) the result
  /// of applying the given mapping function to the value, otherwise an empty `Conditional`.
  ///
  /// @param mapper the mapping function to apply to a value, if present
  /// @param <U> the type of the value returned from the mapping function
  /// @return a `Conditional` describing the mapped result, if present
  /// @throws NullPointerException if the mapping function is `null`
  default <U> Conditional<U> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper, "mapper");
    return isPresent() ? ofNullable(mapper.apply(get())) : empty();
  }

  /// If a value is present, returns the result of applying the given `Conditional`-bearing mapping
  /// function to the value, otherwise an empty `Conditional`.
  ///
  /// @param mapper the mapping function to apply to a value, if present
  /// @param <U> the type of value of the `Conditional` returned by the mapping function
  /// @return the result of applying the mapping function, if present
  /// @throws NullPointerException if the mapper is `null` or returns `null`
  @SuppressWarnings("unchecked")
  default <U> Conditional<U> flatMap(
      @NonNull Function<? super T, ? extends Conditional<? extends U>> mapper) {
    if (isEmpty()) {
      return empty();
    }
    return (Conditional<U>) Objects.requireNonNull(mapper.apply(get()));
  }

  /// If a value is present, returns a `Conditional` describing the value, otherwise returns a
  /// `Conditional` produced by the supplying function.
  ///
  /// @param supplier the supplying function that produces a `Conditional` to be returned
  /// @return a `Conditional` describing the value of this `Conditional`, if present, otherwise one
  ///   produced by the supplier
  /// @throws NullPointerException if the supplier is `null` or returns `null`
  @SuppressWarnings("unchecked")
  default Conditional<T> or(@NonNull Supplier<? extends Conditional<? extends T>> supplier) {
    if (isPresent()) {
      return this;
    }
    return (Conditional<T>) Objects.requireNonNull(supplier.get());
  }

  /// @param other alternative value
  /// @return this conditional if present, otherwise a conditional describing the alternative value
  default Conditional<T> alt(T other) {
    return isPresent() ? this : Conditional.ofNullable(other);
  }

  /// If a value is present, returns a sequential [Stream] containing only that value, otherwise an
  /// empty `Stream`.
  ///
  /// @return the value as a `Stream`
  default Stream<T> stream() {
    return isPresent() ? Stream.of(get()) : Stream.empty();
  }

  // ---------------------------------------------------------------------
  // Extraction
  // ---------------------------------------------------------------------

  /// @param other the value to be returned if no value is present; may be `null`
  /// @return the value, if present, otherwise `other`
  default T orElse(T other) {
    return isPresent() ? get() : other;
  }

  /// @param supplier the supplying function that produces a value to be returned
  /// @return the value, if present, otherwise the result produced by the supplying function
  /// @throws NullPointerException if no value is present and the supplying function is `null`
  default T orElseGet(Supplier<? extends T> supplier) {
    return isPresent() ? get() : supplier.get();
  }

  /// If a value is present, returns the value, otherwise throws [NoSuchElementException].
  ///
  /// @return the non-`null` value described by this `Conditional`
  /// @throws NoSuchElementException if no value is present
  default T orElseThrow() {
    return get();
  }

  /// If a value is present, returns the value, otherwise throws an exception produced by the
  /// exception-supplying function.
  ///
  /// @param exceptionSupplier the supplying function that produces an exception to be thrown
  /// @param <X> type of the exception to be thrown
  /// @return the value, if present
  /// @throws X if no value is present
  /// @throws NullPointerException if no value is present and the exception-supplying function is
  ///   `null`
  default <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (isPresent()) {
      return get();
    }
    throw exceptionSupplier.get();
  }

  /// Converts this `Conditional` to an [Optional] with the same content.
  ///
  /// @return an `Optional` describing the value, if present, otherwise an empty `Optional`
  default Optional<T> toOptional() {
    return isPresent() ? Optional.of(get()) : Optional.empty();
  }

  // ---------------------------------------------------------------------
  // Object contract
  // ---------------------------------------------------------------------

  /// Two `Conditional`s are equal if both are empty, or both have a value present and the values
  /// are equal via `equals()`. Whether a condition is attached does not affect equality.
  @Override
  boolean equals(Object obj);

  /// @return the hash code of the value, if present, otherwise `0`
  @Override
  int hashCode();

  /// @return a non-empty string representation suitable for debugging
  @Override
  String toString();

  @Accessors(fluent = true)
  final class EmptyConditional<T> implements Conditional<T> {

    @Getter(lazy = true)
    private static final EmptyConditional<?> instance = new EmptyConditional<>();

    private EmptyConditional() {}

    @Override
    public boolean isPresent() {
      return false;
    }

    @Override
    public T get() {
      throw new NoSuchElementException("Conditional value is null");
    }
  }
}
