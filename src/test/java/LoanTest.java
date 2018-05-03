import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class LoanTest {

    private static String CSV;

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @BeforeClass
    public static void before() throws URISyntaxException {
        CSV = LoanTest.class.getResource("Market Data for Exercise - csv.csv").toURI().getPath();
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateNegative() {
        Loan loan = new Loan();
        loan.calculate(CSV, -1, new CSV());
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateZero() {
        Loan loan = new Loan();
        loan.calculate(CSV, 0, new CSV());
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateLessThan1000() {
        Loan loan = new Loan();
        loan.calculate(CSV, 999.99, new CSV());
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateMoreThan1500() {
        Loan loan = new Loan();
        loan.calculate(CSV, 1500.01, new CSV());
    }

    @Test
    public void calculateIncorrectCSVPath() throws IOException {
        Loan loan = new Loan();
        File tempCSV = getCSVFile();

        assertThat(loan.calculate(tempCSV.getPath() + "incorrect_path", 1000, new CSV())).isEmpty();
    }

    @Test
    public void calculate1LenderNotEnoughLenders() throws IOException {
        File tempCSV = getCSVFile();

        Loan loan = new Loan();
        assertThat(loan.calculate(tempCSV.getPath(), 1000.00, new CSV())).isEmpty();
    }

    @Test
    public void calculate1LenderNotEnoughAmount() throws IOException {
        File tempCSV = getCSVFile("Bob,0.075,600");

        Loan loan = new Loan();
        assertThat(loan.calculate(tempCSV.getPath(), 1000.00, new CSV())).isEmpty();
    }

    @Test
    public void calculate1LenderCorrectUpperLimitAmount() throws IOException {
        File tempCSV = getCSVFile("Bob,0.075,18000");

        Loan loan = new Loan();
        assertThat(loan.calculate(tempCSV.getPath(), 15000.00, new CSV())).isNotEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculate1LenderExceededAmount() throws IOException {
        File tempCSV = getCSVFile("Bob,0.075,18000");

        Loan loan = new Loan();
        assertThat(loan.calculate(tempCSV.getPath(), 15001.01, new CSV())).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculate1LenderNotModule100Amount() throws IOException {
        File tempCSV = getCSVFile("Bob,0.075,18000");

        Loan loan = new Loan();
        assertThat(loan.calculate(tempCSV.getPath(), 1005.00, new CSV())).isEmpty();
    }

    @Test
    public void calculate1LenderNoDecimals() throws IOException {
        File tempCSV = getCSVFile("Bob,0.01,3600");

        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(tempCSV.getPath(), 3600.00, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.rate.setScale(3, BigDecimal.ROUND_HALF_UP)).isEqualTo(new BigDecimal("0.010"));
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("3600.0"));
            assertThat(b.monthlyRepayment)
                    .isGreaterThan(new BigDecimal("101")); // 3600 / 36  = 100 * 0.01 = 1 + 100 = 101
            assertThat(b.totalRepayment).isGreaterThan(new BigDecimal("3636")); // 101 * 36 = 3636
        });
    }

    @Test
    public void calculate1LenderWithDecimals() throws IOException {
        File tempCSV = getCSVFile( "Bob,0.01,4400");

        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(tempCSV.getPath(), 4000.00, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.rate.setScale(3, BigDecimal.ROUND_HALF_UP))
                    .isEqualTo(new BigDecimal("0.011")); // 44/4000.0 = 0.011
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("4000.0"));

            assertThat(b.monthlyRepayment)
                    .isGreaterThan(new BigDecimal("112.222")); //  111,1111111 * 0.01 = 1,111111111
            // 1,1111111 + 111,11111111= 112,22222222
            assertThat(b.totalRepayment).isGreaterThan(new BigDecimal("4040")); // 112,222222222222222 * 36 = 4040
        });
    }

    @Test
    public void calculate1LenderRate0() throws IOException {
        File tempCSV = getCSVFile( "Bob,0.0,3600.00");

        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(tempCSV.getPath(), 3600, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.rate.setScale(3, BigDecimal.ROUND_HALF_UP))
                    .isEqualTo(new BigDecimal("0.000"));
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("3600.0"));

            assertThat(b.monthlyRepayment)
                    .isEqualTo(new BigDecimal("100.0")); // 3600 / 36  = 100 * 0.00 = 0 + 100 = 100
            assertThat(b.totalRepayment).isEqualTo(new BigDecimal("3600.0")); // 100 * 36 = 3600.0
        });
    }

    @Test
    public void calculate2LendersNoDecimals_SameRate_SameAmount() throws IOException {
        File tempCSV = getCSVFile("Bob,0.02,1800", "Person2,0.02,1800");

        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(tempCSV.getPath(), 3600.00, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.rate).isEqualTo(new BigDecimal("0.02"));
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("3600.0"));
            assertThat(b.monthlyRepayment)
                    .isGreaterThan(new BigDecimal("102")); // 3600 / 36  = 100 * 0.02 = 1 + 100 = 102
            assertThat(b.totalRepayment).isGreaterThan(new BigDecimal("3672"));  // 101 * 36 = 3636
        });
    }

    @Test
    public void calculate2LendersNoDecimals_SameRate_DifferentAmount_LastLenderWithNoRemainingMoney()
            throws IOException {
        File tempCSV = getCSVFile("Bob,0.01,200", "Person2,0.01,7000");

        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(tempCSV.getPath(), 7200.00, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.rate).isEqualTo(new BigDecimal("0.01"));
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("7200.0"));
            assertThat(b.monthlyRepayment)
                    .isGreaterThan(new BigDecimal("202")); // 7200 / 36  = 200 * 0.01 = 2 + 200 = 202
            assertThat(b.totalRepayment).isGreaterThan(new BigDecimal("7272")); // 202 * 36 = 7272
        });
    }

    @Test
    public void calculate2LendersNoDecimals_SameRate_DifferentAmount_LastLenderWithRemainingMoney() throws IOException {
        File tempCSV = getCSVFile("Bob,0.01,200", "Person2,0.01,8000");

        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(tempCSV.getPath(), 7200.00, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.rate.setScale(3, BigDecimal.ROUND_HALF_UP)).isEqualTo(new BigDecimal("0.011"));
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("7200.0"));
            assertThat(b.monthlyRepayment)
                    .isGreaterThan(new BigDecimal("202")); // 7200 / 36  = 200 * 0.01 = 2 + 200 = 202
            assertThat(b.totalRepayment).isGreaterThan(new BigDecimal("7272")); // 202 * 36 = 7272
        });
    }

    @Test
    public void calculate2LendersNoDecimals_DifferentRate_SameAmount() throws IOException {
        File tempCSV = getCSVFile("Bob,0.005,1800", "Person2,0.015,1800");

        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(tempCSV.getPath(), 3600.00, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.rate.setScale(3, BigDecimal.ROUND_HALF_UP))
                    .isEqualTo(new BigDecimal("0.01").setScale(3, BigDecimal.ROUND_HALF_UP));
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("3600.0"));
            assertThat(b.monthlyRepayment)
                    .isGreaterThan(new BigDecimal("101")); // 3600 / 36  = 100  =>  100 * 0.01 = 1  => 1 + 100 = 101
            assertThat(b.totalRepayment).isGreaterThan(new BigDecimal("3636")); // 101 * 36 = 3636
        });
    }

    @Test
    public void calculate2LendersNoDecimals_DifferentRate_DifferentAmount() throws IOException {
        File tempCSV = getCSVFile("Bob,0.005,2000", "Person2,0.01625,1800");

        // 2000 * 0.005 = 10
        // 1600 * 0.01625 = 26
        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(tempCSV.getPath(), 3600.00, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("3600.0"));
            assertThat(b.rate.setScale(3, BigDecimal.ROUND_HALF_UP)).isEqualTo(new BigDecimal("0.011"));
            assertThat(b.totalRepayment).isGreaterThan(new BigDecimal("3636")); // 3600 + 10 + 26 = 3636
            assertThat(b.monthlyRepayment).isGreaterThan(new BigDecimal("101")); // 3636 / 3600  = 101
        });
    }

    @Test
    public void calculate3LendersNoDecimals_DifferentRate_DifferentAmount() throws IOException {
        File tempCSV = getCSVFile("Bob,0.01,2500", "Person2,0.02,5000", "Person3,0.02,2500");

        // 2500 * 0.001 = 25
        // 5000 * 0.02 = 100
        // 2500 * 0.02 = 50
        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(tempCSV.getPath(), 10000, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(
                (BorrowerInfo b) -> assertThat(b.rate).isEqualTo(new BigDecimal("0.0175"))); // 175 / 10000
    }

    @Test
    public void calculate1000() {
        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(CSV, 1000, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("1000.0"));
            assertThat(b.rate.setScale(3, RoundingMode.DOWN)).isEqualTo(new BigDecimal("0.070"));
            assertThat(b.monthlyRepayment.setScale(2, RoundingMode.DOWN)).isEqualTo(new BigDecimal("30.78"));
            assertThat(b.totalRepayment.setScale(2, RoundingMode.DOWN)).isEqualTo(new BigDecimal("1108.10"));
        });
    }

    @Test
    public void calculate1500() {
        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(CSV, 1500, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("1500.0"));
            assertThat(b.rate.setScale(3, BigDecimal.ROUND_HALF_UP)).isEqualTo(new BigDecimal("0.088"));
        });
    }

    @Test
    public void calculate2300() {
        Loan loan = new Loan();
        Optional<BorrowerInfo> borrowerInfo = loan.calculate(CSV, 2300, new CSV());
        assertThat(borrowerInfo).hasValueSatisfying(b -> {
            assertThat(b.requestedAmount).isEqualTo(new BigDecimal("2300.0"));
            assertThat(b.rate.setScale(5, BigDecimal.ROUND_HALF_UP)).isEqualTo(new BigDecimal("0.07663"));
            /*
            0.075 * 640
            0.069 * 480
            0.071 * 520
            0.104 * 170
            0.081 * 320
            0.074 * 140
            0.071 * 60    Total Sum = 176.26 => 2300 / 176.26 = 0,076634782608696
             */
        });
    }

    private File getCSVFile(String ... lines) throws IOException {
        StringBuilder appender = new StringBuilder("Lender,Rate,Available");
        appender.append(System.lineSeparator());
        for(String line : lines) {
            appender.append(line);
            appender.append(System.lineSeparator());
        }
        File tempCSV = testFolder.newFile("tmp.csv");
        Files.write(tempCSV.toPath(), appender.toString().getBytes());
        return tempCSV;
    }
}
