package net.zum.liot.fluent.conditional;

import lombok.NonNull;

public sealed class ConditionalImpl<T> implements Conditional<T> permits ConditionalWithCondition {

  private final T value;

  public ConditionalImpl(@NonNull T value) {
    this.value = value;
  }

  @Override
  public boolean isPresent() {
    return true;
  }

  @Override
  public T get() {
    return value;
  }
}
