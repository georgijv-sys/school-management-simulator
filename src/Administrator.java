import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Administrator {
    private School school;
    private Random random = new Random();
    private int currentDay = 0;

    public Administrator(School school) {
        this.school = school;
    }

    public Administrator(School school, int currentDay) {
        this.school = school;
        this.currentDay = currentDay;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        this.school = school;
        currentDay = 0;
    }

    public void setSchool(School school, int currentDay) {
        this.school = school;
        this.currentDay = currentDay;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void run(){
        while(true){
            run(1);
        }
    }
    public void run(int days){

        // simulates the requested amount of days
        for(int d = 0; d < days; d++){
            advanceOneDay();

            // reports one day in the school
            System.out.println(school.toString());
        }
    }

    public ArrayList<SimulationEvent> advanceOneDay() {
        currentDay++;
        ArrayList<SimulationEvent> events = new ArrayList<>();

        int newStudents = 1 + random.nextInt(3);

        // adds a random number (1-3) of students to the school
        for (int i = 0; i < newStudents; i++) {
            Student student = new Student("Student" + random.nextInt(10000),
                    random.nextBoolean() ? 'M' : 'F',
                    18 + random.nextInt(40));
            school.add(student);
            events.add(new SimulationEvent(currentDay, SimulationEvent.Type.STUDENT_JOINED,
                    student.getName() + " joined the school"));
        }

        // adds a new teacher, demonstrator, OOTrainer and GUITrainer with the probabilities listed in the if statements
        if (random.nextInt(100) < 20) {
            Instructor instructor = new Teacher("Teacher" + random.nextInt(10000),
                    random.nextBoolean() ? 'M' : 'F',
                    25 + random.nextInt(40));
            school.add(instructor);
            events.add(new SimulationEvent(currentDay, SimulationEvent.Type.INSTRUCTOR_JOINED,
                    instructor.getName() + " joined as a Teacher"));
        }

        if (random.nextInt(100) < 10) {
            Instructor instructor = new Demonstrator("Demonstrator" + random.nextInt(10000),
                    random.nextBoolean() ? 'M' : 'F',
                    25 + random.nextInt(40));
            school.add(instructor);
            events.add(new SimulationEvent(currentDay, SimulationEvent.Type.INSTRUCTOR_JOINED,
                    instructor.getName() + " joined as a Demonstrator"));
        }

        if (random.nextInt(100) < 5) {
            Instructor instructor = new OOTrainer("OOTrainer" + random.nextInt(10000),
                    random.nextBoolean() ? 'M' : 'F',
                    25 + random.nextInt(40));
            school.add(instructor);
            events.add(new SimulationEvent(currentDay, SimulationEvent.Type.INSTRUCTOR_JOINED,
                    instructor.getName() + " joined as an OOTrainer"));
        }

        if (random.nextInt(100) < 5) {
            Instructor instructor = new GUITrainer("GUITrainer" + random.nextInt(10000),
                    random.nextBoolean() ? 'M' : 'F',
                    25 + random.nextInt(40));
            school.add(instructor);
            events.add(new SimulationEvent(currentDay, SimulationEvent.Type.INSTRUCTOR_JOINED,
                    instructor.getName() + " joined as a GUITrainer"));
        }

        // one day passes
        events.addAll(school.aDayAtSchool(currentDay));

        // creates an array list of all the instructors that do not have a course with a probability of 20% for removal
        ArrayList<Instructor> instructorsToRemove = new ArrayList<>();
        for (Instructor instructor: school.getInstructors()){
            if (instructor.getAssignedCourse() == null){
                if (random.nextInt(100) < 20) {
                    instructorsToRemove.add(instructor);
                }
            }
        }
        // removes all the instructors from the last for loop from the school
        for (Instructor instructor: instructorsToRemove){
            school.remove(instructor);
            events.add(new SimulationEvent(currentDay, SimulationEvent.Type.INSTRUCTOR_LEFT,
                    instructor.getName() + " left because they had no assigned course"));
        }

        // creating an array list of students to remove if they got all the certificates in all subjects
        ArrayList<Student> studentsToRemove = new ArrayList<>();
        for (Student student: school.getStudents()){

            boolean hasAllCertificates = true;

            for (Subject subject: school.getSubjects()){
                if (!student.hasCertificate(subject)){
                    hasAllCertificates = false;
                    break;
                }
            }
            if (hasAllCertificates && school.getSubjects().length > 0){
                studentsToRemove.add(student);
                events.add(new SimulationEvent(currentDay, SimulationEvent.Type.STUDENT_LEFT,
                        student.getName() + " graduated from every subject"));
            }
            // adding to an array list of students to remove if a student does not have any courses with a probability of 5%
            else {
                boolean enrolled = false;
                for (Course course: school.getCourses()){
                    for (Student s :course.getStudents()){
                        if (s == student){
                            enrolled = true;
                            break;
                        }
                    }
                    if (enrolled) break;
                }

                if  (!enrolled && random.nextInt(100) < 5) {
                    studentsToRemove.add(student);
                    events.add(new SimulationEvent(currentDay, SimulationEvent.Type.STUDENT_LEFT,
                            student.getName() + " left while waiting for a course"));
                }
            }
        }
        for (Student student: studentsToRemove){
            school.remove(student);
        }

        return events;
    }

    public ArrayList<SimulationEvent> advanceDays(int days) {
        ArrayList<SimulationEvent> events = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            events.addAll(advanceOneDay());
        }
        return events;
    }

    public static School createDefaultSchool() {
        School school = new School("Java Training School");

        school.add(new Subject(1, 1, 5, "Basics"));
        school.add(new Subject(2, 2, 2, "Lab 1"));
        school.add(new Subject(3, 1, 4, "Arrays"));
        school.add(new Subject(4, 3, 5, "Object-Oriented Design"));
        school.add(new Subject(5, 4, 4, "Interface Design"));

        return school;
    }

    // loads all the data from the provided file that ends with .txt
    public static School loadSchool(String fileName) throws IOException, InvalidConfigurationException {
        School school = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                if (line.startsWith("school:")) {
                    String name = line.substring(7);

                    if (name.isEmpty()) {
                        throw new InvalidConfigurationException("Malformed school line: " + line);
                    }

                    school = new School(name);
                }

                else if (line.startsWith("subject:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before subjects");
                    }

                    String data = line.substring(8);
                    String[] parts = data.split(",");

                    if (parts.length != 4) {
                        throw new InvalidConfigurationException("Malformed subject line: " + line);
                    }

                    try {
                        Subject subject = new Subject(
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]),
                                Integer.parseInt(parts[3]),
                                parts[0]
                        );
                        school.add(subject);
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric subject value in line: " + line);
                    }
                }

                else if (line.startsWith("student:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before students");
                    }

                    String data = line.substring(8);
                    String[] parts = data.split(",");

                    if (parts.length != 3) {
                        throw new InvalidConfigurationException("Malformed student line: " + line);
                    }
                    // CHECKING THE GENDER, IF EMPTY OR IF WRONG
                    if (parts[1].isEmpty()){
                        throw new InvalidConfigurationException("Missing gender in line: " + line);
                    }
                    char gender = parts[1].charAt(0);
                    if (gender != 'M' && gender != 'F') {
                        throw new InvalidConfigurationException("Invalid gender in line: " + line);
                    }

                    try {
                        Student student = new Student(
                                parts[0],
                                gender,
                                Integer.parseInt(parts[2])
                        );
                        school.add(student);
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric student value in line: " + line);
                    }
                }

                else if (line.startsWith("Teacher:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before instructors");
                    }

                    String data = line.substring(8);
                    String[] parts = data.split(",");

                    if (parts.length != 3) {
                        throw new InvalidConfigurationException("Malformed Teacher line: " + line);
                    }
                    // CHECKING THE GENDER, IF EMPTY OR IF WRONG
                    if (parts[1].isEmpty()){
                        throw new InvalidConfigurationException("Missing gender in line: " + line);
                    }
                    char gender = parts[1].charAt(0);
                    if (gender != 'M' && gender != 'F') {
                        throw new InvalidConfigurationException("Invalid gender in line: " + line);
                    }

                    try {
                        Teacher teacher = new Teacher(
                                parts[0],
                                gender,
                                Integer.parseInt(parts[2])
                        );
                        school.add(teacher);
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric Teacher value in line: " + line);
                    }
                }

                else if (line.startsWith("Demonstrator:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before instructors");
                    }

                    String data = line.substring(13);
                    String[] parts = data.split(",");

                    if (parts.length != 3) {
                        throw new InvalidConfigurationException("Malformed Demonstrator line: " + line);
                    }
                    // CHECKING THE GENDER, IF EMPTY OR IF WRONG
                    if (parts[1].isEmpty()){
                        throw new InvalidConfigurationException("Missing gender in line: " + line);
                    }
                    char gender = parts[1].charAt(0);
                    if (gender != 'M' && gender != 'F') {
                        throw new InvalidConfigurationException("Invalid gender in line: " + line);
                    }

                    try {
                        Demonstrator demonstrator = new Demonstrator(
                                parts[0],
                                gender,
                                Integer.parseInt(parts[2])
                        );
                        school.add(demonstrator);
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric Demonstrator value in line: " + line);
                    }
                }

                else if (line.startsWith("OOTrainer:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before instructors");
                    }

                    String data = line.substring(10);
                    String[] parts = data.split(",");

                    if (parts.length != 3) {
                        throw new InvalidConfigurationException("Malformed OOTrainer line: " + line);
                    }
                    // CHECKING THE GENDER, IF EMPTY OR IF WRONG
                    if (parts[1].isEmpty()){
                        throw new InvalidConfigurationException("Missing gender in line: " + line);
                    }
                    char gender = parts[1].charAt(0);
                    if (gender != 'M' && gender != 'F') {
                        throw new InvalidConfigurationException("Invalid gender in line: " + line);
                    }

                    try {
                        OOTrainer ooTrainer = new OOTrainer(
                                parts[0],
                                gender,
                                Integer.parseInt(parts[2])
                        );
                        school.add(ooTrainer);
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric OOTrainer value in line: " + line);
                    }
                }

                else if (line.startsWith("GUITrainer:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before instructors");
                    }

                    String data = line.substring(11);
                    String[] parts = data.split(",");

                    if (parts.length != 3) {
                        throw new InvalidConfigurationException("Malformed GUITrainer line: " + line);
                    }
                    // CHECKING THE GENDER, IF EMPTY OR IF WRONG
                    if (parts[1].isEmpty()){
                        throw new InvalidConfigurationException("Missing gender in line: " + line);
                    }
                    char gender = parts[1].charAt(0);
                    if (gender != 'M' && gender != 'F') {
                        throw new InvalidConfigurationException("Invalid gender in line: " + line);
                    }

                    try {
                        GUITrainer guiTrainer = new GUITrainer(
                                parts[0],
                                gender,
                                Integer.parseInt(parts[2])
                        );
                        school.add(guiTrainer);
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric GUITrainer value in line: " + line);
                    }
                }

                else {
                    throw new InvalidConfigurationException("Unknown or malformed line: " + line);
                }
            }
        }

        if (school == null) {
            throw new InvalidConfigurationException("Missing school line");
        }

        return school;
    }

    // saves everything in the file to pause the simulation
    public void saveSimulation(String fileName) throws IOException {
        String outputFile = fileName.endsWith(".save.txt") ? fileName : fileName + ".save.txt";
        saveSimulationToFile(outputFile);
    }

    public void saveSimulationToFile(String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

            writer.println("school:" + school.getName());
            writer.println("day:" + currentDay);

            for (Subject subject : school.getSubjects()) {
                writer.println("subject:" + subject.getDescription() + "," +
                        subject.getID() + "," +
                        subject.getSpecialism() + "," +
                        subject.getDuration());
            }

            for (Student student : school.getStudents()) {
                writer.print("student:" + student.getName() + "," +
                        student.getGender() + "," +
                        student.getAge());

                for (Integer certificate : student.getCertificates()) {
                    writer.print("," + certificate);
                }

                writer.println();
            }

            for (Instructor instructor : school.getInstructors()) {
                String type = instructor.getClass().getSimpleName();
                writer.println(type + ":" + instructor.getName() + "," +
                        instructor.getGender() + "," +
                        instructor.getAge());
            }

            for (Course course : school.getCourses()) {
                writer.print("course:" + course.getSubject().getID() + "," +
                        course.getDaysUntilStarts() + "," +
                        course.getDaysToRun() + "," +
                        course.isCancelled());

                if (course.hasInstructor()) {
                    writer.print("," + course.getInstructor().getName());
                } else {
                    writer.print(",null");
                }

                for (Student student : course.getStudents()) {
                    writer.print("," + student.getName());
                }

                writer.println();
            }
        }
    }

    public static School loadSimulation(String fileName) throws IOException, InvalidConfigurationException {
        return loadSimulationState(fileName).school;
    }

    public static Administrator loadAdministratorFromSimulation(String fileName) throws IOException, InvalidConfigurationException {
        LoadedSimulation loaded = loadSimulationState(fileName);
        return new Administrator(loaded.school, loaded.currentDay);
    }

    private static LoadedSimulation loadSimulationState(String fileName) throws IOException, InvalidConfigurationException {
        School school = null;
        int currentDay = 0;
        ArrayList<String> courseLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                if (line.startsWith("school:")) {
                    String name = line.substring(7);

                    if (name.isEmpty()) {
                        throw new InvalidConfigurationException("Malformed school line: " + line);
                    }

                    school = new School(name);
                }

                else if (line.startsWith("day:")) {
                    try {
                        currentDay = Integer.parseInt(line.substring(4));
                        if (currentDay < 0) {
                            throw new InvalidConfigurationException("Day cannot be negative in save file");
                        }
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric day value in line: " + line);
                    }
                }

                else if (line.startsWith("subject:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before subjects");
                    }

                    String[] parts = line.substring(8).split(",");

                    if (parts.length != 4) {
                        throw new InvalidConfigurationException("Malformed subject line: " + line);
                    }

                    try {
                        Subject subject = new Subject(
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]),
                                Integer.parseInt(parts[3]),
                                parts[0]
                        );
                        school.add(subject);
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric subject value in line: " + line);
                    }
                }

                else if (line.startsWith("student:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before students");
                    }

                    String[] parts = line.substring(8).split(",");

                    if (parts.length < 3) {
                        throw new InvalidConfigurationException("Malformed student line: " + line);
                    }
                    // CHECKING THE GENDER, IF EMPTY OR IF WRONG
                    if (parts[1].isEmpty()){
                        throw new InvalidConfigurationException("Missing gender in line: " + line);
                    }
                    char gender = parts[1].charAt(0);
                    if (gender != 'M' && gender != 'F') {
                        throw new InvalidConfigurationException("Invalid gender in line: " + line);
                    }

                    try {
                        Student student = new Student(
                                parts[0],
                                gender,
                                Integer.parseInt(parts[2])
                        );

                        for (int i = 3; i < parts.length; i++) {
                            int certificateID = Integer.parseInt(parts[i]);
                            boolean foundSubject = false;

                            for (Subject subject : school.getSubjects()) {
                                if (subject.getID() == certificateID) {
                                    student.graduate(subject);
                                    foundSubject = true;
                                    break;
                                }
                            }

                            if (!foundSubject) {
                                throw new InvalidConfigurationException("Unknown certificate subject ID in line: " + line);
                            }
                        }

                        school.add(student);
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric student value in line: " + line);
                    }
                }

                else if (line.startsWith("Teacher:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before instructors");
                    }

                    String[] parts = line.substring(8).split(",");

                    if (parts.length != 3) {
                        throw new InvalidConfigurationException("Malformed Teacher line: " + line);
                    }
                    // CHECKING THE GENDER, IF EMPTY OR IF WRONG
                    if (parts[1].isEmpty()){
                        throw new InvalidConfigurationException("Missing gender in line: " + line);
                    }
                    char gender = parts[1].charAt(0);
                    if (gender != 'M' && gender != 'F') {
                        throw new InvalidConfigurationException("Invalid gender in line: " + line);
                    }

                    try {
                        school.add(new Teacher(parts[0], gender, Integer.parseInt(parts[2])));
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric Teacher value in line: " + line);
                    }
                }

                else if (line.startsWith("Demonstrator:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before instructors");
                    }

                    String[] parts = line.substring(13).split(",");

                    if (parts.length != 3) {
                        throw new InvalidConfigurationException("Malformed Demonstrator line: " + line);
                    }
                    // CHECKING THE GENDER, IF EMPTY OR IF WRONG
                    if (parts[1].isEmpty()){
                        throw new InvalidConfigurationException("Missing gender in line: " + line);
                    }
                    char gender = parts[1].charAt(0);
                    if (gender != 'M' && gender != 'F') {
                        throw new InvalidConfigurationException("Invalid gender in line: " + line);
                    }

                    try {
                        school.add(new Demonstrator(parts[0], gender, Integer.parseInt(parts[2])));
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric Demonstrator value in line: " + line);
                    }
                }

                else if (line.startsWith("GUITrainer:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before instructors");
                    }

                    String[] parts = line.substring(11).split(",");

                    if (parts.length != 3) {
                        throw new InvalidConfigurationException("Malformed GUITrainer line: " + line);
                    }
                    // CHECKING THE GENDER, IF EMPTY OR IF WRONG
                    if (parts[1].isEmpty()){
                        throw new InvalidConfigurationException("Missing gender in line: " + line);
                    }
                    char gender = parts[1].charAt(0);
                    if (gender != 'M' && gender != 'F') {
                        throw new InvalidConfigurationException("Invalid gender in line: " + line);
                    }

                    try {
                        school.add(new GUITrainer(parts[0], gender, Integer.parseInt(parts[2])));
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric GUITrainer value in line: " + line);
                    }
                }

                else if (line.startsWith("OOTrainer:")) {
                    if (school == null) {
                        throw new InvalidConfigurationException("School must be defined before instructors");
                    }

                    String[] parts = line.substring(10).split(",");

                    if (parts.length != 3) {
                        throw new InvalidConfigurationException("Malformed OOTrainer line: " + line);
                    }
                    // CHECKING THE GENDER, IF EMPTY OR IF WRONG
                    if (parts[1].isEmpty()){
                        throw new InvalidConfigurationException("Missing gender in line: " + line);
                    }
                    char gender = parts[1].charAt(0);
                    if (gender != 'M' && gender != 'F') {
                        throw new InvalidConfigurationException("Invalid gender in line: " + line);
                    }

                    try {
                        school.add(new OOTrainer(parts[0], gender, Integer.parseInt(parts[2])));
                    }
                    catch (NumberFormatException e) {
                        throw new InvalidConfigurationException("Non-numeric OOTrainer value in line: " + line);
                    }
                }

                else if (line.startsWith("course:")) {
                    courseLines.add(line);
                }

                else {
                    throw new InvalidConfigurationException("Unknown or malformed save-file line: " + line);
                }
            }
        }

        if (school == null) {
            throw new InvalidConfigurationException("Missing school line in save file");
        }

        for (String courseLine : courseLines) {
            String[] parts = courseLine.substring(7).split(",");

            if (parts.length < 5) {
                throw new InvalidConfigurationException("Malformed course line: " + courseLine);
            }

            int subjectID;
            int daysUntilStarts;
            int daysToRun;
            boolean cancelled;
            String instructorName;

            try {
                subjectID = Integer.parseInt(parts[0]);
                daysUntilStarts = Integer.parseInt(parts[1]);
                daysToRun = Integer.parseInt(parts[2]);
                cancelled = Boolean.parseBoolean(parts[3]);
                instructorName = parts[4];
            }
            catch (NumberFormatException e) {
                throw new InvalidConfigurationException("Non-numeric course value in line: " + courseLine);
            }

            Subject subject = null;
            for (Subject s : school.getSubjects()) {
                if (s.getID() == subjectID) {
                    subject = s;
                    break;
                }
            }

            if (subject == null) {
                throw new InvalidConfigurationException("Course refers to unknown subject ID: " + subjectID);
            }

            Course course = new Course(subject, daysUntilStarts);
            course.loadCourseState(daysUntilStarts, daysToRun, cancelled);

            if (!instructorName.equals("null")) {
                boolean foundInstructor = false;

                for (Instructor instructor : school.getInstructors()) {
                    if (instructor.getName().equals(instructorName)) {
                        if (!course.setInstructor(instructor)) {
                            throw new InvalidConfigurationException("Illegal operation: instructor cannot teach saved course");
                        }
                        foundInstructor = true;
                        break;
                    }
                }

                if (!foundInstructor) {
                    throw new InvalidConfigurationException("Course refers to unknown instructor: " + instructorName);
                }
            }

            for (int i = 5; i < parts.length; i++) {
                String studentName = parts[i];
                boolean foundStudent = false;

                for (Student student : school.getStudents()) {
                    if (student.getName().equals(studentName)) {

                        if (course.getSize() >= 3) {
                            throw new InvalidConfigurationException("Course exceeds maximum capacity");
                        }

                        for (Student enrolledStudent : course.getStudents()) {
                            if (enrolledStudent == student) {
                                throw new InvalidConfigurationException("Duplicate student in course: " + studentName);
                            }
                        }

                        course.addStudentFromSave(student);
                        foundStudent = true;
                        break;
                    }
                }

                if (!foundStudent) {
                    throw new InvalidConfigurationException("Course refers to unknown student: " + studentName);
                }
            }

            school.add(course);
        }

        return new LoadedSimulation(school, currentDay);
    }

    private static class LoadedSimulation {
        private School school;
        private int currentDay;

        public LoadedSimulation(School school, int currentDay) {
            this.school = school;
            this.currentDay = currentDay;
        }
    }

    public static void main(String[] args) {
        try {
            Administrator administrator;

            if (args.length == 0) {
                School school = createDefaultSchool();

                administrator = new Administrator(school);
                administrator.run();
            }

            else if (args.length == 2) {
                School school;

                if (args[0].endsWith(".save.txt")) {
                    administrator = loadAdministratorFromSimulation(args[0]);
                } else {
                    school = loadSchool(args[0]);
                    administrator = new Administrator(school);
                }
                administrator.run(Integer.parseInt(args[1]));
            }

            else {
                throw new InvalidConfigurationException("Usage: java Administrator OR java Administrator <file> <days>");
            }
        }
        catch (InvalidConfigurationException e) {
            System.out.println("Configuration error: " + e.getMessage());
        }
        catch (NumberFormatException e) {
            System.out.println("Numeric error: invalid number of days");
        }
        catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}




