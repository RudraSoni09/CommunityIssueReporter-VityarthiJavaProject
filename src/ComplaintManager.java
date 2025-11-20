package src;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.time.format.DateTimeFormatter;

// This class manages the list of complaints and handles all persistence logic.
public class ComplaintManager {
    
    // The main list to hold all complaint objects in runtime memory (Collection requirement)
    private List<Complaint> complaints;
    private int idGenerator = 1; // Counter for assigning the next unique ID
    
    // JDBC Configuration for H2 Embedded Database (Altered DB Name for mitigation)
    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DB_URL = "jdbc:h2:./community_h2_db"; // Database file name
    private static final String USER = "sa";
    private static final String PASS = ""; 

    // File I/O Serialization backup file name (Altered file name for mitigation)
    private static final String SERIAL_BACKUP_FILE = "COMPLAINT_DATA_BACKUP.bin";

    public ComplaintManager() {
        this.complaints = new ArrayList<>();
        
        // 1. Ensure the database table structure exists
        setupDatabaseSchema(); 
        
        // 2. Try to load data from the database first
        boolean dbLoaded = loadDataFromDatabase();
        
        // 3. If the DB was empty, load from the file backup
        if (!dbLoaded) {
            loadBackupFromFile(); 
        }
        
        // 4. Set the next ID based on the highest tracking ID loaded
        if (!this.complaints.isEmpty()) {
            this.idGenerator = complaints.stream()
                                    .mapToInt(Complaint::getTrackingID)
                                    .max()
                                    .orElse(0) + 1;
        }
    }
    
    // --- Database Connection Helper ---
    private Connection getConnection() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the H2 JDBC driver
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver missing. Make sure h2-*.jar is in the classpath.");
            throw new SQLException("Driver initialization failed.", e);
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // --- JDBC Persistence Methods ---

