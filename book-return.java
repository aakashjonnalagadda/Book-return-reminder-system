package aakash;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BookReminderSystem extends JFrame {

    JTextField tStudent, tBook, tBorrow, tReturn, tReminder;
    JTextArea displayArea;
    JButton btnAdd, btnCheck;

    static final String URL = "jdbc:mysql://localhost:3306/library_reminder";
    static final String USER = "root";
    static final String PASS = "Saicharan@06";

    public BookReminderSystem() {
        setTitle("Book Return Reminder System");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        add(new JLabel("Student Name:"));
        tStudent = new JTextField(20);
        add(tStudent);

        add(new JLabel("Book Name:"));
        tBook = new JTextField(20);
        add(tBook);

        add(new JLabel("Borrow Date (YYYY-MM-DD):"));
        tBorrow = new JTextField(20);
        add(tBorrow);

        add(new JLabel("Return Date (YYYY-MM-DD):"));
        tReturn = new JTextField(20);
        add(tReturn);

        add(new JLabel("Reminder Days:"));
        tReminder = new JTextField(20);
        add(tReminder);

        btnAdd = new JButton("Add Record");
        btnCheck = new JButton("Check Reminder");

        add(btnAdd);
        add(btnCheck);

        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea));

        btnAdd.addActionListener(e -> addRecord());
        btnCheck.addActionListener(e -> checkReminders());

        setVisible(true);
    }

    private Connection connect() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private void addRecord() {
        try (Connection con = connect()) {
            String student = tStudent.getText();
            String book = tBook.getText();
            String bDate = tBorrow.getText();
            String rDate = tReturn.getText();
            int reminder = Integer.parseInt(tReminder.getText());

            String sql = "INSERT INTO borrow(student_name, book_name, borrow_date, return_date, reminder_days) VALUES (?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, student);
            ps.setString(2, book);
            ps.setDate(3, Date.valueOf(bDate));
            ps.setDate(4, Date.valueOf(rDate));
            ps.setInt(5, reminder);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Record Added Successfully!");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "❌ Date must be YYYY-MM-DD");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
        }
    }

    private void checkReminders() {
        try (Connection con = connect()) {
            String sql = "SELECT student_name, book_name, return_date, reminder_days, " +
                         "DATEDIFF(return_date, CURDATE()) AS days_left FROM borrow";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            displayArea.setText("Reminder Status:\n\n");

            while (rs.next()) {
                String name = rs.getString("student_name");
                String book = rs.getString("book_name");
                int daysLeft = rs.getInt("days_left");
                int reminder = rs.getInt("reminder_days");

                displayArea.append("Student: " + name + "\n");
                displayArea.append("Book: " + book + "\n");
                displayArea.append("Days Left: " + daysLeft + "\n");

                if (daysLeft <= reminder && daysLeft >= 0)
                    displayArea.append("⚠ Reminder: Return soon!\n");
                else if (daysLeft < 0)
                    displayArea.append("❌ Overdue!\n");
                else
                    displayArea.append("✅ No reminder needed\n");

                displayArea.append("--------------------------\n");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new BookReminderSystem();
    }
}