/*
 * The MIT License (MIT)
 * ---------------------
 *
 * Copyright (c) 2015-2016 Cristatus Solutions
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package cristatus.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A "Rational" number is one which can be expressed in the form of a ratio.
 * The two parts of the ratio, namely the Numerator and the Denominator are
 * integers.
 * <p>
 * This class encapsulates a Rational number and provides methods supporting
 * arithmetic operations on Rational Objects. Rational extends
 * {@link Number}, therefore making it compatible with other numeric types.
 * It uses {@link BigInteger} internally to represent the Numerator and the
 * Denominator, therefore, it is capable of supporting arbitrary-precision
 * arithmetic.
 * <p>
 * Rational has no public Constructors, but the overloaded
 * <code>valueOf()</code> methods provide the interface for creating
 * Rational instances. Because the parameters are defined as {@link Number},
 * any numeric data can be sent to these methods and an appropriate Rational
 * will be returned.
 * <p>
 * A Rational always stays in its reduced form, or in its lowest terms.
 * Consequently, 2/4 and 1/2 represent the same Rational.
 * <p>
 * An important thing to note is that all instances of Rational are
 * <i>immutable</i>.
 *
 * @author Subhomoy Haldar
 * @version 1.0
 * @see #valueOf(Number) The single argument valueOf() method.
 * @see #valueOf(Number, Number) The double argument valueOf() method.
 */
@SuppressWarnings("WeakerAccess")
public class Rational extends Number implements Comparable<Rational> {

    // "psf" constants

    /**
     * The Rational object representing the number 0.
     */
    public static final Rational ZERO = new Rational(0, 1);
    /**
     * The Rational object representing the number 1.
     */
    public static final Rational ONE = new Rational(1, 1);
    /**
     * The Rational object representing the number 10.
     */
    public static final Rational TEN = new Rational(10, 1);

    /**
     * The Rational object representing the number 0.5 or 1/2.
     */
    public static final Rational HALF = new Rational(1, 2);
    /**
     * The Rational object representing the number 0.25 or 1/4.
     */
    public static final Rational QUARTER = new Rational(1, 4);
    /**
     * The Rational object representing the number 0.(3) or 1/3.
     */
    public static final Rational THIRD = new Rational(1, 3);
    /**
     * The Rational object representing the number 0.1 or 1/10.
     */
    public static final Rational TENTH = new Rational(1, 10);

    private static final MathContext DOUBLE_CONTEXT
            = new MathContext(64, RoundingMode.HALF_UP);

    // Class data - the numerator and the denominator

    private final BigInteger num;
    private final BigInteger den;

    // Constructors - use verified arguments. Never make public

    /**
     * Helps to clean up the constant declarations.
     *
     * @param num The long integer representing the numerator.
     * @param den The long integer representing the denominator.
     */
    private Rational(final long num, final long den) {
        this(BigInteger.valueOf(num), BigInteger.valueOf(den));
    }

    /**
     * The main constructor that is used everywhere else. It reduces the
     * arguments to their lowest terms.
     *
     * @param num The {@link BigInteger} representing the numerator.
     * @param den The {@link BigInteger} representing the denominator.
     */
    private Rational(final BigInteger num, final BigInteger den) {
        BigInteger gcd = num.gcd(den);
        this.num = num.divide(gcd);
        this.den = den.divide(gcd);
    }

    // public getters

    /**
     * Returns the numerator of this Rational.
     *
     * @return The numerator of this Rational.
     */
    public BigInteger getNumerator() {
        return num;
    }

    /**
     * Returns the denominator of this Rational.
     *
     * @return The denominator of this Rational.
     */
    public BigInteger getDenominator() {
        return den;
    }

    /**
     * Returns the signum function of this Rational.
     *
     * @return -1, 0 or 1 as the value of this Rational is negative, zero or
     * positive.
     */
    public int signum() {
        return num.signum();
    }

    // Factory methods - always validate arguments

    /**
     * This is the single argument factory method for obtaining Rationals. All
     * numeric types are supported. Sample usage:
     * <p>
     * <pre><code>
     *     Rational two = Rational.valueOf(2);
     *     Rational oneEighth = Rational.valueOf(0.125);
     *     Rational oneHundredth = Rational.valueOf(new BigDecimal("1E-100"));
     * </code></pre>
     *
     * @param number The number to represent by a Rational object.
     * @return A Rational object encapsulating the number.
     * @throws IllegalArgumentException If the argument is {@code null}.
     */
    public static Rational valueOf(Number number)
            throws IllegalArgumentException {
        // Quick exits
        if (number == null)
            throw new IllegalArgumentException("Null argument");
        if (number instanceof Rational)
            return (Rational) number;
        // Approximation isn't any faster than using String...
        if (number instanceof Double || number instanceof Float)
            return rationalFromBigDecimal(BigDecimal.valueOf(number.doubleValue()));
        // A simple BigInteger surely won't cause any trouble
        if (number instanceof BigInteger)
            return new Rational((BigInteger) number, BigInteger.ONE);
        if (number instanceof BigDecimal)
            return rationalFromBigDecimal((BigDecimal) number);
        // Unrecognised Number type... fallback to BigDecimal
        return rationalFromBigDecimal(new BigDecimal(number.toString()));
    }

