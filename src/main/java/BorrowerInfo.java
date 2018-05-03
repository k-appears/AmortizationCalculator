import java.math.BigDecimal;

class BorrowerInfo {

    // Requested amount: £XXXX
    final BigDecimal requestedAmount;
    // Rate: X.X%
    final BigDecimal rate;
    // Monthly repayment: £XXXX.XX
    final BigDecimal monthlyRepayment;
    // Total repayment: £XXXX.XX
    final BigDecimal totalRepayment;

    BorrowerInfo(BigDecimal requestedAmount, BigDecimal rate, BigDecimal monthlyRepayment, BigDecimal totalRepayment) {
        this.requestedAmount = requestedAmount;
        this.rate = rate;
        this.monthlyRepayment = monthlyRepayment;
        this.totalRepayment = totalRepayment;
    }

    BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    BigDecimal getRate() {
        return rate;
    }

    BigDecimal getMonthlyRepayment() {
        return monthlyRepayment;
    }

    BigDecimal getTotalRepayment() {
        return totalRepayment;
    }
}
