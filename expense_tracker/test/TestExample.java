// package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.*;

import java.io.IOException;
import java.text.ParseException;
import java.awt.Color;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
import model.Filter.AmountFilter;
import model.Filter.CategoryFilter;
import view.ExpenseTrackerView;

public class TestExample {

    private ExpenseTrackerModel model;
    private ExpenseTrackerView view;
    private ExpenseTrackerController controller;

    @Before
    public void setup() {
        model = new ExpenseTrackerModel();
        view = new ExpenseTrackerView();
        controller = new ExpenseTrackerController(model, view);
    }

    public double getTotalCost() {
        double totalCost = 0.0;
        List<Transaction> allTransactions = model.getTransactions(); // Using the model's getTransactions method
        for (Transaction transaction : allTransactions) {
            totalCost += transaction.getAmount();
        }
        return totalCost;
    }

    public void checkTransaction(double amount, String category, Transaction transaction) {
        assertEquals(amount, transaction.getAmount(), 0.01);
        assertEquals(category, transaction.getCategory());
        String transactionDateString = transaction.getTimestamp();
        Date transactionDate = null;
        try {
            transactionDate = Transaction.dateFormatter.parse(transactionDateString);
        } catch (ParseException pe) {
            pe.printStackTrace();
            transactionDate = null;
        }
        Date nowDate = new Date();
        assertNotNull(transactionDate);
        assertNotNull(nowDate);
        // They may differ by 60 ms
        assertTrue(nowDate.getTime() - transactionDate.getTime() < 60000);
    }

