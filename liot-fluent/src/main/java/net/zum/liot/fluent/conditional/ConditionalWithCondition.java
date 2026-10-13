package net.zum.liot.fluent.conditional;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Accessors(fluent = true)
public final class ConditionalWithCondition<T> extends ConditionalImpl<T> implements Conditional<T> {

  private final Predicate<? super T> condition;

  @Getter(lazy = true)
  private final Boolean result = condition.test(get());

  ConditionalWithCondition(@NonNull T value, Predicate<? super T> condition) {
    super(value);
    this.condition = condition;
  }

  public ConditionalWithCondition<T> onTrue(Consumer<? super T> action) {
    if (result()) {
      action.accept(get());
    }
    return this;
  }

  public ConditionalWithCondition<T> onFalse(Consumer<? super T> action) {
    if (!result()) {
      action.accept(get());
    }
    return this;
  }

  public <U> Conditional<U> map(Function<? super T, ? extends U> trueMapper, Function<? super T, ? extends U> falseMapper) {
    if (result()) {
      return Conditional.ofNullable(trueMapper.apply(get()));
    } else {
      return Conditional.ofNullable(falseMapper.apply(get()));
    }
  }
}
