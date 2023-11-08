// package test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.io.IOException;
import java.text.ParseException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
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
        }
        catch (ParseException pe) {
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
    public void testAddTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add a transaction
	    double amount = 50.0;
	    String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        // Post-condition: List of transactions contains only
	//                 the added transaction	
        assertEquals(1, model.getTransactions().size());
    
        // Check the contents of the list
	Transaction firstTransaction = model.getTransactions().get(0);
	checkTransaction(amount, category, firstTransaction);
	
	// Check the total amount
        assertEquals(amount, getTotalCost(), 0.01);
    }



    @Test
    public void testAddTransaction_2() {
        // Pre-condition: List of transactions is empty
        List<Transaction>transactions = model.getTransactions();
        double initialtotalCost = getTotalCost();
        int initialSize = transactions.size();
        
    
        // Perform the action: Add a transaction
	    double amount = 50.00;
	    String category = "food";
        assertTrue(controller.addTransaction(amount, category));
    
        assertEquals(initialSize+1, model.getTransactions().size());
    
        // Check the contents of the list
	    Transaction firstTransaction = model.getTransactions().get(0);
	    checkTransaction(amount, category, firstTransaction);
	
	// Check the total amount
        assertEquals(initialtotalCost+amount, getTotalCost(), 0.01);
    }

    @Test
    public void testInvalidInputHandling() {
        // Pre-condition: List of transactions is empty
        List<Transaction>transactions = model.getTransactions();
        double InittialtotalCost = getTotalCost();
    
        // Perform the action: Add an invalid transaction with invalid amount less than 0
	    double test_amount_1 = -50;
	    String test_category_1 = "food";
        try{
            Transaction test_addedTransaction_1 = new Transaction(test_amount_1,test_category_1 );
            model.addTransaction(test_addedTransaction_1);
        }catch(IllegalArgumentException e)
        {
            assertEquals("The amount is not valid.", e.getMessage());
        }
       
        // Pre-condition: List of transactions contains only
	//                the added transaction
       assertEquals(transactions, model.getTransactions());
       assertEquals(InittialtotalCost,getTotalCost(),0.01);

        double test_amount_2 = 1001;
	    String test_category_2 = "food";
        try{
            Transaction test_addedTransaction_2 = new Transaction(test_amount_2,test_category_2);
            model.addTransaction(test_addedTransaction_2);
        }catch(IllegalArgumentException e)
        {
            assertEquals("The amount is not valid.", e.getMessage());
        }

        assertEquals(transactions, model.getTransactions());
        assertEquals(InittialtotalCost,getTotalCost(),0.01);

        double test_amount_3 = 0;
	    String test_category_3 = "food";
       try{
            Transaction test_addedTransaction_3 = new Transaction(test_amount_3,test_category_3);
            model.addTransaction(test_addedTransaction_3);
        }catch(IllegalArgumentException e)
        {
            assertEquals("The amount is not valid.", e.getMessage());
        }

        assertEquals(transactions, model.getTransactions());
        assertEquals(InittialtotalCost,getTotalCost(),0.01);

        double test_amount_4 = 500;
	    String test_category_4 = "   ";
        try{
            Transaction test_addedTransaction_4 = new Transaction(test_amount_4,test_category_4);
            model.addTransaction(test_addedTransaction_4);
        }catch(IllegalArgumentException e)
        {
            assertEquals("The category is not valid.", e.getMessage());
        }

        assertEquals(transactions, model.getTransactions());
        assertEquals(InittialtotalCost,getTotalCost(),0.01);

        double test_amount_5 = 500;
	    String test_category_5 = "0123";
       try{
            Transaction test_addedTransaction_5 = new Transaction(test_amount_5,test_category_5);
            model.addTransaction(test_addedTransaction_5);
        }catch(IllegalArgumentException e)
        {
            assertEquals("The category is not valid.", e.getMessage());
        }

        assertEquals(transactions, model.getTransactions());
        assertEquals(InittialtotalCost,getTotalCost(),0.01);
    }




    @Test
    public void testRemoveTransaction() {
        // Pre-condition: List of transactions is empty
        assertEquals(0, model.getTransactions().size());
    
        // Perform the action: Add and remove a transaction
	double amount = 50.0;
	String category = "food";
        Transaction addedTransaction = new Transaction(amount, category);
        model.addTransaction(addedTransaction);
    
        // Pre-condition: List of transactions contains only
	//                the added transaction
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


    @AfterClass
    public static void cleanup() {
        // Close resources, release memory, or perform any necessary cleanup here
        System.out.println("Cleanup after all test cases have been run");
      
    }

     public static void main(String[] args) throws IOException {
        Result result = JUnitCore.runClasses(TestExample.class);
       
        
    }
    
}
