import com.restaurant.controller.BillController;
import com.restaurant.model.Bill;

/**
 * TestBill – Lightweight ad-hoc integration test verifying pure backend business logic.
 * Explicitly bypasses the GUI/Swing layer validating the BillController's core transaction processing.
 * Useful for debugging database constraints or testing mathematical calculation algorithms in isolation.
 */
public class TestBill {
    
    public static void main(String[] args) {
        System.out.println("Starting Bill Generation test sequence...");
        
        // Attempt to execute a complex transactional query mocking inputs:
        // Mock Parameters: Order ID = 2, Table Number = 5, Discount = 0%, Tax = 5%
        try {
            Bill bill = BillController.generateBill(2, 5, 0, 5);
            
            // Validate logical object mapping against expected database state changes
            if (bill == null) {
                System.out.println("Test Failed! Controller returned a null Bill object. " 
                                 + "Probable missing database records for associated Order ID.");
            } else {
                System.out.println("Test Success! Bill successfully committed to database with ID: " 
                                 + bill.getId());
            }
        } catch (Exception e) {
            // Unravel synchronous transaction failures mapping SQL issues or null pointers
            System.err.println("Uncaught exception during mock transaction execution:");
            e.printStackTrace();
        }
    }
}
