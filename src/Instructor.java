public abstract class Instructor extends Person {

    private Course assignedCourse;

    public Instructor(String name, char gender, int age) {
        super(name, gender, age);
        assignedCourse = null;
    }

    public void assignCourse(Course course) {
        assignedCourse = course;
    }

    public void unassignCourse() {
        assignedCourse = null;
    }

    //returns null instructor does not have a course assigned
    public Course getAssignedCourse() {
        return assignedCourse;
    }

    public abstract boolean canTeach(Subject subject);
}