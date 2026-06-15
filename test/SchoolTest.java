import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SchoolTest {

    @Test
    public void dayAtSchoolCreatesCourseAssignsInstructorAndEnrolsStudents() {
        School school = new School("Java Training School");
        Subject basics = new Subject(1, 1, 3, "Basics");

        school.add(basics);
        school.add(new Teacher("Teacher", 'F', 35));
        school.add(new Student("Alice", 'F', 20));
        school.add(new Student("Ben", 'M', 21));
        school.add(new Student("Casey", 'F', 22));
        school.add(new Student("Drew", 'M', 23));

        ArrayList<SimulationEvent> events = school.aDayAtSchool(1);
        Course[] courses = school.getCourses();

        assertEquals(1, courses.length);
        assertEquals(basics, courses[0].getSubject());
        assertTrue(courses[0].hasInstructor());
        assertEquals(4, courses[0].getSize());
        assertEquals(0, school.getIdleStudentCount());
        assertTrue(containsEvent(events, SimulationEvent.Type.COURSE_CREATED));
        assertTrue(containsEvent(events, SimulationEvent.Type.INSTRUCTOR_ASSIGNED));
        assertTrue(containsEvent(events, SimulationEvent.Type.STUDENT_ENROLLED));
    }

    @Test
    public void openCourseCanBeFoundForSubject() {
        School school = new School("Java Training School");
        Subject lab = new Subject(2, 2, 2, "Lab");
        school.add(lab);
        school.add(new Demonstrator("Demonstrator", 'M', 28));
        school.add(new Student("Alice", 'F', 20));

        school.aDayAtSchool(1);

        assertNotNull(school.getOpenCourseForSubject(lab));
    }

    private boolean containsEvent(ArrayList<SimulationEvent> events, SimulationEvent.Type type) {
        for (SimulationEvent event : events) {
            if (event.getType() == type) {
                return true;
            }
        }
        return false;
    }
}
