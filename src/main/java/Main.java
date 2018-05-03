import java.math.BigDecimal;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Arguments to run this program can not be empty");
        }
        if (args.length != 2) {
            throw new IllegalArgumentException("The number of Arguments to run this program should be 2");
        }
        double loanAmount;
        try {
            loanAmount = Double.valueOf(args[1]);
        } catch (NumberFormatException ne) {
            throw new IllegalArgumentException("The second argument must be a number");
        }

        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(args[0], loanAmount, new CSV());

        // Repayment amounts should be displayed to 2 decimal places and
        // the rate of the loan should be displayed to one decimal place.
        //
        // Requested amount: £XXXX
        // Rate: X.X%
        // Monthly repayment: £XX.XX
        // Total repayment: £XXXX.XX
        if (borrowerInfo.isPresent()) {
            BorrowerInfo borrowerInfo1 = borrowerInfo.get();
            System.out.println(String.format("Requested amount: £%d", borrowerInfo1.getRequestedAmount().intValue()));
            System.out.println(String.format("Rate: %1$,.1f%%",
                    borrowerInfo1.getRate().multiply(new BigDecimal("100")).setScale(1, BigDecimal.ROUND_DOWN).doubleValue()));
            System.out.println(String.format("Monthly repayment: £%1$.2f",
                    borrowerInfo1.getMonthlyRepayment().setScale(2, BigDecimal.ROUND_DOWN).doubleValue()));
            System.out.println(String.format("Total repayment: £%1$.2f",
                    borrowerInfo1.getTotalRepayment().setScale(2, BigDecimal.ROUND_DOWN).doubleValue()));
        } else {
            System.out.println("It is not possible to provide a quote at that time.");
        }
    }

}