    @Test
    public void testModelAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add a transaction
        double amount = 50.0;
        String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);

        // Post-condition: List of transactions contains only the added transaction
        assertEquals(1, model.getTransactions().size());

        // Check the contents of the list
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

        // Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
    }

    @Test
    public void testModelRemoveTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add and remove a transaction
        double amount = 50.0;
        String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);

        // Pre-condition: List of transactions contains only the added transaction
        assertEquals(1, model.getTransactions().size());
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);

        assertEquals(amount, getTotalCost(), 0.01);

        // Perform the action: Remove the transaction
        model.removeTransaction(addedTransaction);

        // Post-condition: List of transactions is empty
        List<Transaction> transactions = model.getTransactions();
        assertEquals(0, transactions.size());

        // Check the total cost after removing the transaction
        double totalCost = getTotalCost();
        assertEquals(0.00, totalCost, 0.01);
    }

    // Test Case 1: Controller - Add Transaction Succeeds
    @Test
    public void testControllerAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add and remove a transaction
        double amount = 50.0;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));

        // Pre-condition: List of transactions contains only the added transaction
        assertEquals(1, model.getTransactions().size());
        Transaction firstTransaction = model.getTransactions().get(0);
        checkTransaction(amount, category, firstTransaction);
        assertEquals(amount, getTotalCost(), 0.01); 
    }

    // Test Case 2: Model - Add Transaction Fails due to Invalid Input
    @Test
    public void testModelInvalidInputHandling() {
        // Pre-condition: Get the initial list and cost
        List<Transaction> transactions = model.getTransactions();
        double initialCost = getTotalCost();

        double validAmount = 50.0;
        String validCategory = "food";

        double[] invalidAmounts = new double[] { -50.0, 1001.0, 0.0 };
        String[] invalidCategories = new String[] { " ", "0123", "UMass" };

        // Test using invalid amounts and a valid category
        for (int i = 0; i < invalidAmounts.length; ++i) {
            try {
                // Perform the action: Add an invalid transaction
                model.addTransaction(new Transaction(invalidAmounts[i], validCategory));

                // Force-fail the test if it reaches this point without throwing an exception
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // Assert that an exception is thrown
                assertEquals("The amount is not valid.", e.getMessage());
            }

            // Post-Condition: No change in the transaction list and cost
            assertEquals(transactions, model.getTransactions());
            assertEquals(initialCost, getTotalCost(), 0.01);
        }

        // Test using a valid amount and invalid categories
        for (int i = 0; i < invalidCategories.length; ++i) {
            try {
                // Perform the action: Add an invalid transaction
                model.addTransaction(new Transaction(validAmount, invalidCategories[i]));

                // Force-fail the test if it reaches this point without throwing an exception
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // Assert that an exception is thrown
                assertEquals("The category is not valid.", e.getMessage());
            }

            // Post-Condition: No change in the transaction list and cost
            assertEquals(transactions, model.getTransactions());
            assertEquals(initialCost, getTotalCost(), 0.01);
        }
    }

    // Test Case 3: Controller - Filter by Amount Succeeds
    @Test
    public void testControllerFilterByAmountSucceeds() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add transactions with different amounts
        double[] amounts = new double[] { 100.0, 200.0, 100.0, 500.0, 100.0 };
        String category = "food";
        for (double amount : amounts) {
            assertTrue(controller.addTransaction(amount, category));
        }

        // Pre-condition: List of transactions contains only the added transaction
        assertEquals(5, model.getTransactions().size());
        for (int i = 0; i < 5; ++i) {
            Transaction currTransaction = model.getTransactions().get(i);
            checkTransaction(amounts[i], category, currTransaction);
        }

        // Perform the action: Apply amount filter
        controller.setFilter(new AmountFilter(100.0));
        controller.applyFilter();

        // Post-condition: Only rows with amount = 100.0 are highlighted
        Set<Integer> highlightedRows = new HashSet<Integer>(Arrays.asList(0, 2, 4));
        for (int row = 0; row < view.getTransactionsTable().getRowCount(); ++row) {
            for (int col = 0; col < view.getTransactionsTable().getColumnCount(); ++col) {
                Color actualColor = view.getCellBackgroundColor(row, col);
                Color expectedColor;
                if (highlightedRows.contains(row)) {
                    expectedColor = new Color(173, 255, 168);
                } else {
                    expectedColor = new Color(255, 255, 255);
                }
                assertEquals(expectedColor, actualColor);
            }
        }
    }

    // Test Case 3: Controller - Filter by Amount Fails on Invalid Input
    @Test
    public void testControllerFilterByAmountFailsOnInvalidInput() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add transactions with different amounts
        double[] amounts = new double[] { 100.0, 200.0, 100.0, 500.0, 100.0 };
        String category = "food";
        for (double amount : amounts) {
            assertTrue(controller.addTransaction(amount, category));
        }

        // Pre-condition: List of transactions contains only the added transaction
        assertEquals(5, model.getTransactions().size());
        for (int i = 0; i < 5; ++i) {
            Transaction currTransaction = model.getTransactions().get(i);
            checkTransaction(amounts[i], category, currTransaction);
        }

        // Perform the action: Try to apply amount filter
        try {
            controller.setFilter(new AmountFilter(0.0));
            controller.applyFilter();

            // Force-fail the test if it reaches this point without throwing an exception
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // Assert that an exception is thrown
            assertEquals("Invalid amount filter", e.getMessage());
        }

        // Post-Condition: No rows are highlighted
        for (int row = 0; row < view.getTransactionsTable().getRowCount(); ++row) {
            for (int col = 0; col < view.getTransactionsTable().getColumnCount(); ++col) {
                Color actualColor = view.getCellBackgroundColor(row, col);
                Color expectedColor = new Color(255, 255, 255);
                assertEquals(expectedColor, actualColor);
            }
        }
    }

    // Test Case 4: Controller - Filter by Category Succeeds
    @Test
    public void testControllerFilterByCategorySucceeds() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add transactions with different amounts
        double amount = 100.0;
        String[] categories = new String[] {"food", "other", "food", "food", "entertainment"};
        for (String category: categories) {
            assertTrue(controller.addTransaction(amount, category));
        }

        // Pre-condition: List of transactions contains only the added transaction
        assertEquals(5, model.getTransactions().size());
        for (int i = 0; i < 5; ++i) {
            Transaction currTransaction = model.getTransactions().get(i);
            checkTransaction(amount, categories[i], currTransaction);
        }

        // Perform the action: Apply amount filter
        controller.setFilter(new CategoryFilter("food"));
        controller.applyFilter();

        // Post-condition: Only rows with amount = 100.0 are highlighted
        Set<Integer> highlightedRows = new HashSet<Integer>(Arrays.asList(0, 2, 3));
        for (int row = 0; row < view.getTransactionsTable().getRowCount(); ++row) {
            for (int col = 0; col < view.getTransactionsTable().getColumnCount(); ++col) {
                Color actualColor = view.getCellBackgroundColor(row, col);
                Color expectedColor;
                if (highlightedRows.contains(row)) {
                    expectedColor = new Color(173, 255, 168);
                } else {
                    expectedColor = new Color(255, 255, 255);
                }
                assertEquals(expectedColor, actualColor);
            }
        }
    }

    // Test Case 4: Controller - Filter by Category Fails on Invalid Input
    @Test
    public void testControllerFilterByCategoryFailsOnInvalidInput() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());

        // Perform the action: Add transactions with different amounts
        double amount = 100.0;
        String[] categories = new String[] {"food", "other", "food", "food", "entertainment"};
        for (String category: categories) {
            assertTrue(controller.addTransaction(amount, category));
        }

        // Pre-condition: List of transactions contains only the added transaction
        assertEquals(5, model.getTransactions().size());
        for (int i = 0; i < 5; ++i) {
            Transaction currTransaction = model.getTransactions().get(i);
            checkTransaction(amount, categories[i], currTransaction);
        }

        // Perform the action: Try to apply amount filter
        try {
            controller.setFilter(new CategoryFilter(" "));
            controller.applyFilter();

            // Force-fail the test if it reaches this point without throwing an exception
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // Assert that an exception is thrown
            assertEquals("Invalid category filter", e.getMessage());
        }

        // Post-Condition: No rows are highlighted
        for (int row = 0; row < view.getTransactionsTable().getRowCount(); ++row) {
            for (int col = 0; col < view.getTransactionsTable().getColumnCount(); ++col) {
                Color actualColor = view.getCellBackgroundColor(row, col);
                Color expectedColor = new Color(255, 255, 255);
                assertEquals(expectedColor, actualColor);
            }
        }
    }

    // Test Case 5: View - Undo is Disallowed
    @Test
    public void testViewUndoDisallowed() {
        // Pre-condition: List of transactions is initially empty
        assertEquals(0, model.getTransactions().size());
        
        // Perform the action: Try to undo a transaction
        assertFalse(view.getUndoBtn().isEnabled());
    }

    // Test Case 6: Controller - Undo is Disallowed
    @Test
    public void testControllerUndoAllowed() {
        // Pre-condition: Undo is disabled when list of transactions is empty
        assertEquals(0, view.getTransactionsTable().getRowCount());
        assertFalse(view.getUndoBtn().isEnabled());
        
        // Perform an action: Add a valid transaction
        double amount = 50.00;
        String category = "food";
        assertTrue(controller.addTransaction(amount, category));

        // There should be 2 rows including the transaction row and the total cost
        assertEquals(2, view.getTransactionsTable().getRowCount());
        assertEquals(50.0, getTotalCost(), 0.01);

        // Perform the action: Undo the transaction
        controller.removeTransaction(0);

        // Post-condition: Only the total cost row should be there and the cost should be 0.0 
        assertEquals(1, view.getTransactionsTable().getRowCount());
        assertEquals(0.0, getTotalCost(), 0.01);
    }

    @AfterClass
    public static void cleanup() {
        // Close resources, release memory, or perform any necessary cleanup here
        System.out.println("Cleanup after all test cases have been run");
    }

    public static void main(String[] args) throws IOException {
        Result result = JUnitCore.runClasses(TestExample.class);
    }

}
