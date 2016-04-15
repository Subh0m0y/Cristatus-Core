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

package cristatus.core.utils;

import cristatus.core.Rational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * @author Subhomoy Haldar
 * @version 1.0
 */
@SuppressWarnings("WeakerAccess")
public class BigMath {

    public static BigDecimal hypot(Number x, Number y, MathContext context) {
        return hypot(decimalFrom(x, context), decimalFrom(y, context), context);
    }

    public static BigDecimal hypot(BigDecimal x, BigDecimal y,
                                   MathContext context) {
        return sqrt(x.pow(2).add(y.pow(2)), context);
    }

    public static BigDecimal sqrt(Number number, MathContext context) {
        return nthRoot(decimalFrom(number, context), 2, context);
    }

    public static BigDecimal sqrt(BigDecimal decimal, MathContext context) {
        return nthRoot(decimal, 2, context);
    }

    public static BigDecimal cbrt(Number number, MathContext context) {
        return nthRoot(decimalFrom(number, context), 3, context);
    }

    public static BigDecimal cbrt(BigDecimal decimal, MathContext context) {
        return nthRoot(decimal, 3, context);
    }

    public static BigDecimal nthRoot(Number number, int n,
                                     MathContext context) {
        return nthRoot(decimalFrom(number, context), n, context);
    }

    public static BigDecimal nthRoot(BigDecimal decimal, int n,
                                     MathContext context) {
        BigInteger value = decimal.unscaledValue();
        int newScale = decimal.scale();
        // Because the scale must be in the form of k*n, where k is an integer
        int adjustment = n - newScale % n;
        newScale += adjustment;
        newScale /= n;
        int precision = context.getPrecision() + adjustment;
        BigInteger padding = BigInteger.TEN.pow(precision * n + adjustment);
        value = value.multiply(padding);
        BigInteger eps = BigInteger.ONE;
        return new BigDecimal(newtonApproximate(value, eps, n), newScale + precision);
    }

    private static BigInteger newtonApproximate(BigInteger raw,
                                                BigInteger eps,
                                                int n) {
        int nM1 = n - 1;
        BigInteger N = BigInteger.valueOf(n);
        BigInteger guess = BigInteger.ONE.shiftLeft(raw.bitLength() / n);
        BigInteger delta;
        do {
            BigInteger powered = guess.pow(nM1);
            delta = raw.divide(powered).subtract(guess).divide(N);
            guess = guess.add(delta);
        } while (delta.abs().compareTo(eps) > 0);
        return guess;
    }

    static BigDecimal decimalFrom(Number number, MathContext context) {
        return (number instanceof Rational)
                ? ((Rational) number).toBigDecimal(context)
                : new BigDecimal(number.toString());
    }

    static BigInteger integerFrom(Number number) {
        return (number instanceof Rational)
                ? ((Rational) number).toBigInteger()
                : new BigInteger(number.toString());
    }
}