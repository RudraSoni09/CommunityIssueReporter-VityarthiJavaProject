package src;

import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.Comparator;

public class CIRPAnalyzer {

    private ComplaintManager manager;
    private Scanner consoleScanner; // Renamed Scanner instance

    // Constructor to set up manager and scanner
    public CIRPAnalyzer() {
        this.manager = new ComplaintManager();
        this.consoleScanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  COMMUNITY COMPLAINT LOGGER & PATTERN ANALYZER");
        System.out.println("==================================================");
        
        CIRPAnalyzer app = new CIRPAnalyzer();
        app.startInterface(); // Renamed main loop method
    }

    // Renamed main loop method
    private void startInterface() {
        int userChoice = -1;
        while (userChoice != 6) {
            showMainMenu();
            
            // Gracefully handle exceptions when user inputs text instead of a number
            try {
                System.out.print("Enter your menu selection (1-6): ");
                userChoice = consoleScanner.nextInt();
                consoleScanner.nextLine(); // Consume the remaining newline

                switch (userChoice) {
                    case 1: logNewComplaint(); break;
                    case 2: displayAllComplaints(); break;
                    case 3: changeComplaintStatus(); break;
                    case 4: showTrendReport(); break;
                    case 5: handleDataMenu(); break;
                    case 6: closeApplication(); break;
                    default: System.out.println("\n❌ Unknown selection. Please enter a number from the menu.");
                }
            } catch (InputMismatchException e) {
                System.err.println("\n🛑 Input Error: Please enter a valid number for your choice.");
                consoleScanner.nextLine(); // Clear the bad input from the buffer
                userChoice = -1; 
            } catch (Exception e) {
                System.err.println("\nAn unexpected system error occurred: " + e.getMessage());
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n--- COMPLAINT TRACKING SYSTEM ---");
        System.out.println("1. Log New Complaint");
        System.out.println("2. View All Active & Closed Complaints");
        System.out.println("3. Change Complaint Status (Review/Close)");
        System.out.println("4. Generate Trend Report (Analyze Hotspots)");
        System.out.println("5. Manual Data Backup/Restore (File I/O)");
        System.out.println("6. Shut Down Application");
        System.out.println("---------------------------------");
    }

    private void logNewComplaint() {
        System.out.println("\n--- NEW COMPLAINT LOG ---");
        
        try {
            System.out.print("Enter Zone Number (e.g., 1-10): ");
            int zoneNumber = consoleScanner.nextInt();
            consoleScanner.nextLine(); 

            System.out.print("Enter Complaint Details (brief description): ");
            String details = consoleScanner.nextLine();
            
            // Show categories for user selection
            System.out.println("\nSelect Category:");
            IssueCategory[] categories = IssueCategory.values();
            for (int i = 0; i < categories.length; i++) {
                System.out.println((i + 1) + ". " + categories[i]);
            }
            System.out.print("Enter the category number: ");
            int categoryChoice = consoleScanner.nextInt();
            consoleScanner.nextLine();

            if (categoryChoice < 1 || categoryChoice > categories.length) {
                System.err.println("❌ Invalid category selection. Operation cancelled.");
                return;
            }
            IssueCategory selectedCategory = categories[categoryChoice - 1];

            manager.logNewComplaint(zoneNumber, details, selectedCategory);

        } catch (InputMismatchException e) {
            System.err.println("🛑 Invalid input for Zone Number or Category. Please try again.");
            consoleScanner.nextLine(); 
        }
    }

    private void displayAllComplaints() {
        System.out.println("\n--- FULL COMPLAINTS LIST ---");
        var complaints = manager.getAllComplaints();
        if (complaints.isEmpty()) {
            System.out.println("No complaints currently logged in the system.");
            return;
        }

        // Display the list using the overridden toString method
        complaints.forEach(System.out::println);
    }

    private void changeComplaintStatus() {
        System.out.println("\n--- UPDATE COMPLAINT STATUS ---");
        try {
            System.out.print("Enter the Tracking ID to update: ");
            int trackingID = consoleScanner.nextInt();
            consoleScanner.nextLine();
            
            // Show status options for user selection (starting from 1 to skip SUBMITTED)
            System.out.println("\nSelect New Status:");
            ComplaintStatus[] statuses = ComplaintStatus.values();
            System.out.println("1. " + statuses[1]); // IN_REVIEW
            System.out.println("2. " + statuses[2]); // CLOSED
            
            System.out.print("Enter status number (1 or 2): "); 
            int statusChoice = consoleScanner.nextInt();
            consoleScanner.nextLine();
            
            ComplaintStatus newStatus;
            if (statusChoice == 1) {
                newStatus = ComplaintStatus.IN_REVIEW;
            } else if (statusChoice == 2) {
                newStatus = ComplaintStatus.CLOSED;
            } else {
                System.err.println("❌ Invalid status selection. Update cancelled.");
                return;
            }

            if (manager.modifyStatus(trackingID, newStatus)) {
                System.out.println("✅ Tracking ID " + trackingID + " status updated to " + newStatus + ".");
            } else {
                System.err.println("❌ Complaint Tracking ID " + trackingID + " not found.");
            }

        } catch (InputMismatchException e) {
            System.err.println("🛑 Invalid input for Tracking ID. Update failed.");
            consoleScanner.nextLine();
        }
    }

    private void showTrendReport() {
        System.out.println("\n--- PATTERN ANALYSIS: OPEN COMPLAINT TRENDS ---");
        System.out.println("Counting issues that are SUBMITTED or IN_REVIEW.\n");
        
        Map<IssueCategory, Integer> report = manager.getTrendAnalysis();

        if (report.isEmpty()) {
            System.out.println("No open complaints to analyze.");
            return;
        }

        // Print header
        System.out.printf("%-25s | %s%n", "PROBLEM CATEGORY", "OPEN COUNT");
        System.out.println("--------------------------|-----------");

        // Sort results to highlight high-frequency areas first
        report.entrySet().stream()
              .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
              .forEach(entry -> {
                  String trend = (entry.getValue() > 3) ? " (HIGH HOTSPOT!)" : "";
                  System.out.printf("%-25s | %-10d %s%n", entry.getKey(), entry.getValue(), trend);
              });
        System.out.println("--------------------------|-----------");
    }

    private void handleDataMenu() {
        System.out.println("\n--- DATA PERSISTENCE & BACKUP OPTIONS ---");
        System.out.println("1. Manually Save Current Data to File Backup");
        System.out.println("2. Manually Restore Data From File Backup");
        System.out.println("3. Back to Main Menu");
        System.out.print("Enter choice (1-3): ");

        try {
            int choice = consoleScanner.nextInt();
            consoleScanner.nextLine();

            switch (choice) {
                case 1: manager.saveBackupToFile(); break;
                case 2: manager.loadBackupFromFile(); break;
                case 3: break;
                default: System.out.println("❌ Invalid choice.");
            }
        } catch (InputMismatchException e) {
            System.err.println("🛑 Invalid Input. Returning to main menu.");
            consoleScanner.nextLine(); 
        }
    }
    
    // Gracefully shuts down the application
    private void closeApplication() {
        System.out.println("\n--- SYSTEM SHUTDOWN IN PROGRESS ---");
        
        // Ensure both persistence methods are called before termination
        manager.saveDataToDatabase();
        manager.saveBackupToFile();
        
        System.out.println("All data synchronized and backed up. Thank you.");
        consoleScanner.close(); 
    }
}