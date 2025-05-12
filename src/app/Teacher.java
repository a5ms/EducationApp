package app;

import java.util.ArrayList;
import java.util.List;

public class Teacher extends User {
    private String subject;
    private List<Course> courses = new ArrayList<>();

    public Teacher(String name, int age, String subject, String username, String password) {
        super(name, age, username, password);
        this.subject = subject;
    }

    public void addCourse(Course course) {
        courses.add(course);
    }

    public List<Course> getCourses() {
        return courses;
    }

    public String getSubject() {
        return subject;
    }

    @Override
    public String getInfo() {
        return "Teacher - Name: " + name + ", Age: " + age + ", Subject: " + subject;
    }
}
