// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package edu.wpi.first.units;

/**
 * A measure holds the magnitude and unit of some dimension, such as distance, time, or speed. Two
 * measures with the same <i>unit</i> and <i>magnitude</i> are effectively the same object.
 *
 * @param <U> the unit type of the measure
 */
public interface Measure<U extends Unit<U>> extends Comparable<Measure<U>> {
  /**
   * The threshold for two measures to be considered equivalent if converted to the same unit. This
   * is only needed due to floating-point error.
   */
  double EQUIVALENCE_THRESHOLD = 1e-12;

  /** Gets the unitless magnitude of this measure. */
  double magnitude();

  /** Gets the magnitude of this measure in terms of the base unit. */
  double baseUnitMagnitude();

  /** Gets the units of this measure. */
  U unit();

  /**
   * Converts this measure to a measure with a different unit of the same type, eg minutes to
   * seconds. Converting to the same unit is equivalent to calling {@link #magnitude()}.
   *
   * <pre>
   *   Meters.of(12).in(Feet) // => 39.3701
   *   Seconds.of(15).in(Minutes) // => 0.25
   * </pre>
   *
   * @param unit the unit to convert this measure to
   * @return the value of this measure in the given unit
   */
  default double in(Unit<U> unit) {
    if (this.unit().equals(unit)) {
      return magnitude();
    } else {
      return unit.fromBaseUnits(baseUnitMagnitude());
    }
  }

  /**
   * Multiplies this measurement by some constant multiplier and returns the result.
   *
   * @param multiplier the constant to multiply by
   */
  default Measure<U> times(double multiplier) {
    return ImmutableMeasure.ofBaseUnits(baseUnitMagnitude() * multiplier, unit());
  }

  /**
   * Generates a new measure that is equal to this measure multiplied by another. Some dimensional
   * analysis is performed to reduce the units down somewhat; for example, multiplying a {@code
   * Measure<Time>} by a {@code Measure<Velocity<Distance>>} will return just a {@code
   * Measure<Distance>} instead of the naive {@code Measure<Mult<Time, Velocity<Distance>>}. This is
   * not guaranteed to perform perfect dimensional analysis.
   *
   * @param <U2> the type of the other measure to multiply by
   * @param other the unit to multiply by
   * @return the multiplicative unit
   */
  @SuppressWarnings("unchecked")
  default <U2 extends Unit<U2>> Measure<?> times(Measure<U2> other) {
    if (other.unit() instanceof Dimensionless) {
      // scalar multiplication
      return times(other.baseUnitMagnitude());
    }

    if (unit() instanceof Per
        && other.unit().m_baseType.equals(((Per<?, ?>) unit()).denominator().m_baseType)) {
      // denominator of the Per cancels out, return with just the units of the numerator
      Unit<?> numerator = ((Per<?, ?>) unit()).numerator();
      return numerator.ofBaseUnits(baseUnitMagnitude() * other.baseUnitMagnitude());
    } else if (unit() instanceof Velocity && other.unit().m_baseType.equals(Time.class)) {
      // Multiplying a velocity by a time, return the scalar unit (eg Distance)
      Unit<?> numerator = ((Velocity<?>) unit()).getUnit();
      return numerator.ofBaseUnits(baseUnitMagnitude() * other.baseUnitMagnitude());
    } else if (other.unit() instanceof Per
        && unit().m_baseType.equals(((Per<?, ?>) other.unit()).denominator().m_baseType)) {
      Unit<?> numerator = ((Per<?, ?>) other.unit()).numerator();
      return numerator.ofBaseUnits(baseUnitMagnitude() * other.baseUnitMagnitude());
    } else if (unit() instanceof Per
        && other.unit() instanceof Per
        && ((Per<?, ?>) unit())
            .denominator()
            .m_baseType
            .equals(((Per<?, U>) other.unit()).numerator().m_baseType)
        && ((Per<?, ?>) unit())
            .numerator()
            .m_baseType
            .equals(((Per<?, ?>) other.unit()).denominator().m_baseType)) {
      // multiplying eg meters per second * milliseconds per foot
      // return a scalar
      return Units.Value.of(baseUnitMagnitude() * other.baseUnitMagnitude());
    }

    // Dimensional analysis fallthrough, do a basic unit multiplication
    return unit().mult(other.unit()).ofBaseUnits(baseUnitMagnitude() * other.baseUnitMagnitude());
  }

