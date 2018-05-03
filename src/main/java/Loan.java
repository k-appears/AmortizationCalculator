import ch.obermuhlner.math.big.BigDecimalMath;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

class Loan {

    private static final BigDecimal DURATION_MONTHS = new BigDecimal("36");

    /**
     * Calculate as low a rate to the borrower as possible.
     * <p>
     * Should be able to request a loan of any £100 increment between £1000 and £15000 inclusive.
     * <p>
     * If the market does not have sufficient offers from lenders to satisfy the loan then
     * the system should inform the borrower that it is not possible to provide a quote at that time.
     *
     * @return borrower info when possible
     */
    Optional<BorrowerInfo> calculate(String filenamePath, double requestedAmount, CSV csv) {
        if (requestedAmount < 1000 || requestedAmount > 15000) {
            throw new IllegalArgumentException(
                    "Requested quantity '" + requestedAmount + "'should be between £1000 and £15000 inclusive");
        }
        if (requestedAmount % 100 != 0) {
            throw new IllegalArgumentException("Requested quantity '" + requestedAmount
                    + "' should be of any £100 increment between £1000 and £15000 inclusive");
        }
        List<Lender> lenders;
        try {
            lenders = csv.extractLenders(filenamePath);
        } catch (IOException e) {
            return Optional.empty();
        }
        if (lenders.size() == 0) {
            return Optional.empty();
        }
        double totalAmount = lenders.stream().mapToDouble(lender -> lender.getAvailable().doubleValue()).sum();
        if (totalAmount < requestedAmount) {
            return Optional.empty();
        }
        // http://www.stichlberger.com/software/java-bigdecimal-gotchas/
        BigDecimal requestedAmountP = new BigDecimal("" + requestedAmount);

        BigDecimal minLendersRate = calculateRate(requestedAmount, lenders);

        // https://en.wikipedia.org/wiki/Mortgage_calculator#Monthly_payment_formula
        // r*P/(1 - (1+r)^(-N))      if r != 0
        // P/N                          if r == 0
        BigDecimal monthlyRepayment;
        if (minLendersRate.compareTo(BigDecimal.ZERO) == 0) {
            monthlyRepayment = requestedAmountP.divide(DURATION_MONTHS, MathContext.DECIMAL128);
        } else {
            BigDecimal r = calculateEffectiveInterestInMonths(minLendersRate);
            BigDecimal tmp = r.add(BigDecimal.ONE).pow(-DURATION_MONTHS.intValue(), MathContext.DECIMAL128);
            BigDecimal dividend = BigDecimal.ONE.subtract(tmp);
            monthlyRepayment = r.multiply(requestedAmountP).divide(dividend, MathContext.DECIMAL128);
        }
        BigDecimal totalRepayment = monthlyRepayment.multiply(DURATION_MONTHS);

        return Optional.of(new BorrowerInfo(requestedAmountP, minLendersRate, monthlyRepayment, totalRepayment));
    }

    private BigDecimal calculateRate(double requestedAmount, List<Lender> lenders) {
        List<Lender> sortedLenders = lenders.stream().sorted(Comparator.comparing(Lender::getRate)).collect(toList());
        List<BigDecimal> moneyLended = moneyLended(requestedAmount, sortedLenders);

        BigDecimal totalMoneyLended = IntStream.range(0, moneyLended.size())
                .mapToObj(index -> moneyLended.get(index).multiply(sortedLenders.get(index).getRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalMoneyLended.divide(new BigDecimal("" + requestedAmount), MathContext.DECIMAL128);
    }

    private List<BigDecimal> moneyLended(double requestedAmount, List<Lender> sortedLenders) {
        List<BigDecimal> moneyLended = new ArrayList<>(sortedLenders.size());

        int i = 0;
        Double remaining = requestedAmount;
        while (remaining > 0) {
            if (remaining - sortedLenders.get(i).getAvailable().doubleValue() >= 0) {
                remaining = remaining - sortedLenders.get(i).getAvailable().doubleValue();

            } else {
                remaining = 0d;
            }
            moneyLended.add(sortedLenders.get(i).getAvailable());
            i++;
        }
        return moneyLended;
    }

    private BigDecimal calculateEffectiveInterestInMonths(BigDecimal nominalInterest) {
        // Formula: ((1+nominalInterest)^(1/12))-1
        BigDecimal exp = BigDecimal.ONE.divide(new BigDecimal("12"), MathContext.DECIMAL128);
        return BigDecimalMath.pow(BigDecimal.ONE.add(nominalInterest), exp, MathContext.DECIMAL128)
                .subtract(BigDecimal.ONE);
    }

}
