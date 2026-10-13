package net.zum.liot;

import lombok.NonNull;

import java.util.Objects;
import java.util.function.Supplier;

public final class Lazy<T> {

  private final Supplier<T> initCode;

  private T value;

  private boolean initialized = false;

  private Lazy(@NonNull Supplier<T> initCode) {
      this.initCode = initCode;
  }

  public synchronized T get() {
    if (!initialized) {
      value = initCode.get();
      initialized = true;
    }
    return value;
  }

  public static <T> Lazy<T> of(Supplier<T> initCode) {
    return new Lazy<>(initCode);
  }
}