  /**
   * Divides this measurement by some constant divisor and returns the result. This is equivalent to
   * {@code times(1 / divisor)}
   *
   * @param divisor the constant to divide by
   * @see #times(double)
   */
  default Measure<U> divide(double divisor) {
    return times(1 / divisor);
  }

  /**
   * Divides this measurement by some constant divisor and returns the result. This is equivalent to
   * {@code divide(divisor.baseUnitMagnitude())}
   *
   * @param divisor the dimensionless measure to divide by
   * @see #divide(double)
   * @see #times(double)
   */
  default Measure<U> divide(Measure<Dimensionless> divisor) {
    return divide(divisor.baseUnitMagnitude());
  }

  /**
   * Creates a velocity measure by dividing this one by a time period measure.
   *
   * <pre>
   *   Meters.of(1).per(Second) // => Measure&lt;Velocity&lt;Distance&gt;&gt;
   * </pre>
   *
   * @param period the time period to divide by.
   * @return the velocity result
   */
  default Measure<Velocity<U>> per(Measure<Time> period) {
    var newUnit = unit().per(period.unit());
    return ImmutableMeasure.ofBaseUnits(baseUnitMagnitude() / period.baseUnitMagnitude(), newUnit);
  }

  /**
   * Creates a relational measure equivalent to this one per some other unit.
   *
   * <pre>
   *   Volts.of(1.05).per(Meter) // => V/m, potential PID constant
   * </pre>
   *
   * @param <U2> the type of the denominator unit
   * @param denominator the denominator unit being divided by
   * @return the relational measure
   */
  default <U2 extends Unit<U2>> Measure<Per<U, U2>> per(U2 denominator) {
    var newUnit = unit().per(denominator);
    return newUnit.of(magnitude());
  }

  /**
   * Adds another measure to this one. The resulting measure has the same unit as this one.
   *
   * @param other the measure to add to this one
   * @return a new measure containing the result
   */
  default Measure<U> plus(Measure<U> other) {
    return unit().ofBaseUnits(baseUnitMagnitude() + other.baseUnitMagnitude());
  }

  /**
   * Subtracts another measure from this one. The resulting measure has the same unit as this one.
   *
   * @param other the measure to subtract from this one
   * @return a new measure containing the result
   */
  default Measure<U> minus(Measure<U> other) {
    return unit().ofBaseUnits(baseUnitMagnitude() - other.baseUnitMagnitude());
  }

  /** Negates this measure and returns the result. */
  default Measure<U> negate() {
    return times(-1);
  }

  /**
   * Returns an immutable copy of this measure. The copy can be used freely and is guaranteed never
   * to change.
   */
  Measure<U> copy();

  /**
   * Creates a new mutable copy of this measure.
   *
   * @return a mutable measure initialized to be identical to this measure
   */
  default MutableMeasure<U> mutableCopy() {
    return MutableMeasure.mutable(this);
  }

  /**
   * Checks if this measure is near another measure of the same unit. Provide a variance threshold
   * for use for a +/- scalar, such as 0.05 for +/- 5%.
   *
   * <pre>
   *   Inches.of(11).isNear(Inches.of(10), 0.1) // => true
   *   Inches.of(12).isNear(Inches.of(10), 0.1) // => false
   * </pre>
   *
   * @param other the other measurement to compare against
   * @param varianceThreshold the acceptable variance threshold, in terms of an acceptable +/- error
   *     range multiplier. Checking if a value is within 10% means a value of 0.1 should be passed;
   *     checking if a value is within 1% means a value of 0.01 should be passed, and so on.
   * @return true if this unit is near the other measure, otherwise false
   */
  default boolean isNear(Measure<?> other, double varianceThreshold) {
    if (this.unit().m_baseType != other.unit().m_baseType) {
      return false; // Disjoint units, not compatible
    }

    // abs so negative inputs are calculated correctly
    var allowedVariance = Math.abs(varianceThreshold);

    return other.baseUnitMagnitude() * (1 - allowedVariance) <= this.baseUnitMagnitude()
        && other.baseUnitMagnitude() * (1 + allowedVariance) >= this.baseUnitMagnitude();
  }