    /**
     * Processes a {@link BigDecimal} and returns it as a Rational.
     *
     * @param decimal The {@link BigDecimal} to process.
     * @return A Rational encapsulating the {@link BigDecimal}.
     */
    private static Rational rationalFromBigDecimal(BigDecimal decimal) {
        BigInteger num = decimal.unscaledValue();
        BigInteger den = BigInteger.ONE;
        int scale = decimal.scale();
        if (scale < 0)
            num = num.multiply(BigInteger.TEN.pow(-scale));
        else
            den = den.multiply(BigInteger.TEN.pow(scale));
        return getCorrectedRational(num, den);
    }

    /**
     * This is the double argument factory method for obtaining Rationals.
     * The major advantage of this method is that it returns a lossless
     * representation of the division of any two numbers. This is very handy
     * in case of fractional numbers and especially {@link BigDecimal}, because
     * it does not require the use of a {@link MathContext} for proper
     * functioning.
     * <p>
     * Different data types can also be used in combination. It is a very
     * flexible interface.
     * <p>
     * Sample usage:
     * <p>
     * <pre><code>
     *      Rational big = Rational.valueOf(1E300, 1E-300);
     *      Rational tiny = Rational.valueOf(1E-100, new BigDecimal("1E200"));
     * </code></pre>
     *
     * @param num The numerator or dividend.
     * @param den The denominator or divisor.
     * @return A Rational exactly representing the division of num by den.
     * @throws IllegalArgumentException If any of the arguments is {@code
     *                                  null} or the divisor is zero.
     */
    public static Rational valueOf(Number num, Number den)
            throws IllegalArgumentException {
        if (num == null)
            throw new IllegalArgumentException("Null numerator.");
        if (den == null)
            throw new IllegalArgumentException("Null denominator.");
        if (isFractional(num) || isFractional(den)) {
            return getRationalFromFractions(num, den);
        } else {
            return getRationalFromIntegers(num, den);
        }
    }

    /**
     * Helper method to check if a number is of a fractional type. Helps to
     * make the code readable.
     *
     * @param number The number whose type to check.
     * @return {@code true} if the number is a float, double or
     * {@link BigDecimal}.
     */
    private static boolean isFractional(Number number) {
        return number instanceof Float || number instanceof Double
                || number instanceof BigDecimal;
    }

    /**
     * The double argument valueOf() delegates control to this method once it
     * has identified at least one of the two arguments as fractional.
     *
     * @param num The numerator or dividend.
     * @param den The denominator or divisor.
     * @return The appropriate Rational.
     */
    private static Rational getRationalFromFractions(Number num,
                                                     Number den) {
        // try to prevent expensive String parsing
        BigDecimal n0 = num instanceof BigDecimal
                ? (BigDecimal) num
                : new BigDecimal(num.toString());
        BigDecimal d0 = den instanceof BigDecimal
                ? (BigDecimal) den
                : new BigDecimal(den.toString());
        BigInteger n = n0.unscaledValue();
        BigInteger d = d0.unscaledValue();
        int scale = n0.scale() - d0.scale();
        if (scale < 0)
            n = n.multiply(BigInteger.TEN.pow(-scale));
        else
            d = d.multiply(BigInteger.TEN.pow(scale));
        return getCorrectedRational(n, d);
    }

    /**
     * The double argument valueOf() delegates control to this method once it
     * has identified both the arguments as integral.
     *
     * @param num The numerator.
     * @param den The denominator.
     * @return The appropriate Rational.
     */
    private static Rational getRationalFromIntegers(Number num,
                                                    Number den) {
        // Try to skip toString altogether
        BigInteger n = num instanceof BigInteger
                ? (BigInteger) num
                : BigInteger.valueOf(num.longValue());
        BigInteger d = den instanceof BigInteger
                ? (BigInteger) den
                : BigInteger.valueOf(den.longValue());
        return getCorrectedRational(n, d);
    }

    /**
     * Control is delegated to this method to check for zero divisor checking
     * and sign correction.
     *
     * @param num The numerator.
     * @param den The denominator.
     * @return The "corrected" Rational.
     * @throws IllegalArgumentException If the divisor is zero.
     */
    private static Rational getCorrectedRational(BigInteger num,
                                                 BigInteger den)
            throws IllegalArgumentException {
        // Don't allow zero denominators
        if (den.signum() == 0)
            throw new IllegalArgumentException("Zero denominator.");

        // There is no concept of (+)ve or (-)ve 0 here...
        if (num.signum() == 0) return ZERO;

        // Make sure the numerator carries the sign
        if (den.signum() < 0) {
            num = num.negate();
            den = den.negate();
        }
        // The constructor will reduce the terms
        return new Rational(num, den);
    }

    // Methods from Number.java and extras

