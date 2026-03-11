import com.restaurant.controller.BillController;
import com.restaurant.controller.OrderController;
import com.restaurant.model.Bill;

public class TestBill {
    public static void main(String[] args) {
        System.out.println("Starting test...");
        // Get order 2 for table 5
        try {
            Bill bill = BillController.generateBill(2, 5, 0, 5);
            if (bill == null) {
                System.out.println("Bill is null!");
            } else {
                System.out.println("Success! Bill ID: " + bill.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
