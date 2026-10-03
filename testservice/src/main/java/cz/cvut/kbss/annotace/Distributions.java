package cz.cvut.kbss.annotace;

/**
 * Numerical computation of the quantile (critical value) of the Student's
 * t-distribution and the Phi-distribution (standard normal distribution,
 * which is the limit of the Student's t-distribution as n -> infinity).
 *
 * No lookup tables - everything is computed.
 *
 * Usage:
 *   Distributions.studentQuantile(degreesOfFreedom, level)
 *   Distributions.phiQuantile(level)
 *
 * "level" = the required cumulative probability.
 * For a one-sided test at significance level alpha = 5%, pass level = 0.95.
 */
public class Distributions {

    /** Natural logarithm of the gamma function (Lanczos approximation, g=7). */
    private static double logGamma(double x) {
        double[] g = {
                676.5203681218851, -1259.1392167224028, 771.32342877765313,
                -176.61502916214059, 12.507343278686905, -0.13857109526572012,
                9.9843695780195716e-6, 1.5056327351493116e-7
        };
        if (x < 0.5) {
            return Math.log(Math.PI / Math.sin(Math.PI * x)) - logGamma(1.0 - x);
        }
        x -= 1.0;
        double a = 0.99999999999980993;
        double t = x + 7.5;
        for (int i = 0; i < 8; i++) {
            a += g[i] / (x + i + 1);
        }
        return 0.5 * Math.log(2 * Math.PI) + (x + 0.5) * Math.log(t) - t + Math.log(a);
    }

    /** Continued fraction for the incomplete beta function (Numerical Recipes algorithm). */
    private static double betacf(double a, double b, double x) {
        final int MAXIT = 200;
        final double EPS = 3.0e-14;
        final double FPMIN = 1.0e-300;

        double qab = a + b;
        double qap = a + 1.0;
        double qam = a - 1.0;
        double c = 1.0;
        double d = 1.0 - qab * x / qap;
        if (Math.abs(d) < FPMIN) d = FPMIN;
        d = 1.0 / d;
        double h = d;

        for (int m = 1; m <= MAXIT; m++) {
            int m2 = 2 * m;
            double aa = m * (b - m) * x / ((qam + m2) * (a + m2));
            d = 1.0 + aa * d;
            if (Math.abs(d) < FPMIN) d = FPMIN;
            c = 1.0 + aa / c;
            if (Math.abs(c) < FPMIN) c = FPMIN;
            d = 1.0 / d;
            h *= d * c;

            aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2));
            d = 1.0 + aa * d;
            if (Math.abs(d) < FPMIN) d = FPMIN;
            c = 1.0 + aa / c;
            if (Math.abs(c) < FPMIN) c = FPMIN;
            d = 1.0 / d;
            double del = d * c;
            h *= del;

            if (Math.abs(del - 1.0) < EPS) break;
        }
        return h;
    }

    /** Regularized incomplete beta function I_x(a,b). */
    private static double regIncBeta(double x, double a, double b) {
        if (x <= 0.0) return 0.0;
        if (x >= 1.0) return 1.0;
        double bt = Math.exp(logGamma(a + b) - logGamma(a) - logGamma(b)
                + a * Math.log(x) + b * Math.log(1.0 - x));
        if (x < (a + 1.0) / (a + b + 2.0)) {
            return bt * betacf(a, b, x) / a;
        } else {
            return 1.0 - bt * betacf(b, a, 1.0 - x) / b;
        }
    }

    // =========================================================
    //  Student's t-distribution
    // =========================================================

    /** Cumulative distribution function (CDF) of the Student's t-distribution with "df" degrees of freedom. */
    public static double studentCDF(double t, double df) {
        double x = df / (df + t * t);
        double ib = regIncBeta(x, df / 2.0, 0.5);
        return (t >= 0) ? 1.0 - 0.5 * ib : 0.5 * ib;
    }

    /**
     * Quantile (critical value) of the Student's t-distribution - found by
     * bisection, since the CDF is a strictly monotonic function.
     *
     * @param degreesOfFreedom number of degrees of freedom
     * @param level            required cumulative probability, e.g. 0.95
     *                         for a one-sided test at significance level 5%
     */
    public static double studentQuantile(long degreesOfFreedom, double level) {
        if (level <= 0.0 || level >= 1.0) {
            throw new IllegalArgumentException("level must be in the interval (0;1)");
        }
        if (degreesOfFreedom < 1) {
            throw new IllegalArgumentException("degrees of freedom must be >= 1");
        }

        boolean negative = level < 0.5;
        double p = negative ? 1.0 - level : level;

        double lo = 0.0, hi = 1.0;
        while (studentCDF(hi, (double) degreesOfFreedom) < p) {
            hi *= 2.0;
        }
        for (int i = 0; i < 100; i++) {
            double mid = (lo + hi) / 2.0;
            if (studentCDF(mid, (double) degreesOfFreedom) < p) lo = mid; else hi = mid;
        }
        double result = (lo + hi) / 2.0;
        return negative ? -result : result;
    }

    // =========================================================
    //  Phi-distribution = standard normal distribution
    //  (limit of the Student's t-distribution as degrees of freedom -> infinity)
    // =========================================================

    /**
     * Quantile of the standard normal distribution (Acklam's algorithm,
     * relative accuracy about 1.15e-9).
     *
     * @param level required cumulative probability
     */
    public static double phiQuantile(double level) {
        if (level <= 0.0 || level >= 1.0) {
            throw new IllegalArgumentException("level must be in the interval (0;1)");
        }

        double[] a = {-3.969683028665376e+01, 2.209460984245205e+02, -2.759285104469687e+02,
                1.383577518672690e+02, -3.066479806614716e+01, 2.506628277459239e+00};
        double[] b = {-5.447609879822406e+01, 1.615858368580409e+02, -1.556989798598866e+02,
                6.680131188771972e+01, -1.328068155288572e+01};
        double[] c = {-7.784894002430293e-03, -3.223964580411365e-01, -2.400758277161838e+00,
                -2.549732539343734e+00, 4.374664141464968e+00, 2.938163982698783e+00};
        double[] d = {7.784695709041462e-03, 3.224671290700398e-01, 2.445134137142996e+00,
                3.754408661907416e+00};

        double plow = 0.02425;
        double phigh = 1.0 - plow;
        double q, r;

        if (level < plow) {
            q = Math.sqrt(-2.0 * Math.log(level));
            return (((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) /
                    ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1.0);
        } else if (level <= phigh) {
            q = level - 0.5;
            r = q * q;
            return (((((a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) * q /
                    (((((b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1.0);
        } else {
            q = Math.sqrt(-2.0 * Math.log(1.0 - level));
            return -(((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) /
                    ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1.0);
        }
    }
}
