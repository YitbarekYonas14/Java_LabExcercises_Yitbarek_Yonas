# Java Lab Exercises and Assignments - Yitbarek Yonas

## 📚 About This Repository

Welcome to my Java Lab Exercise repository! This collection represents my journey through various Java concepts, from basic file operations to advanced networking and distributed systems. Each project here has taught me something valuable, and I've documented them to show my understanding of core Java principles.

---

## 📁 Lab Exercise 1: University Package

> **Focus Areas:** File I/O, Database Operations, Sockets, Serialization

This lab exercise implements a **University Management System** that demonstrates how Java handles data persistence and network communication.

### What I Learned:
- **File I/O Streams** - Reading/writing student and course data to files
- **Database Connectivity** - Storing and retrieving information from databases
- **Socket & ServerSocket** - Client-server communication within the university network
- **Serialization** - Converting objects to byte streams for storage/transmission

### Key Features:
- Add, view, update, and delete student records
- File-based data storage with backup functionality
- Database integration for persistent storage
- Client-server architecture using sockets

---

## 💻 Lab Exercise 2: UniversityJavaFX

> **Focus Areas:** GUI Development, University Package Integration

Building on Lab Exercise 1, this project adds a **Graphical User Interface** using JavaFX to make the University Management System more user-friendly.

### What I Learned:
- **JavaFX Scene Builder** - Designing intuitive interfaces
- **Event Handling** - Responding to button clicks, form submissions
- **Real-time Updates** - Reflecting database changes instantly in the UI

### Key Features:
- Login screen for administrators and staff
- Dashboard to manage students, courses, and professors
- Visual feedback for database operations
- Form validation and error handling

---

## 🌐 Lab Exercise 3: University-RMI

> **Focus Areas:** Remote Method Invocation, Distributed Systems

This project takes the university system **remote**! The file and database operations are moved to a server and accessed remotely using RMI.

### What I Learned:
- **Remote Interface** - Defining methods that can be called from other JVMs
- **UnicastRemoteObject** - Exporting remote objects
- **Registry** - Registering and looking up remote services
- **Distributed Architecture** - Separating server logic from client applications

### Key Features:
- Remote server hosting all university data operations
- Multiple clients can connect simultaneously
- Transparent remote method calls (they feel like local calls!)
- Centralized data management with distributed access

---

##  Assignments

### PokerGame
> **Understanding Collections Framework**

A fully functional poker game that helped me master Java's Collection Framework.

**Concepts Covered:**
- `ArrayList` and `LinkedList` for card management
- `HashMap` for player statistics
- `HashSet` for unique card tracking

**What I Built:**
- Deck shuffling and dealing mechanics
- Hand ranking system (pair, flush, straight, etc.)
- Multi-player support


---

### ChatApp
> **Understanding Multiple Client Connections & Threads**

A real-time chat application that demonstrates concurrent programming in Java.

**Concepts Covered:**
- **Multithreading** - Each client gets its own thread
- **Concurrent Connections** - Handling multiple clients simultaneously
- **Broadcasting** - Sending messages to all connected clients
- **Synchronization** - Managing shared resources safely

**What I Built:**
- Server that accepts multiple client connections
- Private messaging between specific users
- Public(group) chat rooms


---

### Notepad Application
> **Understanding File I/O Streams**

A simple but complete text editor that made file operations second nature to me.

**Concepts Covered:**
- **FileInputStream/FileOutputStream** - Reading/writing raw bytes
- **BufferedReader/BufferedWriter** - Efficient text operations
- **FileReader/FileWriter** - Character-based file handling

**What I Built:**
- Create, open, edit, and save text files
- Cut, copy, paste, and select all functionality
---
## 🗄️ Database Setup (Required Before Running)

### ⚠️ IMPORTANT: Create Databases and Tables First!

This repository contains **TWO different applications** that use MySQL databases:

| Application | Database Name | Purpose |
|-------------|---------------|---------|
| **ChatApp** | `telegram_clone` | Stores user accounts, messages, groups |
| **University,UniversityJavaFX,UinversityRMI** | `university_db` | Stores student and teacher records |

### Step-by-Step Database Setup:

#### 1. **Start MySQL Server**

| XAMPP (Windows/Linux) | MAMP (macOS) | Command Line (Linux) |
|-----------------------|--------------|---------------------|
| Open XAMPP Control Panel → Start MySQL | Start MAMP → MySQL will start automatically | `sudo systemctl start mysql` |

#### 2. **Create Both Databases and Tables**

Open your MySQL client (phpMyAdmin or terminal) and run:

```sql
-- ============================================
-- CREATE DATABASES
-- ============================================

-- Create ChatApp database
CREATE DATABASE telegram_clone;

-- Create University database
CREATE DATABASE university_db;

-- ============================================
-- USE ChatApp DATABASE AND CREATE TABLES
-- ============================================

USE telegram_clone;

-- Users table (stores user credentials)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Private messages table (stores 1-on-1 conversations)
CREATE TABLE IF NOT EXISTS private_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(50) NOT NULL,
    receiver VARCHAR(50) NOT NULL,
    message TEXT,
    file_path VARCHAR(255),
    is_file BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conv (sender, receiver)
);

-- Group messages table (stores group chat messages)
CREATE TABLE IF NOT EXISTS group_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(50) NOT NULL,
    group_name VARCHAR(50) NOT NULL,
    message TEXT,
    file_path VARCHAR(255),
    is_file BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Group members table (tracks who is in which group)
CREATE TABLE IF NOT EXISTS group_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_gm (group_name, username)
);

-- ============================================
-- USE University DATABASE AND CREATE TABLES
-- ============================================

USE university_db;

-- Students table
CREATE TABLE IF NOT EXISTS students (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    section CHAR(1) NOT NULL,
    year INT NOT NULL
);

-- Teachers table
CREATE TABLE IF NOT EXISTS teachers (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    experience INT NOT NULL
);

-- ============================================
-- VERIFY EVERYTHING WAS CREATED
-- ============================================

-- Check ChatApp tables
USE telegram_clone;
SHOW TABLES;

-- Check University tables
USE university_db;
SHOW TABLES;
## 🚀 How to Clone, Compile, and Run

### Prerequisites

Before you begin, make sure you have:

| Requirement | Version | Check Command |
|-------------|---------|---------------|
| **Java JDK** | 11 or higher | `java -version` |
| **JavaFX SDK** | 25.0.2 or higher | Download from [Gluon](https://gluonhq.com/products/javafx/) |
| **MySQL Connector** | 9.6.0 | Included in `/lib` folder |

