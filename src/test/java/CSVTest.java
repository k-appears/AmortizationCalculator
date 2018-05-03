import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CSVTest {

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void extractLendersEmptyCSVPath() throws IOException {
        CSV csv = new CSV();
        csv.extractLenders("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractLendersNullCSVPath() throws IOException {
        CSV csv = new CSV();
        csv.extractLenders("");
    }

    @Test(expected = IOException.class)
    public void extractLendersNonExistingPath() throws IOException {
        CSV csv = new CSV();
        csv.extractLenders("non_existing_path/zopita");
    }

    // First line of csv is "Lender,Rate,Available"
    // Second line empty
    @Test
    public void extractLendersCorrectHeaders() throws IOException {
        File tempCSV = getCSVFile("Lender,Rate,Available");

        CSV csv = new CSV();
        csv.extractLenders(tempCSV.getPath());
    }

    // First line of csv is NOT "Lender,Rate,Available"
    @Test(expected = IllegalArgumentException.class)
    public void extractLendersInvalidHeader() throws IOException {
        File tempCSV = getCSVFile("Lender,Rate,JARL");

        CSV csv = new CSV();
        csv.extractLenders(tempCSV.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractLendersExceededColumnsInCSV() throws IOException {
        File tempCSV = getCSVFile("Lender,Rate,Available", "Caldo,0.1,100,OtraCosa");

        CSV csv = new CSV();
        csv.extractLenders(tempCSV.getPath());
    }

    // Second line of csv is "Bob,0.075,640"
    @Test
    public void extractLendersCheckFirstLender() throws IOException, URISyntaxException {
        CSV csv = new CSV();
        String csvPath = LoanTest.class.getResource("Market Data for Exercise - csv.csv").toURI().getPath();
        List<Lender> lenders = csv.extractLenders(csvPath);
        assertThat(lenders).contains(new Lender("Bob", new BigDecimal(0.075), new BigDecimal(640)));
    }

    @Test(expected = NumberFormatException.class)
    public void extractLendersIncorrectFormatCurrency() throws IOException {
        File tempCSV = getCSVFile("Lender,Rate,Available", "Caldo,0.1,ShouldBeNumber");

        CSV csv = new CSV();
        csv.extractLenders(tempCSV.getPath());
    }


    @Test(expected = NumberFormatException.class)
    public void extractLendersIncorrectFormatRate() throws IOException {
        File tempCSV = getCSVFile("Lender,Rate,Available", "Caldo,ShouldBeNumber,600");

        CSV csv = new CSV();
        csv.extractLenders(tempCSV.getPath());
    }

    @Test(expected = NumberFormatException.class)
    public void extractLendersIncorrectFormatRateFormat() throws IOException {
        File tempCSV = getCSVFile("Lender,Rate,Available",  "Caldo,ShouldBeNumber,ShouldBeNumber");

        CSV csv = new CSV();
        csv.extractLenders(tempCSV.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractLendersNegativeCurrency() throws IOException {
        File tempCSV = getCSVFile("Lender,Rate,Available", "Caldo,0.1,-600");

        CSV csv = new CSV();
        csv.extractLenders(tempCSV.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractLendersNegativeRate() throws IOException {
        File tempCSV = getCSVFile("Lender,Rate,Available", "Caldo,-0.1,600");

        CSV csv = new CSV();
        csv.extractLenders(tempCSV.getPath());
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractLendersNegativeRateAndCurrent() throws IOException {
        File tempCSV = getCSVFile("Lender,Rate,Available", "Caldo,-0.1,-600");

        CSV csv = new CSV();
        csv.extractLenders(tempCSV.getPath());
    }

    private File getCSVFile(String ... lines) throws IOException {
        StringBuilder appender = new StringBuilder();
        for(String line : lines) {
            appender.append(line);
            appender.append(System.lineSeparator());
        }
        File tempCSV = testFolder.newFile("tmp.csv");
        Files.write(tempCSV.toPath(), appender.toString().getBytes());
        return tempCSV;
    }
}
