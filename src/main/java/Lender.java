import java.math.BigDecimal;
import java.util.Objects;

public class Lender {

    final private String name;
    // https://blogs.oracle.com/corejavatechtips/the-need-for-bigdecimal
    final private BigDecimal rate;
    // It could be used Currency class but there is only one instance
    final private BigDecimal available;

    public Lender(String name, BigDecimal rate, BigDecimal available) {
        this.name = name;
        this.rate = rate;
        this.available = available;
    }

    BigDecimal getAvailable() {
        return available;
    }

    BigDecimal getRate() {
        return rate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Lender))
            return false;
        // http://www.stichlberger.com/software/java-bigdecimal-gotchas/
        Lender lender = (Lender) o;
        return Objects.equals(name, lender.name) && lender.getRate().compareTo(lender.rate) == 0 && lender.getAvailable()
                .compareTo(lender.available) == 0;
    }
}
