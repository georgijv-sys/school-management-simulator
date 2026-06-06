import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdministratorPersistenceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void savedSimulationRestoresDayCoursesInstructorsStudentsAndCertificates() throws Exception {
        School school = new School("Java Training School");
        Subject basics = new Subject(1, 1, 2, "Basics");
        Student alice = new Student("Alice", 'F', 20);
        Instructor teacher = new Teacher("Teacher", 'F', 35);

        school.add(basics);
        school.add(alice);
        school.add(teacher);
        school.aDayAtSchool(5);
        alice.graduate(basics);

        Administrator administrator = new Administrator(school, 5);
        File saveFile = temporaryFolder.newFile("school.save.txt");

        administrator.saveSimulationToFile(saveFile.getAbsolutePath());
        Administrator loaded = Administrator.loadAdministratorFromSimulation(saveFile.getAbsolutePath());
        School loadedSchool = loaded.getSchool();
        Course loadedCourse = loadedSchool.getCourses()[0];

        assertEquals(5, loaded.getCurrentDay());
        assertEquals("Java Training School", loadedSchool.getName());
        assertEquals(1, loadedSchool.getSubjects().length);
        assertEquals(1, loadedSchool.getStudents().length);
        assertEquals(1, loadedSchool.getInstructors().length);
        assertEquals(1, loadedSchool.getCourses().length);
        assertNotNull(loadedCourse.getInstructor());
        assertEquals("Teacher", loadedCourse.getInstructor().getName());
        assertEquals("Alice", loadedCourse.getStudents()[0].getName());
        assertTrue(loadedSchool.getStudents()[0].hasCertificate(basics));
    }
}