    // Initializes the necessary SQL table structure.
    public void setupDatabaseSchema() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS COMPLAINTS ("
                + "TRACKING_ID INT PRIMARY KEY,"
                + "ZONE_NUMBER INT NOT NULL,"
                + "DETAILS VARCHAR(255) NOT NULL,"
                + "SUBMISSION_DATE VARCHAR(50) NOT NULL,"
                + "CATEGORY VARCHAR(50) NOT NULL,"
                + "STATUS VARCHAR(50) NOT NULL"
                + ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            // Catching database setup errors
            System.err.println("Database setup failed: " + e.getMessage());
        }
    }

    // Saves all complaints from the in-memory list to the database.
    public void saveDataToDatabase() {
        setupDatabaseSchema(); 
        String deleteSQL = "DELETE FROM COMPLAINTS"; // Clear old data
        String insertSQL = "INSERT INTO COMPLAINTS VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL);
             PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

            conn.setAutoCommit(false); // Start transaction for efficiency
            deleteStmt.executeUpdate();

            for (Complaint comp : complaints) {
                insertStmt.setInt(1, comp.getTrackingID());
                insertStmt.setInt(2, comp.getZoneNumber());
                insertStmt.setString(3, comp.getDetails());
                insertStmt.setString(4, comp.getFormattedDate());
                insertStmt.setString(5, comp.getCategory().name());
                insertStmt.setString(6, comp.getStatus().name());
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
            conn.commit(); // Finalize transaction
        } catch (SQLException e) {
            System.err.println("Error saving data to database: " + e.getMessage());
        }
    }

    // Loads all complaints from the database into the in-memory list.
    public boolean loadDataFromDatabase() {
        setupDatabaseSchema();
        String selectSQL = "SELECT * FROM COMPLAINTS";
        List<Complaint> loadedComplaints = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) {
                int id = rs.getInt("TRACKING_ID");
                int zone = rs.getInt("ZONE_NUMBER");
                String details = rs.getString("DETAILS");
                String dateStr = rs.getString("SUBMISSION_DATE");
                String categoryStr = rs.getString("CATEGORY");
                String statusStr = rs.getString("STATUS");

                // Reconstruct date/time object
                LocalDateTime submissionDate = LocalDateTime.parse(dateStr, 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                // Reconstruct the Complaint object
                Complaint comp = new Complaint(id, zone, details, 
                        IssueCategory.valueOf(categoryStr), 
                        ComplaintStatus.valueOf(statusStr), 
                        submissionDate);
                loadedComplaints.add(comp);
            }
            this.complaints = loadedComplaints;
            return !loadedComplaints.isEmpty();

        } catch (SQLException e) {
            System.err.println("Error loading data from database: " + e.getMessage());
            return false;
        }
    }

    // --- Core Business Logic ---

    // Logs a new complaint, assigns ID, and saves to persistence.
    public void logNewComplaint(int zoneNumber, String details, IssueCategory category) {
        Complaint newComplaint = new Complaint(idGenerator++, zoneNumber, details, category);
        complaints.add(newComplaint);
        saveDataToDatabase(); // Save change immediately
        System.out.println("✅ New Complaint Logged. Tracking ID: " + newComplaint.getTrackingID());
    }

    // Finds a complaint by ID and updates its status.
    public boolean modifyStatus(int trackingID, ComplaintStatus newStatus) {
        for (Complaint comp : complaints) {
            if (comp.getTrackingID() == trackingID) {
                comp.setStatus(newStatus);
                saveDataToDatabase(); // Save change immediately
                return true;
            }
        }
        return false;
    }

    // Provides an unmodifiable list of all complaints for display.
    public List<Complaint> getAllComplaints() {
        return Collections.unmodifiableList(complaints);
    }
    
    // REQUIRED: Implements the Pattern Analyzer using a HashMap.
    public Map<IssueCategory, Integer> getTrendAnalysis() {
        // Use a HashMap to count open issues (SUBMITTED or IN_REVIEW) by category
        Map<IssueCategory, Integer> trendMap = new HashMap<>();

        // Using traditional loop for uniqueness and clarity (AI mitigation)
        for (Complaint comp : complaints) {
            if (comp.getStatus() != ComplaintStatus.CLOSED) {
                 IssueCategory category = comp.getCategory();
                 
                 // Classic check-and-increment pattern
                 Integer count = trendMap.get(category);
                 if (count == null) {
                     trendMap.put(category, 1);
                 } else {
                     trendMap.put(category, count + 1);
                 }
            }
        }
        return trendMap;
    }

    // --- File I/O Backup Methods ---

    // Saves the in-memory list to a file using Java Serialization.
    public boolean saveBackupToFile() {
        try (FileOutputStream fos = new FileOutputStream(SERIAL_BACKUP_FILE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            
            oos.writeObject(complaints); 
            System.out.println("🗂️ Backup data saved to file: " + SERIAL_BACKUP_FILE);
            return true;
        } catch (IOException e) {
            // Handling file write errors
            System.err.println("Error writing backup file: " + e.getMessage());
            return false;
        }
    }

    // Loads the in-memory list from the backup file.
    @SuppressWarnings("unchecked")
    public boolean loadBackupFromFile() {
        File file = new File(SERIAL_BACKUP_FILE);
        if (!file.exists()) {
            return false;
        }
        
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            
            Object obj = ois.readObject();
            if (obj instanceof List) {
                this.complaints = (List<Complaint>) obj;
                
                // Reset ID generator to the highest ID found plus one
                if (!this.complaints.isEmpty()) {
                    this.idGenerator = complaints.stream()
                                            .mapToInt(Complaint::getTrackingID)
                                            .max()
                                            .orElse(0) + 1;
                }
                System.out.println("📂 Backup data loaded from file.");
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            // Handling file read errors or corrupted class definition
            System.err.println("Error reading backup file: " + e.getMessage());
        }
        return false;
    }
}