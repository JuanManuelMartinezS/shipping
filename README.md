# 📦 Shipping Management System

A full-stack shipping management system with a **Java backend (REST API with Maven)** built upon **all fundamental Object-Oriented Programming (OOP) principles**, including **inheritance, polymorphism, encapsulation and abstraction**, utilizing **abstract classes and methods** for a robust and extensible architecture. The system also features a **modern frontend using HTML, CSS, and JavaScript**. It enables management of clients, packages, envelopes, sacks, and merchandise with a responsive and modular interface.
---

## 🚀 Features

-   Full management of:
    -   Clients
    -   Boxes, envelopes, packs and sacks
    -   Merchandise and shipping types
-   Shipment tracking and status updates
-   Responsive user interface with **Bootstrap**
-   Interactive tables using **Tabulator**
-   Modular and maintainable codebase

---

## 📁 Project Structure

```text
.
├── enviamos/              # Frontend (HTML, CSS, JS, assets)
│   ├── index.html
│   ├── resources/
│   │   ├── css/
│   │   ├── js/
│   │   └── assets/
│   └── utils/
├── envios/                # Backend (Java, Maven, REST API)
│   ├── src/
│   ├── data/
│   └── pom.xml
├── data/                  # Shared JSON data
└── README.md 
```
---

## ⚙️ Backend (Java, Maven)

### Requirements

-   Java 17+
-   Maven 3.6+

### Setup & Run

1.  Clone the repository.
2.  Navigate to the `envios/` folder.
3. Navigate into the `envios/app` folder and click the 'Run' button in your VS Code editor.
    ```
    For detailed setup, check the full guide: [Backend Setup Instructions](https://docs.google.com/document/d/18z5B4Oka-OxHYr5kuWLxxph708_b8rY61QBIPg5KWj8/edit?tab=t.0#h.d246n3m03a11)

### API

The backend exposes RESTful endpoints for all entities.

Use the included `Peticiones.http` file or tools like Postman to test the API.

---

## 🌐 Frontend (HTML/CSS/JS)

### Requirements

-   A modern web browser
-   (Recommended) Live Server extension for VS Code

### Run

1.  Open `enviamos/index.html` directly in your browser.
    or
2.  Right-click `index.html` in VS Code → “Open with Live Server”.

### Features

-   Navigate between modules (Clients, Shipments, etc.)
-   Responsive UI built with Bootstrap
-   Interactive tables and forms with Tabulator

---

## 🗂️ Data

Sample data is available in the `data/` folder as JSON files.

Images and icons are located in `resources/assets/`.

---

## 🛠️ Technologies

-   **Backend:** Java, Maven, Spring Boot
-   **Frontend:** HTML5, CSS3, JavaScript (ES6+), Bootstrap, Tabulator
-   **Libraries:** Luxon (date/time management), custom utility functions

---

## 🤝 Contributing

1.  Fork the repository.
2.  Create a new branch:
    ```bash
    git checkout -b feature/your-feature
    ```
3.  Make your changes and commit them.
4.  Push to your fork and open a Pull Request.
