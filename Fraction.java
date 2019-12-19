public class Fraction implements Comparable<Fraction>
{
    private int numerator;
    private int denominator;

    public Fraction()
    {
        numerator = 0;
        denominator = 1;
    }

    public Fraction (int n, int d)
    {
        numerator = n;
        denominator = d;
    }


    public double toDecimal()
    {
        return (double)numerator/denominator;
    }

    public void setNumerator(int n)
    {
        numerator = n;
    }

    public int compareTo(Fraction other)
    {
        if (this.toDecimal() > other.toDecimal())
        {
            return 1;
        }
        else if (this.toDecimal() == other.toDecimal())
        {
            return 0;
        }

        return -1;
    }

    public String toString()
    {
        return numerator + "/" + denominator;
    }

}
