# Bus Booking System

## 📌 Project Overview
The **Bus Booking System** is a web application developed using **Spring Boot, MySQL, and Thymeleaf** that allows users to book bus tickets online. The system provides functionalities for **user authentication, seat selection, booking management, and payment handling**.

## 🚀 Features
- **User Authentication** (Signup & Login)
- **Bus Ticket Booking** with seat selection
- **Booking Management** (view, update, and delete bookings)
- **QR Code-based Confirmation**
- **MySQL Database Integration**
- **Spring Boot MVC Architecture**

## 🛠️ Tech Stack
- **Backend:** Spring Boot, Java, MySQL
- **Frontend:** Thymeleaf, HTML, CSS, JavaScript
- **Database:** MySQL
- **Dependencies:** Spring Boot Starter Web, Spring Boot Starter Thymeleaf, MySQL Connector

## 🔧 Installation & Setup
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/Luvish-Arora/BusBookingSystem.git
   cd BusBookingSystem
   ```
2. **Configure MySQL Database:**
   - Update `application.properties` with your MySQL credentials:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/bus_booking
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   ```
3. **Build & Run the Application:**
   ```bash
   mvn spring-boot:run
   ```
4. **Access the Application:**
   - Open [http://localhost:8080](http://localhost:8080) in your browser.

## 📂 Project Structure
```
BusBookingSystem/
│── src/main/java/com/example/busticketbooking/
│   ├── controllers/        # Controller classes (Booking, User Authentication)
│   ├── models/             # Entity classes (Booking, User)
│   ├── services/           # Business logic (UserService, BookingService)
│   ├── BusTicketBookingApplication.java  # Main Spring Boot Application
│── src/main/resources/
│   ├── static/css/         # Stylesheets
│   ├── templates/          # Thymeleaf HTML templates
│   ├── application.properties  # Spring Boot configuration
│── pom.xml                 # Maven dependencies
│── README.md               # Project documentation
```

## 📷 Screenshots

![image](https://github.com/user-attachments/assets/5856d92b-64ec-498b-856f-6caddced0307)

![image](https://github.com/user-attachments/assets/3f71f6f1-cd92-471d-929f-85ced9f4e84b)

![image](https://github.com/user-attachments/assets/500d8cae-1094-4d92-8004-5fa8849d7a11)

![image](https://github.com/user-attachments/assets/6738d1dc-4d27-43d1-9b6e-140f93cb20ad)

![image](https://github.com/user-attachments/assets/63eeb5d4-5227-4a2b-a4bd-59b5bce9c073)

![image](https://github.com/user-attachments/assets/1b98a7d0-de8a-4ee3-8049-9b20257caeb1)

![image](https://github.com/user-attachments/assets/fab46383-0014-4576-a47f-5ec44beb5a4f)

![image](https://github.com/user-attachments/assets/861b9e4b-b3b6-4b9e-9f50-e3b725602ade)





---

_Enjoy booking your bus tickets hassle-free! 🚌💨_

7dq99fMtHppsDNr9pcYm

busbookingsystem.cme6kcj6afda.ap-south-1.rds.amazonaws.com

admin