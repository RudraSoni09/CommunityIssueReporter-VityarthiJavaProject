# Community Complaint Logger & Pattern Analyzer (CIRPAnalyzer)

## Project Summary

This project is a comprehensive **Java Command-Line Interface (CLI) application** developed to solve the practical problem of tracking and analyzing municipal issues reported by the community. The application is built using a strong **Object-Oriented Programming (OOP)** design and demonstrates mastery of crucial Java persistence technologies, including **JDBC** and **File I/O**.

The system's core function is to provide immediate operational insight by running a **Pattern Analyzer** that calculates issue frequency across various categories, highlighting service hotspots.

---

## 🏗️ Technical Architecture & Key Achievements

The project follows a clear, layered architecture (Separation of Concerns):

### 1. Business Logic and Persistence (ComplaintManager)
This component is the heart of the system, responsible for all data management:
* **Primary Storage (JDBC):** Data is synchronized and persisted to an H2 embedded database (named `community_h2_db`), ensuring transactional integrity and data recovery across sessions.
* **Backup Storage (File I/O):** Implements **Java Serialization** to save the entire in-memory list to a binary backup file (`COMPLAINT_DATA_BACKUP.bin`).
* **Pattern Analyzer:** Uses a **`HashMap`** and loop-based logic (`getTrendAnalysis`) to quickly tally active complaints by category.

### 2. Data Model (`Complaint.java` & Enums)
* The `Complaint` class is the central, **`Serializable`** data structure.
* **Enums:** `IssueCategory` and `ComplaintStatus` are used rigorously to standardize inputs and manage the complaint lifecycle (SUBMITTED, IN\_REVIEW, CLOSED).

### 3. User Interface (`CIRPAnalyzer.java`)
* Manages the interactive CLI menu and handles all user input/output.
* Features robust **Exception Handling** (e.g., catching `InputMismatchException`) to ensure the application remains stable despite bad user inputs.

---

## 🚀 Compilation and Execution Instructions

The application must be run from the project root directory (`CIRPAnalyzerProject/`).

1.  **Prerequisites:** Ensure the **`h2-*.jar`** file is placed inside the **`lib/`** directory.

2.  **Step 1: Compile All Source Files**
    Compile all `.java` files in the `src` package, including the required library:
    ```bash
    javac -cp "lib/*" src/*.java
    ```

3.  **Step 2: Run the Main Program (The Final Command)**
    Execute the main class using the **Fully Qualified Class Name (`src.CIRPAnalyzer`)** and setting the correct classpath root (`.`) for packaged code:
    ```bash
    java -cp ".:lib/*" src.CIRPAnalyzer
    ```

---

## 📂 Repository Structure