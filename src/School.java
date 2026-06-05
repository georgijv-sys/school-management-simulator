import java.util.ArrayList;

public class School {

    private String name;
    private ArrayList<Student> students;
    private ArrayList<Instructor> instructors;
    private ArrayList<Subject> subjects;
    private ArrayList<Course> courses;

    public School(String name){
        this.name = name;
        students = new ArrayList<>();
        instructors = new ArrayList<>();
        subjects = new ArrayList<>();
        courses = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    // adding students, instructors, subjects, courses to the school
    // adding them to the corresponding arrays
    public void add(Student student){
        students.add(student);
    }
    public void add(Instructor instructor){
        instructors.add(instructor);
    }
    public void add(Subject subject){
        subjects.add(subject);
    }
    public void add(Course course){
        courses.add(course);
    }

    // removing students, instructors, subjects, courses from the school
    // removing them to from the corresponding arrays
    public void remove(Student student){
        students.remove(student);
    }
    public void remove(Instructor instructor){
        instructors.remove(instructor);
    }
    public void remove(Subject subject){
        subjects.remove(subject);
    }
    public void remove(Course course){
        courses.remove(course);
    }

    // returns an array of students, instructors, subjects and courses
    public Student[] getStudents(){
        return students.toArray(new Student[0]);
    }
    public Instructor[] getInstructors(){
        return instructors.toArray(new Instructor[0]);
    }
    public Subject[] getSubjects(){
        return subjects.toArray(new Subject[0]);
    }
    public Course[] getCourses(){
        return courses.toArray(new Course[0]);
    }

    public void aDayAtSchool(){
        aDayAtSchool(0);
    }

    public ArrayList<SimulationEvent> aDayAtSchool(int day){
        ArrayList<SimulationEvent> events = new ArrayList<>();

        // assigns a course to a subject that does not have an open course, is not cancelled and is not finished
        for(Subject subject: subjects){
            boolean hasOpenCourse = false;

            for(Course course: courses){
                if (course.getSubject().equals(subject) && !course.isCancelled() && course.getStatus() != 0){
                    hasOpenCourse = true;
                    break;
                }
            }
            if(!hasOpenCourse){
                Course course = new Course(subject, 2);
                courses.add(course);
                events.add(new SimulationEvent(day, SimulationEvent.Type.COURSE_CREATED,
                        "Created a new " + subject.getDescription() + " course"));
            }
        }

        // assigns an instructor to a course if course does not have an instructor
        // and if instructor does not have any courses assigned
        // and if instructor has required specialism
        for(Course course: courses){
            if(!course.hasInstructor()){
                for(Instructor instructor: instructors){
                    if (instructor.getAssignedCourse() == null){
                        if (course.setInstructor(instructor)){
                            events.add(new SimulationEvent(day, SimulationEvent.Type.INSTRUCTOR_ASSIGNED,
                                    instructor.getName() + " was assigned to " + course.getSubject().getDescription()));
                            break;
                        }
                    }
                }
            }
        }

        for(Student student: students){
            boolean enrolled = false;

            // for each course we check the array of students and if it is the same as the student
            // that we are checking for - we break
            // meaning that that student is already enrolled in one of the courses
            // (student can be only enrolled in one course at a time)
            for(Course course: courses){
                for(Student enrolledStudents : course.getStudents()){
                    if (enrolledStudents.equals(student)){
                        enrolled = true;
                        break;
                    }
                }
                if(enrolled){
                    break;
                }
            }

            // enrolls a student on a course if a student is not enrolled
            // and the course is not full, has not started
            // and if the student does not have a certificate form that course
            if(!enrolled){
                for (Course course: courses){
                    if(course.getSize() < 3 && course.getStatus() < 0 && !student.hasCertificate(course.getSubject())){
                        if (course.enrolStudent(student)){
                            events.add(new SimulationEvent(day, SimulationEvent.Type.STUDENT_ENROLLED,
                                    student.getName() + " enrolled in " + course.getSubject().getDescription()));
                            break;
                        }
                    }
                }
            }
        }

        // one day passes for a course
        for(Course course: courses){
            events.addAll(course.aDayPasses(day));
        }

        // removes finished or cancelled courses
        ArrayList<Course> coursesToRemove = new ArrayList<>();
        for(Course course: courses){
            if (course.getStatus() == 0 || course.isCancelled()){
                coursesToRemove.add(course);
            }
        }
        courses.removeAll(coursesToRemove);

        return events;
    }

    public Course getCourseForStudent(Student student) {
        for (Course course : courses) {
            for (Student enrolled : course.getStudents()) {
                if (enrolled == student) {
                    return course;
                }
            }
        }
        return null;
    }

    public Course getOpenCourseForSubject(Subject subject) {
        for (Course course : courses) {
            if (course.getSubject() == subject && !course.isCancelled() && course.getStatus() != 0) {
                return course;
            }
        }
        return null;
    }

    public int getTotalCertificatesAwarded() {
        int total = 0;
        for (Student student : students) {
            total += student.getCertificates().size();
        }
        return total;
    }

    public int getAvailableInstructorCount() {
        int total = 0;
        for (Instructor instructor : instructors) {
            if (instructor.getAssignedCourse() == null) {
                total++;
            }
        }
        return total;
    }

    public int getIdleStudentCount() {
        int total = 0;
        for (Student student : students) {
            if (getCourseForStudent(student) == null) {
                total++;
            }
        }
        return total;
    }

    // returns a string of everything and everyone in the school
    public String toString() {

        String result = "";

        // COURSES
        result += "Courses:\n";
        for (Course course : courses) {

            result += course.getSubject().getDescription() + " " + course.getStatus();

            Student[] students = course.getStudents();
            if (students.length > 0) {
                result += " ";

                for (int i = 0; i < students.length; i++) {
                    result += students[i].getName();

                    if (i < students.length - 1) {
                        result += ", ";
                    }
                }
            }

            result += "\n";
        }

        // STUDENTS
        result += "Students:\n";
        for (Student student : students) {

            result += student.getName() + " " + student.getCertificates();

            for (Course course : courses) {
                for (Student enrolled : course.getStudents()) {

                    if (enrolled == student) {
                        result += " " + course.getSubject().getDescription();
                        break;
                    }
                }
            }

            result += "\n";
        }

        // INSTRUCTORS
        result += "Instructors:\n";
        for (Instructor instructor : instructors) {

            result += instructor.getName();

            if (instructor.getAssignedCourse() != null) {
                result += " -> " +
                        instructor.getAssignedCourse().getSubject().getDescription();
            }

            result += "\n";
        }

        return result;
    }

}