    /**
     * This method returns a BigDecimal with the given accuracy encapsulated
     * in the context.
     *
     * @param context The desired precision of representation.
     * @return A {@link BigDecimal} of the desired accuracy having the same
     * value as this Rational.
     */
    public BigDecimal toBigDecimal(MathContext context) {
        return new BigDecimal(num)
                .divide(new BigDecimal(den), context)
                .stripTrailingZeros();
    }

    /**
     * Returns the result of arbitrary-precision division of the numerator by
     * the denominator as a {@link BigInteger}.
     *
     * @return The result of arbitrary-precision division of the numerator by
     * the denominator as a {@link BigInteger}.
     */
    public BigInteger toBigInteger() {
        return num.divide(den);
    }

    /**
     * Returns the value of this Rational, approximated as an int.
     *
     * @return The value of this Rational, approximated as an int.
     */
    @Override
    public int intValue() {
        return toBigInteger().intValue();
    }

    /**
     * Returns the value of this Rational, approximated as a long.
     *
     * @return The value of this Rational, approximated as a long.
     */
    @Override
    public long longValue() {
        return toBigInteger().longValue();
    }

    /**
     * Returns the value of this Rational, approximated as a float.
     *
     * @return The value of this Rational, approximated as a float.
     */
    @Override
    public float floatValue() {
        return toBigDecimal(MathContext.DECIMAL128).floatValue();
    }

    /**
     * Returns the value of this Rational, approximated as a double.
     *
     * @return The value of this Rational, approximated as a double.
     */
    @Override
    public double doubleValue() {
        return toBigDecimal(DOUBLE_CONTEXT).doubleValue();
    }

    // Methods from Object.java

    /**
     * Returns {@code true} if the invoking Rational and the argument are equal.
     *
     * @param other The Object to check for equality.
     * @return {@code true} if the invoking Rational and the argument are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Rational)) return false;
        Rational r = (Rational) other;
        return signum() == r.signum()   // fast reject
                && num.multiply(r.den).equals(den.multiply(r.num));
    }

    /**
     * Returns the hash code for this Rational. It is useful when it is used
     * in {@link java.util.HashSet}s and {@link java.util.HashMap}s.
     *
     * @return The hash code for this Rational.
     */
    @Override
    public int hashCode() {
        return Objects.hash(num, den);
    }

    /**
     * Returns a representation of this Rational as a String.
     *
     * @return A representation of this Rational as a String.
     */
    @Override
    public String toString() {
        return num + "/" + den;
    }

    // From Comparable: compareTo...

    /**
     * Compares the two Rationals and returns an int storing the result. It
     * returns -1, 0 or 1 as this Rational is numerically less than, equal
     * to, or greater than the given Rational.
     *
     * @param r The Rational to compare it with.
     * @return -1, 0 or 1 as this Rational is numerically less than, equal
     * to, or greater than the given Rational.
     */
    @Override
    public int compareTo(Rational r) {
        // A big help, this one...
        if (signum() != r.signum())
            return Integer.compare(signum(), r.signum());
        return num.multiply(r.den).compareTo(r.num.multiply(den));
    }

    // Arithmetic methods

    public Rational add(Rational term) {
        BigInteger n1 = this.num.multiply(term.den);
        BigInteger n2 = term.num.multiply(this.den);
        return new Rational(n1.add(n2), this.den.multiply(term.den));
    }

    public Rational subtract(Rational term) {
        BigInteger n1 = this.num.multiply(term.den);
        BigInteger n2 = term.num.multiply(this.den);
        return new Rational(n1.subtract(n2), this.den.multiply(term.den));
    }

    public Rational negate() {
        return new Rational(num.negate(), den);
    }

    public Rational reciprocate() {
        return new Rational(den, num);
    }

    public Rational multiply(Rational term) {
        return new Rational(num.multiply(term.num), den.multiply(term.den));
    }

    public Rational divide(Rational term) {
        return this.multiply(term.reciprocate());
    }

    public Rational pow(Rational power, MathContext context)
            throws ArithmeticException {
        if (signum() < 0 && (power.intValue() & 1) != 1) {
            throw new ArithmeticException("Real principal root doesn't exist.");
        }
        int pow = power.num.intValueExact();
        boolean neg = pow < 0;
        pow = neg ? -pow : pow;
        BigInteger n0 = num.pow(pow);
        BigInteger d0 = den.pow(pow);
        int root = power.den.intValueExact();
        if (root == 1)
            return neg ? getCorrectedRational(d0, n0) : getCorrectedRational(n0, d0);
        else
            return nthRoot(n0, d0, root, neg, context);
    }

    private Rational nthRoot(BigInteger n0, BigInteger d0,
                             int root, boolean neg,
                             MathContext context) {
        if (context == null) {
            throw new ArithmeticException("MathContext is null for fractional power.");
        }
        BigDecimal n = BigMath.nthRoot(n0, root, context);
        BigDecimal d = BigMath.nthRoot(d0, root, context);
        return neg
                ? getRationalFromFractions(d, n)
                : getRationalFromFractions(n, d);
    }
}
