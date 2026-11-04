package restaurantrush.model;

/**
 * An immutable amount of money held as whole cents.
 * <p>
 * Storing integer cents keeps every price exact (no floating-point drift) and
 * gives percentage discounts one well-defined rounding rule: half-up to the
 * nearest cent.
 */
public final class Money implements Comparable<Money> {

    public static final Money ZERO = new Money(0);

    private final long cents;

    private Money(long cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + cents + " cents");
        }
        this.cents = cents;
    }

    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    public static Money ofDollars(long dollars) {
        return new Money(Math.multiplyExact(dollars, 100L));
    }

    public long getCents() {
        return cents;
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(cents, other.cents));
    }

    /**
     * Returns {@code percent}% of this amount, rounded half-up to the nearest cent.
     * Example: {@code Money.ofDollars(27).percentage(90)} is $24.30.
     */
    public Money percentage(int percent) {
        if (percent < 0) {
            throw new IllegalArgumentException("Percentage cannot be negative: " + percent);
        }
        return new Money((cents * percent + 50) / 100);
    }

    public boolean isAtLeast(Money other) {
        return cents >= other.cents;
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(cents, other.cents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money)) {
            return false;
        }
        return cents == ((Money) o).cents;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(cents);
    }

    /** Dollars with two decimals, e.g. {@code $18.00}. */
    @Override
    public String toString() {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }
}
