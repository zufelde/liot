package net.zum.liot.fluent.conditional;

import java.util.function.Function;
import java.util.function.Supplier;

/** Dependency-free regression tests for mapper and supplier null handling. */
public final class ConditionalNullChecksTest {

  private ConditionalNullChecksTest() {}

  /**
   * Runs the regression tests, throwing an assertion error on failure.
   *
   * @param args unused command-line arguments
   */
  static void main(String[] args) {
    flatMapRejectsNullResults();
    flatMapSkipsMapperWhenEmpty();
    flatMapPreservesReturnedContainer();
    orRejectsNullResults();
    orSkipsSupplierWhenPresent();
    orPreservesReturnedContainer();
    nullArgumentsAreRejected();
    System.out.println("All 7 Conditional null-check regression tests passed.");
  }

  private static void flatMapRejectsNullResults() {
    expectNullPointerException(() -> Conditional.of("value").flatMap(value -> null));
  }

  private static void flatMapSkipsMapperWhenEmpty() {
    Conditional<String> result =
        Conditional.<String>empty()
            .flatMap(
                value -> {
                  throw new AssertionError("Empty conditional must not invoke the mapper");
                });
    if (!result.isEmpty()) {
      throw new AssertionError("Expected an empty result");
    }
  }

  private static void flatMapPreservesReturnedContainer() {
    Conditional<Integer> mapped = Conditional.of(42);
    Conditional<Number> result = Conditional.of("value").flatMap(_ -> mapped);
    if ((Conditional<?>) result != mapped) {
      throw new AssertionError("Expected the original mapped container");
    }
  }

  private static void orRejectsNullResults() {
    expectNullPointerException(() -> Conditional.<String>empty().or(() -> null));
  }

  private static void orSkipsSupplierWhenPresent() {
    Conditional<String> present = Conditional.of("value");
    Conditional<String> result =
        present.or(
            () -> {
              throw new AssertionError("Present conditional must not invoke the supplier");
            });
    if (result != present) {
      throw new AssertionError("Expected the original present container");
    }
  }

  private static void orPreservesReturnedContainer() {
    Conditional<Integer> fallback = Conditional.of(42);
    Conditional<Number> result = Conditional.<Number>empty().or(() -> fallback);
    if ((Conditional<?>) result != fallback) {
      throw new AssertionError("Expected the original fallback container");
    }
  }

  private static void nullArgumentsAreRejected() {
    Function<String, Conditional<String>> mapper = null;
    Supplier<Conditional<String>> supplier = null;
    expectNullPointerException(() -> Conditional.of("value").flatMap(mapper));
    expectNullPointerException(() -> Conditional.<String>empty().flatMap(mapper));
    expectNullPointerException(() -> Conditional.of("value").or(supplier));
    expectNullPointerException(() -> Conditional.<String>empty().or(supplier));
  }

  private static void expectNullPointerException(Runnable action) {
    try {
      action.run();
    } catch (NullPointerException expected) {
      return;
    }
    throw new AssertionError("Expected NullPointerException");
  }
}
