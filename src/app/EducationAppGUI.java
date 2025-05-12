package app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EducationAppGUI extends JFrame {
    private List<Student> students = new ArrayList<>();
    private List<Teacher> teachers = new ArrayList<>();
    private User currentUser;

    public EducationAppGUI() {
        initLoginScreen();
    }

    private void initLoginScreen() {
        setTitle("Education App Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JComboBox<String> userTypeCombo = new JComboBox<>(new String[]{"Student", "Teacher"});

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        loginPanel.add(new JLabel("User Type:"), gbc);
        gbc.gridy = 3;
        loginPanel.add(userTypeCombo, gbc);

        JButton loginButton = new JButton("Login");
        gbc.gridy = 4;
        loginPanel.add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String userType = (String) userTypeCombo.getSelectedItem();

            if (authenticateUser(username, password, userType)) {
                openUserSpecificGUI();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials");
            }
        });

        add(loginPanel, BorderLayout.CENTER);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private boolean authenticateUser(String username, String password, String userType) {
        if (userType.equals("Student")) {
            for (Student student : students) {
                if (student.getUsername().equals(username) && student.checkPassword(password)) {
                    currentUser = student;
                    return true;
                }
            }
        } else {
            for (Teacher teacher : teachers) {
                if (teacher.getUsername().equals(username) && teacher.checkPassword(password)) {
                    currentUser = teacher;
                    return true;
                }
            }
        }
        return false;
    }

    private void openUserSpecificGUI() {
        dispose(); // Close login screen
        if (currentUser instanceof Student) {
            openStudentGUI((Student) currentUser);
        } else if (currentUser instanceof Teacher) {
            openTeacherGUI((Teacher) currentUser);
        }
    }

    private void openStudentGUI(Student student) {
        JFrame studentFrame = new JFrame("Student Dashboard - " + student.name);
        studentFrame.setSize(600, 400);
        studentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea performanceArea = new JTextArea();
        performanceArea.setEditable(false);

        // Calculate and display academic performance
        Map<Course, Double> grades = student.getGrades();
        if (!grades.isEmpty()) {
            performanceArea.append("Academic Performance:\n");
            double totalGrade = 0;
            for (Map.Entry<Course, Double> entry : grades.entrySet()) {
                performanceArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
                totalGrade += entry.getValue();
            }
            double gpa = totalGrade / grades.size();
            performanceArea.append("\nOverall GPA: " + String.format("%.2f", gpa));
        } else {
            performanceArea.append("No grades recorded yet.");
        }

        JScrollPane scrollPane = new JScrollPane(performanceArea);
        studentFrame.add(scrollPane);
        studentFrame.setLocationRelativeTo(null);
        studentFrame.setVisible(true);
    }

    private void openTeacherGUI(Teacher teacher) {
        JFrame teacherFrame = new JFrame("Teacher Dashboard - " + teacher.name);
        teacherFrame.setSize(800, 600);
        teacherFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        teacherFrame.setLayout(new BorderLayout());

        // Panel for course and student selection
        JPanel selectionPanel = new JPanel(new FlowLayout());
        
        // Course selection
        JComboBox<Course> courseCombo = new JComboBox<>();
        for (Course course : teacher.getCourses()) {
            courseCombo.addItem(course);
        }

        selectionPanel.add(new JLabel("Select Course: "));
        selectionPanel.add(courseCombo);

        // Table for student grades
        String[] columnNames = {"Student Name", "Course", "Grade"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable gradesTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(gradesTable);

        // Update table when course is selected
        courseCombo.addActionListener(e -> {
            // Clear existing rows
            tableModel.setRowCount(0);
            
            // Get selected course
            Course selectedCourse = (Course) courseCombo.getSelectedItem();
            
            // Find students in this course
            List<Student> courseStudents = students.stream()
                .filter(s -> s.getCourses().contains(selectedCourse))
                .collect(Collectors.toList());
            
            // Populate table with students and their current grades
            for (Student student : courseStudents) {
                Double currentGrade = student.getGrades().get(selectedCourse);
                tableModel.addRow(new Object[]{
                    student.name, 
                    selectedCourse, 
                    currentGrade != null ? currentGrade : "Not Graded"
                });
            }
        });

        // Grade entry button
        JButton enterGradeButton = new JButton("Enter/Update Grade");
        enterGradeButton.addActionListener(e -> {
            // Get selected row
            int selectedRow = gradesTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(teacherFrame, 
                    "Please select a student first.", 
                    "No Student Selected", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get student name and course
            String studentName = (String) tableModel.getValueAt(selectedRow, 0);
            Course selectedCourse = (Course) courseCombo.getSelectedItem();

            // Find the student
            Student selectedStudent = students.stream()
                .filter(s -> s.name.equals(studentName))
                .findFirst()
                .orElse(null);

            if (selectedStudent == null) return;

            // Prompt for grade
            String gradeInput = JOptionPane.showInputDialog(
                teacherFrame, 
                "Enter grade for " + studentName + " in " + selectedCourse + ":",
                "Enter Grade",
                JOptionPane.PLAIN_MESSAGE
            );

            if (gradeInput != null && !gradeInput.trim().isEmpty()) {
                try {
                    double grade = Double.parseDouble(gradeInput);
                    
                    // Update student's grade
                    selectedStudent.addGrade(selectedCourse, grade);
                    
                    // Refresh the table
                    tableModel.setValueAt(grade, selectedRow, 2);
                    
                    JOptionPane.showMessageDialog(teacherFrame, 
                        "Grade updated successfully!", 
                        "Grade Entry", 
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(teacherFrame, 
                        "Invalid grade format. Please enter a number.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add components to frame
        teacherFrame.add(selectionPanel, BorderLayout.NORTH);
        teacherFrame.add(tableScrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(enterGradeButton);
        teacherFrame.add(buttonPanel, BorderLayout.SOUTH);

        teacherFrame.setLocationRelativeTo(null);
        teacherFrame.setVisible(true);

        // Trigger initial population of the table
        if (courseCombo.getItemCount() > 0) {
            courseCombo.setSelectedIndex(0);
        }
    }

    // Method to add users with authentication
    public void addStudent(String name, int age, String username, String password) {
        Student student = new Student(name, age, username, password);
        students.add(student);
    }


    public void addTeacher(String name, int age, String subject, String username, String password) {
        Teacher teacher =new Teacher(name, age, subject, username, password);
        teachers.add(teacher);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EducationAppGUI app = new EducationAppGUI();
            
            // Pre-populate some users for demonstration
            // Create students
            app.addStudent("John Doe", 20, "student1", "password");
            app.addStudent("Jane Smith", 22, "student2", "password");
            app.addStudent("Mike Johnson", 21, "student3", "password");
            
            // Create courses
            Course mathCourse = new Course("Math");
            Course scienceCourse = new Course("Science");
            Course physicsCourse = new Course("Physics");

            // Set up first student
            Student student1 = app.students.get(0);
            student1.enroll(mathCourse);
            student1.enroll(scienceCourse);
            student1.addGrade(mathCourse, 85.5);
            student1.addGrade(scienceCourse, 78.0);

            // Set up second student
            Student student2 = app.students.get(1);
            student2.enroll(scienceCourse);
            student2.enroll(physicsCourse);
            student2.addGrade(scienceCourse, 92.0);
            student2.addGrade(physicsCourse, 88.5);

            // Set up third student
            Student student3 = app.students.get(2);
            student3.enroll(mathCourse);
            student3.enroll(physicsCourse);
            student3.addGrade(mathCourse, 90.0);
            student3.addGrade(physicsCourse, 85.0);

            // Create teachers
            app.addTeacher("Mr. Brown", 35, "Mathematics", "teacher1", "password");
            app.addTeacher("Ms. Garcia", 40, "Science", "teacher2", "password");

            // Set up teachers' courses
            Teacher teacher1 = app.teachers.get(0);
            teacher1.addCourse(mathCourse);
            teacher1.addCourse(physicsCourse);

            Teacher teacher2 = app.teachers.get(1);
            teacher2.addCourse(scienceCourse);
            teacher2.addCourse(physicsCourse);
        });
    }
}
