import java.util.ArrayList;

public class Course {

    private Subject subject;
    private int daysUntilStarts;
    private int daysToRun;
    private ArrayList<Student> students = new ArrayList<Student>();
    private Instructor instructor;
    private boolean cancelled = false;

    public Course(Subject subject, int daysUntilStarts) {
        this.subject = subject;
        this.daysUntilStarts = daysUntilStarts;
        this.daysToRun = subject.getDuration();
    }

    public Subject getSubject() {
        return subject;
    }

    // returns negative number of days that are left until the course starts
    // if started returns days left till the end
    // returns 0 if ended
    public int getStatus() {
        if (daysUntilStarts > 0) {
            return -daysUntilStarts;
        }
        if (daysToRun > 0) {
            return daysToRun;
        }
        return 0;
    }

    // decided to add this, this is not in the instructions
    // it is used to save the instructor and days-related data in the file when pausing the simulation and using it to resume
    public Instructor getInstructor() {
        return instructor;
    }
    public int getDaysUntilStarts() {
        return daysUntilStarts;
    }
    public int getDaysToRun() {
        return daysToRun;
    }
    // decided to add this
    // it is needed for restoring exact timing and cancel state for resuming the simulation
    // will be using in the administrator class to store the current state of the simulation in a file
    public void loadCourseState(int daysUntilStarts, int daysToRun, boolean cancelled) {
        this.daysUntilStarts = daysUntilStarts;
        this.daysToRun = daysToRun;
        this.cancelled = cancelled;
    }

    public void addStudentFromSave(Student student) {
        students.add(student);
    }

    // adds a student to a course arrayList(all the students in the course) and returns true if successful
    // if the course is full (3 or more) -> false
    // if the course is ended -> false
    public boolean enrolStudent(Student student) {
        if (students.size() >= 3) {
            return false;
        }

        if (daysUntilStarts <= 0) {
            return false;
        }

        students.add(student);
        return true;
    }

    public int getSize() {
        return students.size();
    }

    //here we are converting to an array from the array list
    //since the tasks asks us to do so
    public Student[] getStudents() {
        return students.toArray(new Student[0]);
    }

    public void aDayPasses() {
        aDayPasses(0);
    }

    public ArrayList<SimulationEvent> aDayPasses(int day) {
        ArrayList<SimulationEvent> events = new ArrayList<>();

        if (daysUntilStarts > 0) {
            daysUntilStarts--;
            // if there is no students or no instructors for the course
            // and it has already started then cancell the course
            if (daysUntilStarts == 0 && (students.isEmpty() || instructor == null)) {
                cancelled = true;
                if (instructor != null) {
                    instructor.unassignCourse();
                }
                events.add(new SimulationEvent(day, SimulationEvent.Type.COURSE_CANCELLED,
                        subject.getDescription() + " was cancelled because it did not have enough support"));
            } else if (daysUntilStarts == 0) {
                events.add(new SimulationEvent(day, SimulationEvent.Type.COURSE_STARTED,
                        subject.getDescription() + " started with " + students.size() + " student(s)"));
            }
        } else if (daysToRun > 0) {
            daysToRun--;
            //when course is finished all the students from that course get a certificate
            if (daysToRun == 0) {
                for (Student student : students) {
                    student.graduate(subject);
                }
                // unassign instructor if the course ended
                if (instructor != null) {
                    instructor.unassignCourse();
                }
                events.add(new SimulationEvent(day, SimulationEvent.Type.COURSE_COMPLETED,
                        subject.getDescription() + " finished and awarded " + students.size() + " certificate(s)"));
            }
        }

        return events;
    }

    // sets and instructor for the course, if the specialism of the instructor allows so
    //returns true if specialism allows
    public boolean setInstructor(Instructor instructor) {
        if (instructor.canTeach(subject)) {
            this.instructor = instructor;
            instructor.assignCourse(this);
            return true;
        }
        return false;
    }

    // returns true if the course has an instructor
    public boolean hasInstructor() {
        return instructor != null;
    }

    // returns true if the course is cancelled
    public boolean isCancelled() {
        return cancelled;
    }
}
