import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstructorSpecialismTest {

    @Test
    public void teacherCanTeachCoreAndLabSubjectsOnly() {
        Instructor teacher = new Teacher("Teacher", 'F', 35);

        assertTrue(teacher.canTeach(subjectWithSpecialism(1)));
        assertTrue(teacher.canTeach(subjectWithSpecialism(2)));
        assertFalse(teacher.canTeach(subjectWithSpecialism(3)));
        assertFalse(teacher.canTeach(subjectWithSpecialism(4)));
    }

    @Test
    public void specialistInstructorsSupportTheirSpecialisms() {
        Instructor demonstrator = new Demonstrator("Demonstrator", 'M', 28);
        Instructor ooTrainer = new OOTrainer("OO Trainer", 'F', 31);
        Instructor guiTrainer = new GUITrainer("GUI Trainer", 'M', 33);

        assertTrue(demonstrator.canTeach(subjectWithSpecialism(2)));
        assertFalse(demonstrator.canTeach(subjectWithSpecialism(1)));

        assertTrue(ooTrainer.canTeach(subjectWithSpecialism(3)));
        assertFalse(ooTrainer.canTeach(subjectWithSpecialism(4)));

        assertTrue(guiTrainer.canTeach(subjectWithSpecialism(4)));
        assertFalse(guiTrainer.canTeach(subjectWithSpecialism(3)));
    }

    private Subject subjectWithSpecialism(int specialism) {
        return new Subject(specialism, specialism, 3, "Subject " + specialism);
    }
}
