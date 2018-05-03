# Feedback
Close results, not exact.

# Purpose
The purpose of this repo is to understand why the results are close but not exact, sice [Stackoverflow question](https://stackoverflow.com/questions/50090589) was not very popular, please feel free to Pull Request or create issue.

# Suposition
Floating point using BigDecimal carries over a decimal error


# Consideration
Not tested with CSV files with size bigger than 300MB
See [CSV example](src/test/resources/Market Data for Exercise - csv.csv)


# Exercise

> There is a need for a rate calculation system allowing prospective borrowers to obtain a quote from our pool of lenders for 36 month loans. This system will take the form of a command-line application.

> You will be provided with a file containing a list of all the offers being made
by the lenders within the system in CSV format, see the example market.csv file provided alongside this specification.
You should strive to provide as low a rate to the borrower as is possible to ensure that Zopa's quotes are as competitive as they can be against our competitors'. You should also provide the borrower with the details of the monthly repayment amount and the total repayment amount.

> Repayment amounts should be displayed to 2 decimal places and the rate of the loan should be displayed to one decimal place.
Borrowers should be able to request a loan of any £100 increment between £1000 and £15000 inclusive. If the market does not have sufficient offers from lenders to satisfy the loan then the system should inform the borrower that it is not possible to provide a quote at that time.

> The application should take arguments in the form:
Example:

```
 java -jar quote.jar market.csv 1000

Requested amount: £1000
Rate: 7.0%
Monthly repayment: £30.78 Total repayment: £1108.10
```