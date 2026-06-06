import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CourseTest {

    @Test
    public void courseAcceptsMaximumOfThreeStudentsBeforeItStarts() {
        Course course = new Course(new Subject(1, 1, 2, "Basics"), 2);

        assertTrue(course.enrolStudent(new Student("Alice", 'F', 20)));
        assertTrue(course.enrolStudent(new Student("Ben", 'M', 21)));
        assertTrue(course.enrolStudent(new Student("Casey", 'F', 22)));
        assertFalse(course.enrolStudent(new Student("Drew", 'M', 23)));
        assertEquals(3, course.getSize());
    }

    @Test
    public void courseCannotAcceptStudentsAfterItStarts() {
        Course course = new Course(new Subject(1, 1, 2, "Basics"), 1);

        course.aDayPasses(1);

        assertFalse(course.enrolStudent(new Student("Alice", 'F', 20)));
    }

    @Test
    public void courseCancelsWhenItStartsWithoutRequiredSupport() {
        Course course = new Course(new Subject(1, 1, 2, "Basics"), 1);

        ArrayList<SimulationEvent> events = course.aDayPasses(1);

        assertTrue(course.isCancelled());
        assertEquals(SimulationEvent.Type.COURSE_CANCELLED, events.get(0).getType());
    }

    @Test
    public void courseCompletionAwardsCertificatesAndReleasesInstructor() {
        Subject subject = new Subject(1, 1, 2, "Basics");
        Student student = new Student("Alice", 'F', 20);
        Instructor instructor = new Teacher("Teacher", 'F', 35);
        Course course = new Course(subject, 1);

        assertTrue(course.enrolStudent(student));
        assertTrue(course.setInstructor(instructor));

        course.aDayPasses(1);
        course.aDayPasses(2);
        ArrayList<SimulationEvent> events = course.aDayPasses(3);

        assertEquals(0, course.getStatus());
        assertTrue(student.hasCertificate(subject));
        assertNull(instructor.getAssignedCourse());
        assertEquals(SimulationEvent.Type.COURSE_COMPLETED, events.get(0).getType());
    }
}
