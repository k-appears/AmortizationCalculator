
# Kata
https://en.wikipedia.org/wiki/Amortization_calculator

Borrowers should be able to request a loan of any 100€ increment between 1000€ and 15000€ inclusive.

Example:

```
java -jar quote.jar market.csv 1000

Requested amount: 1000
Rate: 7.0%
Monthly repayment: £30.78 Total repayment: 1108.10
```
# Purpose
The purpose of this repo is to understand why the results are close but not exact, sice [Stackoverflow question](https://stackoverflow.com/questions/50090589) was not very popular, please feel free to Pull Request or create issue.

# Consideration
Not tested with CSV files with size bigger than 300MB
See [CSV example](https://github.com/k-appears/AmortizationCalculator/blob/master/src/test/resources/Market%20Data%20for%20Exercise%20-%20csv.csv)