  /**
   * Checks if this measure is equivalent to another measure of the same unit.
   *
   * @param other the measure to compare to
   * @return true if this measure is equivalent, false otherwise
   */
  default boolean isEquivalent(Measure<?> other) {
    if (this.unit().m_baseType != other.unit().m_baseType) {
      return false; // Disjoint units, not compatible
    }

    return Math.abs(baseUnitMagnitude() - other.baseUnitMagnitude()) <= EQUIVALENCE_THRESHOLD;
  }

  @Override
  default int compareTo(Measure<U> o) {
    return Double.compare(this.baseUnitMagnitude(), o.baseUnitMagnitude());
  }

  /**
   * Checks if this measure is greater than another measure of the same unit.
   *
   * @param o the other measure to compare to
   */
  default boolean gt(Measure<U> o) {
    return compareTo(o) > 0;
  }

  /**
   * Checks if this measure is greater than or equivalent to another measure of the same unit.
   *
   * @param o the other measure to compare to
   */
  default boolean gte(Measure<U> o) {
    return compareTo(o) > 0 || isEquivalent(o);
  }

  /**
   * Checks if this measure is less than another measure of the same unit.
   *
   * @param o the other measure to compare to
   */
  default boolean lt(Measure<U> o) {
    return compareTo(o) < 0;
  }

  /**
   * Checks if this measure is less than or equivalent to another measure of the same unit.
   *
   * @param o the other measure to compare to
   */
  default boolean lte(Measure<U> o) {
    return compareTo(o) < 0 || isEquivalent(o);
  }

  /**
   * Returns the measure with the absolute value closest to positive infinity.
   *
   * @param <U> the type of the units of the measures
   * @param measures the set of measures to compare
   * @return the measure with the greatest positive magnitude, or null if no measures were provided
   */
  @SafeVarargs
  static <U extends Unit<U>> Measure<U> max(Measure<U>... measures) {
    if (measures.length == 0) {
      return null; // nothing to compare
    }

    Measure<U> max = null;
    for (Measure<U> measure : measures) {
      if (max == null || measure.gt(max)) {
        max = measure;
      }
    }

    return max;
  }

  /**
   * Returns the measure with the absolute value closest to negative infinity.
   *
   * @param <U> the type of the units of the measures
   * @param measures the set of measures to compare
   * @return the measure with the greatest negative magnitude
   */
  @SafeVarargs
  static <U extends Unit<U>> Measure<U> min(Measure<U>... measures) {
    if (measures.length == 0) {
      return null; // nothing to compare
    }

    Measure<U> max = null;
    for (Measure<U> measure : measures) {
      if (max == null || measure.lt(max)) {
        max = measure;
      }
    }

    return max;
  }

  /**
   * Returns a string representation of this measurement in a shorthand form. The symbol of the
   * backing unit is used, rather than the full name, and the magnitude is represented in scientific
   * notation.
   *
   * @return the short form representation of this measurement
   */
  default String toShortString() {
    // eg 1.234e+04 V/m (1234 Volt per Meter in long form)
    return String.format("%.3e %s", magnitude(), unit().symbol());
  }

  /**
   * Returns a string representation of this measurement in a longhand form. The name of the backing
   * unit is used, rather than its symbol, and the magnitude is represented in a full string, no
   * scientific notation. (Very large values may be represented in scientific notation, however)
   *
   * @return the long form representation of this measurement
   */
  default String toLongString() {
    // eg 1234 Volt per Meter (1.234e+04 V/m in short form)
    return String.format("%s %s", magnitude(), unit().name());
  }
}
