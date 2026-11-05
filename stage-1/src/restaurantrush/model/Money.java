package restaurantrush.model;

/**
 * An immutable amount of money held as whole cents, so prices are always exact.
 * Displayed as dollars with two decimals.
 */
public final class Money {

    public static final Money ZERO = new Money(0);

    private final long cents;

    private Money(long cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + cents + " cents");
        }
        this.cents = cents;
    }

    public static Money ofDollars(long dollars) {
        return new Money(Math.multiplyExact(dollars, 100L));
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(cents, other.cents));
    }

    public boolean isAtLeast(Money other) {
        return cents >= other.cents;
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
