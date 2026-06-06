import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class SchoolDashboardApp {

    private Administrator administrator;
    private JFrame frame;
    private Timer autoRunTimer;
    private final Object simulationLock = new Object();
    private SwingWorker<?, ?> activeWorker;
    private ArrayList<JButton> actionButtons = new ArrayList<>();

    private JLabel dayValue;
    private JLabel studentsValue;
    private JLabel instructorsValue;
    private JLabel coursesValue;
    private JLabel certificatesValue;
    private JLabel utilisationValue;
    private JLabel statusLabel;
    private JButton autoButton;

    private CourseTableModel courseTableModel;
    private StudentTableModel studentTableModel;
    private InstructorTableModel instructorTableModel;
    private SubjectTableModel subjectTableModel;
    private EventListModel eventListModel;
    private StatsPanel statsPanel;

    private ArrayList<DailySnapshot> history = new ArrayList<>();

    public SchoolDashboardApp(Administrator administrator) {
        this.administrator = administrator;
        autoRunTimer = new Timer(700, event -> runDays(1));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // The default look and feel is fine if the system one is unavailable.
            }

            School school = Administrator.createDefaultSchool();
            SchoolDashboardApp app = new SchoolDashboardApp(new Administrator(school));
            app.show();
        });
    }

    public void show() {
        frame = new JFrame("School Operations Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1120, 720));
        frame.setSize(new Dimension(1120, 720));
        frame.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(new Color(246, 247, 249));

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createMainArea(), BorderLayout.CENTER);

        frame.setContentPane(root);
        refreshDashboard();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 12));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout(4, 4));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("School Operations Simulator");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(28, 35, 43));

        JLabel subtitle = new JLabel("Live course scheduling, enrolment, instructor allocation, persistence, and analytics");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(91, 99, 110));

        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        header.add(titlePanel, BorderLayout.NORTH);
        header.add(createControlPanel(), BorderLayout.CENTER);
        header.add(createMetricPanel(), BorderLayout.SOUTH);

        return header;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);

        JButton nextDay = createButton("Next Day");
        nextDay.addActionListener(event -> runDays(1));

        JButton week = createButton("Run 7 Days");
        week.addActionListener(event -> runDaysInBackground(7));

        JButton month = createButton("Run 30 Days");
        month.addActionListener(event -> runDaysInBackground(30));

        autoButton = createButton("Auto Run");
        autoButton.addActionListener(event -> {
            if (autoRunTimer.isRunning()) {
                autoRunTimer.stop();
                autoButton.setText("Auto Run");
                setStatus("Ready");
            } else {
                autoRunTimer.start();
                autoButton.setText("Pause");
                setStatus("Auto run active");
            }
        });

        JButton addStudent = createButton("Add Student");
        addStudent.addActionListener(event -> showAddStudentDialog());

        JButton addInstructor = createButton("Add Instructor");
        addInstructor.addActionListener(event -> showAddInstructorDialog());

        JButton addSubject = createButton("Add Subject");
        addSubject.addActionListener(event -> showAddSubjectDialog());

        JButton load = createButton("Load");
        load.addActionListener(event -> loadSchool());

        JButton save = createButton("Save");
        save.addActionListener(event -> saveSchool());

        controls.add(nextDay);
        controls.add(week);
        controls.add(month);
        controls.add(autoButton);
        controls.add(addStudent);
        controls.add(addInstructor);
        controls.add(addSubject);
        controls.add(load);
        controls.add(save);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(92, 101, 113));

        panel.add(controls, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        actionButtons.add(button);
        return button;
    }

    private JPanel createMetricPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 10, 10));
        panel.setOpaque(false);

        dayValue = new JLabel();
        studentsValue = new JLabel();
        instructorsValue = new JLabel();
        coursesValue = new JLabel();
        certificatesValue = new JLabel();
        utilisationValue = new JLabel();

        panel.add(createMetricCard("Day", dayValue, new Color(38, 86, 153)));
        panel.add(createMetricCard("Students", studentsValue, new Color(22, 128, 98)));
        panel.add(createMetricCard("Instructors", instructorsValue, new Color(138, 83, 17)));
        panel.add(createMetricCard("Active Courses", coursesValue, new Color(139, 63, 102)));
        panel.add(createMetricCard("Certificates", certificatesValue, new Color(85, 79, 166)));
        panel.add(createMetricCard("Instructor Use", utilisationValue, new Color(60, 94, 105)));

        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(new Color(92, 101, 113));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(new Color(28, 35, 43));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JSplitPane createMainArea() {
        courseTableModel = new CourseTableModel();
        studentTableModel = new StudentTableModel();
        instructorTableModel = new InstructorTableModel();
        subjectTableModel = new SubjectTableModel();
        eventListModel = new EventListModel();
        statsPanel = new StatsPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Courses", createTableScrollPane(new JTable(courseTableModel)));
        tabs.addTab("Students", createTableScrollPane(new JTable(studentTableModel)));
        tabs.addTab("Instructors", createTableScrollPane(new JTable(instructorTableModel)));
        tabs.addTab("Subjects", createTableScrollPane(new JTable(subjectTableModel)));

        JPanel rightPanel = new JPanel(new BorderLayout(12, 12));
        rightPanel.setOpaque(false);
        rightPanel.add(createSection("Trend", statsPanel), BorderLayout.NORTH);
        rightPanel.add(createEventLog(), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, rightPanel);
        splitPane.setResizeWeight(0.72);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        return splitPane;
    }

    private JScrollPane createTableScrollPane(JTable table) {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 8, 0, 8));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(221, 225, 230)));
        return scrollPane;
    }

    private JPanel createSection(String title, JPanel content) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(221, 225, 230)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(45, 53, 62));

        panel.add(label, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEventLog() {
        JList<String> eventList = new JList<>(eventListModel);
        eventList.setFont(new Font("SansSerif", Font.PLAIN, 12));
        eventList.setFixedCellHeight(24);

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(221, 225, 230)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel label = new JLabel("Event Log");
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(45, 53, 62));

        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(eventList), BorderLayout.CENTER);

        return panel;
    }

    private void runDays(int days) {
        if (isBackgroundWorkRunning()) {
            return;
        }

        ArrayList<SimulationEvent> events;
        synchronized (simulationLock) {
            events = administrator.advanceDays(days);
        }
        appendEvents(events);
        refreshDashboard();
    }

    private void runDaysInBackground(int days) {
        if (isBackgroundWorkRunning()) {
            return;
        }

        SwingWorker<ArrayList<SimulationEvent>, Void> worker = new SwingWorker<ArrayList<SimulationEvent>, Void>() {
            protected ArrayList<SimulationEvent> doInBackground() {
                synchronized (simulationLock) {
                    return administrator.advanceDays(days);
                }
            }

            protected void done() {
                try {
                    appendEvents(get());
                    refreshDashboard();
                    finishBackgroundWork("Completed " + days + " day run");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    finishBackgroundWork("Ready");
                    showError("Simulation interrupted", e);
                } catch (ExecutionException e) {
                    finishBackgroundWork("Ready");
                    showError("Could not run simulation", unwrapWorkerException(e));
                }
            }
        };

        startBackgroundWork(worker, "Running " + days + " days in background");
    }

    private void refreshDashboard() {
        synchronized (simulationLock) {
            School school = administrator.getSchool();
            int students = school.getStudents().length;
            int instructors = school.getInstructors().length;
            int courses = school.getCourses().length;
            int busyInstructors = instructors - school.getAvailableInstructorCount();
            int utilisation = instructors == 0 ? 0 : (busyInstructors * 100) / instructors;

            dayValue.setText(String.valueOf(administrator.getCurrentDay()));
            studentsValue.setText(String.valueOf(students));
            instructorsValue.setText(String.valueOf(instructors));
            coursesValue.setText(String.valueOf(courses));
            certificatesValue.setText(String.valueOf(school.getTotalCertificatesAwarded()));
            utilisationValue.setText(utilisation + "%");

            courseTableModel.refresh();
            studentTableModel.refresh();
            instructorTableModel.refresh();
            subjectTableModel.refresh();

            recordSnapshot();
        }
        statsPanel.repaint();
    }

    private void startBackgroundWork(SwingWorker<?, ?> worker, String status) {
        if (autoRunTimer.isRunning()) {
            autoRunTimer.stop();
            autoButton.setText("Auto Run");
        }
        activeWorker = worker;
        setControlsEnabled(false);
        setStatus(status);
        worker.execute();
    }

    private void finishBackgroundWork(String status) {
        activeWorker = null;
        setControlsEnabled(true);
        setStatus(status);
    }

    private boolean isBackgroundWorkRunning() {
        return activeWorker != null && !activeWorker.isDone();
    }

    private void setControlsEnabled(boolean enabled) {
        for (JButton button : actionButtons) {
            button.setEnabled(enabled);
        }
    }

    private void setStatus(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    private Exception unwrapWorkerException(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof Exception) {
            return (Exception) cause;
        }
        return e;
    }

    private void appendEvents(ArrayList<SimulationEvent> events) {
        if (events.isEmpty()) {
            eventListModel.addEvent("No major changes this day");
            return;
        }

        for (SimulationEvent event : events) {
            eventListModel.addEvent(event.toString());
        }
    }

    private void recordSnapshot() {
        School school = administrator.getSchool();
        DailySnapshot snapshot = new DailySnapshot(
                administrator.getCurrentDay(),
                school.getStudents().length,
                school.getInstructors().length,
                school.getCourses().length,
                school.getTotalCertificatesAwarded()
        );

        if (!history.isEmpty() && history.get(history.size() - 1).day == snapshot.day) {
            history.set(history.size() - 1, snapshot);
        } else {
            history.add(snapshot);
        }

        if (history.size() > 80) {
            history.remove(0);
        }
    }

    private void showAddStudentDialog() {
        JTextField name = new JTextField("Student" + System.currentTimeMillis() % 10000);
        JComboBox<String> gender = new JComboBox<>(new String[]{"M", "F"});
        JTextField age = new JTextField("20");

        Object[] fields = {
                "Name", name,
                "Gender", gender,
                "Age", age
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Add Student", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Student student = new Student(name.getText().trim(), gender.getSelectedItem().toString().charAt(0),
                        Integer.parseInt(age.getText().trim()));
                administrator.getSchool().add(student);
                eventListModel.addEvent("Manual - Added student " + student.getName());
                refreshDashboard();
            } catch (Exception e) {
                showError("Could not add student", e);
            }
        }
    }

    private void showAddInstructorDialog() {
        JTextField name = new JTextField("Instructor" + System.currentTimeMillis() % 10000);
        JComboBox<String> type = new JComboBox<>(new String[]{"Teacher", "Demonstrator", "OOTrainer", "GUITrainer"});
        JComboBox<String> gender = new JComboBox<>(new String[]{"M", "F"});
        JTextField age = new JTextField("32");

        Object[] fields = {
                "Type", type,
                "Name", name,
                "Gender", gender,
                "Age", age
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Add Instructor", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Instructor instructor = createInstructor(
                        type.getSelectedItem().toString(),
                        name.getText().trim(),
                        gender.getSelectedItem().toString().charAt(0),
                        Integer.parseInt(age.getText().trim())
                );
                administrator.getSchool().add(instructor);
                eventListModel.addEvent("Manual - Added " + instructor.getClass().getSimpleName() + " " + instructor.getName());
                refreshDashboard();
            } catch (Exception e) {
                showError("Could not add instructor", e);
            }
        }
    }

    private Instructor createInstructor(String type, String name, char gender, int age) {
        if (type.equals("Demonstrator")) {
            return new Demonstrator(name, gender, age);
        }
        if (type.equals("OOTrainer")) {
            return new OOTrainer(name, gender, age);
        }
        if (type.equals("GUITrainer")) {
            return new GUITrainer(name, gender, age);
        }
        return new Teacher(name, gender, age);
    }

    private void showAddSubjectDialog() {
        JTextField description = new JTextField("New Subject");
        JTextField id = new JTextField(String.valueOf(nextSubjectID()));
        JTextField specialism = new JTextField("1");
        JTextField duration = new JTextField("4");

        Object[] fields = {
                "Description", description,
                "ID", id,
                "Specialism", specialism,
                "Duration", duration
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Add Subject", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Subject subject = new Subject(
                        Integer.parseInt(id.getText().trim()),
                        Integer.parseInt(specialism.getText().trim()),
                        Integer.parseInt(duration.getText().trim()),
                        description.getText().trim()
                );
                administrator.getSchool().add(subject);
                eventListModel.addEvent("Manual - Added subject " + subject.getDescription());
                refreshDashboard();
            } catch (Exception e) {
                showError("Could not add subject", e);
            }
        }
    }

    private int nextSubjectID() {
        int max = 0;
        for (Subject subject : administrator.getSchool().getSubjects()) {
            if (subject.getID() > max) {
                max = subject.getID();
            }
        }
        return max + 1;
    }

    private void saveSchool() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("school.save.txt"));
        int result = chooser.showSaveDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            saveSchoolInBackground(chooser.getSelectedFile());
        }
    }

    private void saveSchoolInBackground(File file) {
        if (isBackgroundWorkRunning()) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws IOException {
                synchronized (simulationLock) {
                    administrator.saveSimulationToFile(file.getAbsolutePath());
                }
                return null;
            }

            protected void done() {
                try {
                    get();
                    eventListModel.addEvent("Saved simulation to " + file.getName());
                    finishBackgroundWork("Saved " + file.getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    finishBackgroundWork("Ready");
                    showError("Save interrupted", e);
                } catch (ExecutionException e) {
                    finishBackgroundWork("Ready");
                    showError("Could not save simulation", unwrapWorkerException(e));
                }
            }
        };

        startBackgroundWork(worker, "Saving in background");
    }

    private void loadSchool() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            loadSchoolInBackground(chooser.getSelectedFile());
        }
    }

    private void loadSchoolInBackground(File file) {
        if (isBackgroundWorkRunning()) {
            return;
        }

        SwingWorker<Administrator, Void> worker = new SwingWorker<Administrator, Void>() {
            protected Administrator doInBackground() throws IOException, InvalidConfigurationException {
                if (file.getName().endsWith(".save.txt")) {
                    return Administrator.loadAdministratorFromSimulation(file.getAbsolutePath());
                }
                return new Administrator(Administrator.loadSchool(file.getAbsolutePath()));
            }

            protected void done() {
                try {
                    Administrator loadedAdministrator = get();
                    synchronized (simulationLock) {
                        administrator = loadedAdministrator;
                    }

                    history.clear();
                    eventListModel.clear();
                    eventListModel.addEvent("Loaded " + file.getName());
                    refreshDashboard();
                    finishBackgroundWork("Loaded " + file.getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    finishBackgroundWork("Ready");
                    showError("Load interrupted", e);
                } catch (ExecutionException e) {
                    finishBackgroundWork("Ready");
                    showError("Could not load school", unwrapWorkerException(e));
                }
            }
        };

        startBackgroundWork(worker, "Loading in background");
    }

    private void showError(String title, Exception e) {
        JOptionPane.showMessageDialog(frame, e.getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }

    private String courseStatus(Course course) {
        int status = course.getStatus();
        if (course.isCancelled()) {
            return "Cancelled";
        }
        if (status < 0) {
            return "Waiting";
        }
        if (status > 0) {
            return "Running";
        }
        return "Finished";
    }

    private String courseTiming(Course course) {
        int status = course.getStatus();
        if (status < 0) {
            return "Starts in " + (-status) + " day(s)";
        }
        if (status > 0) {
            return status + " day(s) left";
        }
        return "Complete";
    }

    private String studentNames(Course course) {
        Student[] students = course.getStudents();
        if (students.length == 0) {
            return "None";
        }

        String result = "";
        for (int i = 0; i < students.length; i++) {
            result += students[i].getName();
            if (i < students.length - 1) {
                result += ", ";
            }
        }
        return result;
    }

    private String instructorName(Course course) {
        if (course.getInstructor() == null) {
            return "Unassigned";
        }
        return course.getInstructor().getName();
    }

    private String specialismName(int specialism) {
        if (specialism == 1) {
            return "Core";
        }
        if (specialism == 2) {
            return "Lab";
        }
        if (specialism == 3) {
            return "OO";
        }
        if (specialism == 4) {
            return "GUI";
        }
        return "Specialism " + specialism;
    }

    private class CourseTableModel extends AbstractTableModel {
        private String[] columns = {"Subject", "State", "Timing", "Instructor", "Students", "Capacity", "Risk"};
        private Course[] courses = new Course[0];

        public void refresh() {
            courses = administrator.getSchool().getCourses();
            fireTableDataChanged();
        }

        public int getRowCount() {
            return courses.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public String getColumnName(int column) {
            return columns[column];
        }

        public Object getValueAt(int row, int column) {
            synchronized (simulationLock) {
                Course course = courses[row];
                if (column == 0) {
                    return course.getSubject().getDescription();
                }
                if (column == 1) {
                    return courseStatus(course);
                }
                if (column == 2) {
                    return courseTiming(course);
                }
                if (column == 3) {
                    return instructorName(course);
                }
                if (column == 4) {
                    return studentNames(course);
                }
                if (column == 5) {
                    return course.getSize() + "/3";
                }

                if (course.getStatus() < 0 && (!course.hasInstructor() || course.getSize() == 0)) {
                    return "At risk";
                }
                if (course.getStatus() > 0) {
                    return "On track";
                }
                return "Ready";
            }
        }
    }

    private class StudentTableModel extends AbstractTableModel {
        private String[] columns = {"Name", "Gender", "Age", "Certificates", "Current Course", "Progress"};
        private Student[] students = new Student[0];

        public void refresh() {
            students = administrator.getSchool().getStudents();
            fireTableDataChanged();
        }

        public int getRowCount() {
            return students.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public String getColumnName(int column) {
            return columns[column];
        }

        public Object getValueAt(int row, int column) {
            synchronized (simulationLock) {
                Student student = students[row];
                School school = administrator.getSchool();
                Course course = school.getCourseForStudent(student);
                if (column == 0) {
                    return student.getName();
                }
                if (column == 1) {
                    return student.getGender();
                }
                if (column == 2) {
                    return student.getAge();
                }
                if (column == 3) {
                    return student.getCertificates();
                }
                if (column == 4) {
                    return course == null ? "Waiting" : course.getSubject().getDescription();
                }
                return student.getCertificates().size() + "/" + school.getSubjects().length;
            }
        }
    }

    private class InstructorTableModel extends AbstractTableModel {
        private String[] columns = {"Name", "Type", "Gender", "Age", "Assigned Course", "Can Teach", "Status"};
        private Instructor[] instructors = new Instructor[0];

        public void refresh() {
            instructors = administrator.getSchool().getInstructors();
            fireTableDataChanged();
        }

        public int getRowCount() {
            return instructors.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public String getColumnName(int column) {
            return columns[column];
        }

        public Object getValueAt(int row, int column) {
            synchronized (simulationLock) {
                Instructor instructor = instructors[row];
                if (column == 0) {
                    return instructor.getName();
                }
                if (column == 1) {
                    return instructor.getClass().getSimpleName();
                }
                if (column == 2) {
                    return instructor.getGender();
                }
                if (column == 3) {
                    return instructor.getAge();
                }
                if (column == 4) {
                    Course course = instructor.getAssignedCourse();
                    return course == null ? "Available" : course.getSubject().getDescription();
                }
                if (column == 5) {
                    return teachableSubjects(instructor);
                }
                return instructor.getAssignedCourse() == null ? "Available" : "Busy";
            }
        }

        private String teachableSubjects(Instructor instructor) {
            String result = "";
            Subject[] subjects = administrator.getSchool().getSubjects();
            for (Subject subject : subjects) {
                if (instructor.canTeach(subject)) {
                    if (!result.isEmpty()) {
                        result += ", ";
                    }
                    result += subject.getDescription();
                }
            }
            return result.isEmpty() ? "None" : result;
        }
    }

    private class SubjectTableModel extends AbstractTableModel {
        private String[] columns = {"ID", "Description", "Specialism", "Duration", "Active Course"};
        private Subject[] subjects = new Subject[0];

        public void refresh() {
            subjects = administrator.getSchool().getSubjects();
            fireTableDataChanged();
        }

        public int getRowCount() {
            return subjects.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        public String getColumnName(int column) {
            return columns[column];
        }

        public Object getValueAt(int row, int column) {
            synchronized (simulationLock) {
                Subject subject = subjects[row];
                if (column == 0) {
                    return subject.getID();
                }
                if (column == 1) {
                    return subject.getDescription();
                }
                if (column == 2) {
                    return specialismName(subject.getSpecialism());
                }
                if (column == 3) {
                    return subject.getDuration() + " day(s)";
                }

                Course course = administrator.getSchool().getOpenCourseForSubject(subject);
                return course == null ? "No active course" : courseStatus(course) + " - " + courseTiming(course);
            }
        }
    }

    private class EventListModel extends javax.swing.AbstractListModel<String> {
        private ArrayList<String> events = new ArrayList<>();

        public int getSize() {
            return events.size();
        }

        public String getElementAt(int index) {
            return events.get(index);
        }

        public void addEvent(String event) {
            events.add(0, event);
            fireIntervalAdded(this, 0, 0);
            if (events.size() > 300) {
                events.remove(events.size() - 1);
                fireIntervalRemoved(this, events.size(), events.size());
            }
        }

        public void clear() {
            int oldSize = events.size();
            events.clear();
            if (oldSize > 0) {
                fireIntervalRemoved(this, 0, oldSize - 1);
            }
        }
    }

    private class StatsPanel extends JPanel {
        public StatsPanel() {
            setPreferredSize(new Dimension(320, 190));
            setBackground(Color.WHITE);
        }

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 42;
            int right = width - 16;
            int top = 18;
            int bottom = height - 32;

            g.setColor(new Color(232, 235, 239));
            g.drawLine(left, bottom, right, bottom);
            g.drawLine(left, top, left, bottom);

            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(92, 101, 113));
            g.drawString("People", 4, top + 4);
            g.drawString("Courses", 4, bottom - 34);
            g.drawString("Certs", 4, bottom - 8);

            if (history.size() < 2) {
                g.setColor(new Color(120, 128, 138));
                g.drawString("Run the simulation to build trend data.", left + 16, (top + bottom) / 2);
                return;
            }

            int maxValue = 1;
            for (DailySnapshot snapshot : history) {
                maxValue = Math.max(maxValue, snapshot.students + snapshot.instructors);
                maxValue = Math.max(maxValue, snapshot.courses);
                maxValue = Math.max(maxValue, snapshot.certificates);
            }

            drawLine(g, left, right, top, bottom, maxValue, new Color(38, 86, 153), Metric.PEOPLE);
            drawLine(g, left, right, top, bottom, maxValue, new Color(139, 63, 102), Metric.COURSES);
            drawLine(g, left, right, top, bottom, maxValue, new Color(22, 128, 98), Metric.CERTIFICATES);

            int legendY = bottom + 20;
            drawLegend(g, left, legendY, new Color(38, 86, 153), "People");
            drawLegend(g, left + 86, legendY, new Color(139, 63, 102), "Courses");
            drawLegend(g, left + 178, legendY, new Color(22, 128, 98), "Certificates");
        }

        private void drawLine(Graphics2D g, int left, int right, int top, int bottom, int maxValue, Color color, Metric metric) {
            g.setColor(color);

            int previousX = -1;
            int previousY = -1;
            int count = history.size();

            for (int i = 0; i < count; i++) {
                DailySnapshot snapshot = history.get(i);
                int value = snapshot.value(metric);
                int x = left + ((right - left) * i) / (count - 1);
                int y = bottom - ((bottom - top) * value) / maxValue;

                if (previousX >= 0) {
                    g.drawLine(previousX, previousY, x, y);
                }

                g.fillOval(x - 3, y - 3, 6, 6);
                previousX = x;
                previousY = y;
            }
        }

        private void drawLegend(Graphics2D g, int x, int y, Color color, String text) {
            g.setColor(color);
            g.fillRect(x, y - 9, 10, 10);
            g.setColor(new Color(70, 78, 89));
            g.drawString(text, x + 14, y);
        }
    }

    private enum Metric {
        PEOPLE,
        COURSES,
        CERTIFICATES
    }

    private static class DailySnapshot {
        private int day;
        private int students;
        private int instructors;
        private int courses;
        private int certificates;

        public DailySnapshot(int day, int students, int instructors, int courses, int certificates) {
            this.day = day;
            this.students = students;
            this.instructors = instructors;
            this.courses = courses;
            this.certificates = certificates;
        }

        public int value(Metric metric) {
            if (metric == Metric.PEOPLE) {
                return students + instructors;
            }
            if (metric == Metric.COURSES) {
                return courses;
            }
            return certificates;
        }
    }
}
