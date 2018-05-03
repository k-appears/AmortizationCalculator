import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

public class MainTest {

    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final String CSV = "Market Data for Exercise - csv.csv";

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void tearDownStreams() {
        outContent.reset();
        System.setIn(System.in);
        System.setOut(System.out);
    }

    @Test
    public void main() throws URISyntaxException {
        String csv = LoanTest.class.getResource("Market Data for Exercise - csv.csv").toURI().getPath();
        Main.main(new String[] {csv, "1000"});

        assertThat(outContent.toString()).contains("Requested amount: £1000");
        assertThat(outContent.toString()).contains("Rate: 7.0%");
        assertThat(outContent.toString()).contains("Monthly repayment: £30.78");
        assertThat(outContent.toString()).contains("Total repayment: £1108.10");
    }

    @Test
    public void mainInvalidCVS() throws URISyntaxException {
        String csv = LoanTest.class.getResource(CSV).toURI().getPath();
        Main.main(new String[] {csv+"Invalid", "1000"});

        assertThat(outContent.toString()).contains("It is not possible to provide a quote at that time");
    }


    @Test(expected = IllegalArgumentException.class)
    public void mainInvalidFormatRequestAmount() throws URISyntaxException {
        String csv = LoanTest.class.getResource(CSV).toURI().getPath();
        Main.main(new String[] {csv+"Invalid", "1000NotANumber"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void mainInvalidNotArguments() {
        Main.main(new String[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void mainInvalidThreeArguments() throws URISyntaxException {
        String csv = LoanTest.class.getResource(CSV).toURI().getPath();
        Main.main(new String[] {csv, "10000" , "Another argument"});
    }
}