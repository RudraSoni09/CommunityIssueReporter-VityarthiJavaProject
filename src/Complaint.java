package src;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// The core data model, must be Serializable for the File I/O requirement.
public class Complaint implements Serializable {
    // Unique ID for serialization verification (altered from default LLM value)
    private static final long serialVersionUID = 20251120L; 
    
    private int trackingID;
    private int zoneNumber; // Renamed from wardNumber for uniqueness
    private String details; // Renamed from description
    private LocalDateTime submissionDate;
    
    // Uses the enums
    private IssueCategory category;
    private ComplaintStatus status;

    // Constructor used for logging a new complaint
    public Complaint(int trackingID, int zoneNumber, String details, IssueCategory category) {
        this.trackingID = trackingID;
        this.zoneNumber = zoneNumber;
        this.details = details;
        this.category = category;
        
        // Default values upon creation
        this.status = ComplaintStatus.SUBMITTED;
        this.submissionDate = LocalDateTime.now();
    }
    
    // Constructor used when loading an existing complaint from storage
    public Complaint(int trackingID, int zoneNumber, String details, IssueCategory category, ComplaintStatus status, LocalDateTime submissionDate) {
        this.trackingID = trackingID;
        this.zoneNumber = zoneNumber;
        this.details = details;
        this.category = category;
        this.status = status;
        this.submissionDate = submissionDate;
    }

    // --- Getters ---
    public int getTrackingID() { return trackingID; }
    public int getZoneNumber() { return zoneNumber; }
    public String getDetails() { return details; }
    public IssueCategory getCategory() { return category; }
    public ComplaintStatus getStatus() { return status; }
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    
    // Returns the date formatted for display and DB storage
    public String getFormattedDate() {
        return submissionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // --- Setters ---
    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }
    
    // Formatting the complaint for clean console output
    @Override
    public String toString() {
        String shortDetails = details.substring(0, Math.min(30, details.length())) + "...";
        return String.format("| ID: %-5d | Zone: %-4d | Category: %-18s | Status: %-12s | Date: %s | Details: %s",
            trackingID, zoneNumber, category, status, getFormattedDate(), shortDetails);
    }
}