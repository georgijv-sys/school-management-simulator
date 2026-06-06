import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;

public class ConfigurationLoadingTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void loadSchoolParsesValidConfigurationFile() throws Exception {
        File configuration = writeConfiguration(
                "school:Java Training School",
                "subject:Basics,1,1,5",
                "student:Alice,F,20",
                "Teacher:Dr Smith,F,35"
        );

        School school = Administrator.loadSchool(configuration.getAbsolutePath());

        assertEquals("Java Training School", school.getName());
        assertEquals(1, school.getSubjects().length);
        assertEquals(1, school.getStudents().length);
        assertEquals(1, school.getInstructors().length);
    }

    @Test
    public void loadSchoolRejectsInvalidGender() throws Exception {
        File configuration = writeConfiguration(
                "school:Java Training School",
                "subject:Basics,1,1,5",
                "student:Alice,X,20"
        );

        try {
            Administrator.loadSchool(configuration.getAbsolutePath());
        } catch (InvalidConfigurationException e) {
            return;
        }

        throw new AssertionError("Expected InvalidConfigurationException");
    }

    @Test
    public void loadSchoolRequiresSchoolBeforeSubjects() throws Exception {
        File configuration = writeConfiguration("subject:Basics,1,1,5");

        try {
            Administrator.loadSchool(configuration.getAbsolutePath());
        } catch (InvalidConfigurationException e) {
            return;
        }

        throw new AssertionError("Expected InvalidConfigurationException");
    }

    private File writeConfiguration(String... lines) throws Exception {
        File configuration = temporaryFolder.newFile("school.txt");
        try (PrintWriter writer = new PrintWriter(configuration)) {
            for (String line : lines) {
                writer.println(line);
            }
        }
        return configuration;
    }
}
