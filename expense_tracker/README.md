# HW3 - Implementation and Testing

The homework will be based on this project named "Expense Tracker",where users will be able to add daily transactions, filter and remove them . 

## Compile

To compile the code from terminal, use the following command:
```
cd src
javac ExpenseTrackerApp.java
java ExpenseTracker
```

You should be able to view the GUI of the project upon successful compilation. 

## Java Version
This code is compiled with ```openjdk 17.0.7 2023-04-18```. Please update your JDK accordingly if you face any incompatibility issue.

## Functionality

The ExpenseTrackerApp includes the following functionality :- 

#### Add Transactions

The user can add new transactions in the application by specifying the `amount` and `category`.

The `amount` field should be greater than `0` and less than `1000`. 

The `category` field can only take in specific categories limited to the following: `food`, `travel`, `bills`, `entertainment`, or `other`.

By clicking on `Add Transaction` button, the user can successfully add it in the List of expenses. 

#### Filter Transactions

The user can filter transactions in the application by specifying the `amount` or `category` (not both) and clicking on the `Filter Transactions` button. (Note: The same input fields used for adding transactions are also used for filtering them.)

If the `amount` field contains a valid input, transactions matching that amount will be automatically highlighted in green.

If the `category` field contains a valid input, transactions matching that category will be automatically highlighted in green.

#### Undo Transactions

The user can remove transactions in the application by slecting a single row from the Transaction ist and clicking on the `Undo Transaction` button. 

If the user has `not selected any valid row` the undo button will remain `disabled`.

