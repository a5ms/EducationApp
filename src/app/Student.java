package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Student extends User {
    private List<Course> courses = new ArrayList<>();
    private Map<Course, Double> grades = new HashMap<>();

    public Student(String name, int age, String username, String password) {
        super(name, age, username, password);
    }

    public void enroll(Course course) {
        if (!courses.contains(course)) {
            courses.add(course);
        }
    }

    public void addGrade(Course course, double grade) {
        // Ensure the student is enrolled in the course before adding a grade
        if (!courses.contains(course)) {
            enroll(course);
        }
        grades.put(course, grade);
    }

    public Map<Course, Double> getGrades() {
        return grades;
    }
  
    @Override
    public String getInfo() {
        return "Student - Name: " + name + ", Age: " + age + ", Courses: " + courses;
    }

    public List<Course> getCourses() {
        return courses;
    }
}
