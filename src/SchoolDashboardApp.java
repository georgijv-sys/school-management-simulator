import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import javax.swing.table.JTableHeader;
import java.awt.BasicStroke;
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
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(Theme.BACKGROUND);

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
        title.setFont(Theme.font(Font.BOLD, 22));
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Live course scheduling, enrolment, instructor allocation, persistence, and analytics");
        subtitle.setFont(Theme.font(Font.PLAIN, 13));
        subtitle.setForeground(Theme.TEXT_SECONDARY);

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

        JButton nextDay = createPrimaryButton("Next Day");
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
        statusLabel.setFont(Theme.font(Font.PLAIN, 12));
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        statusLabel.setBorder(new EmptyBorder(4, 2, 0, 0));

        panel.add(controls, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createButton(String text) {
        JButton button = new Theme.PillButton(text, false);
        actionButtons.add(button);
        return button;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new Theme.PillButton(text, true);
        actionButtons.add(button);
        return button;
    }

    private JPanel createMetricPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 12, 12));
        panel.setOpaque(false);

        dayValue = new JLabel();
        studentsValue = new JLabel();
        instructorsValue = new JLabel();
        coursesValue = new JLabel();
        certificatesValue = new JLabel();
        utilisationValue = new JLabel();

        panel.add(createMetricCard("Day", dayValue, Theme.INDIGO));
        panel.add(createMetricCard("Students", studentsValue, Theme.EMERALD));
        panel.add(createMetricCard("Instructors", instructorsValue, Theme.AMBER));
        panel.add(createMetricCard("Active Courses", coursesValue, Theme.ROSE));
        panel.add(createMetricCard("Certificates", certificatesValue, Theme.VIOLET));
        panel.add(createMetricCard("Instructor Use", utilisationValue, Theme.CYAN));

        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new Theme.CardPanel(new BorderLayout(4, 6), 14);

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.smallCapsFont(11));
        titleLabel.setForeground(Theme.TEXT_SECONDARY);
        titleLabel.setIcon(Theme.dot(accent, 8));
        titleLabel.setIconTextGap(6);

        valueLabel.setFont(Theme.font(Font.BOLD, 26));
        valueLabel.setForeground(Theme.TEXT_PRIMARY);

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

        JTable courseTable = new JTable(courseTableModel);
        JTable studentTable = new JTable(studentTableModel);
        JTable instructorTable = new JTable(instructorTableModel);
        JTable subjectTable = new JTable(subjectTableModel);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.font(Font.BOLD, 13));
        tabs.addTab("Courses", createTableScrollPane(courseTable));
        tabs.addTab("Students", createTableScrollPane(studentTable));
        tabs.addTab("Instructors", createTableScrollPane(instructorTable));
        tabs.addTab("Subjects", createTableScrollPane(subjectTable));

        courseTable.getColumnModel().getColumn(1).setCellRenderer(new Theme.BadgeRenderer());
        courseTable.getColumnModel().getColumn(6).setCellRenderer(new Theme.BadgeRenderer());
        instructorTable.getColumnModel().getColumn(6).setCellRenderer(new Theme.BadgeRenderer());

        JPanel rightPanel = new JPanel(new BorderLayout(16, 16));
        rightPanel.setOpaque(false);
        rightPanel.add(createSection("Trend", statsPanel), BorderLayout.NORTH);
        rightPanel.add(createEventLog(), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, rightPanel);
        splitPane.setResizeWeight(0.72);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setDividerSize(8);
        return splitPane;
    }

    private JScrollPane createTableScrollPane(JTable table) {
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(Theme.SURFACE);
        table.setSelectionBackground(Theme.ROW_SELECTED);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.font(Font.PLAIN, 13));

        JTableHeader header = table.getTableHeader();
        header.setFont(Theme.font(Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 34));
        header.setDefaultRenderer(new Theme.HeaderRenderer(header.getDefaultRenderer()));

        Theme.StripedCellRenderer renderer = new Theme.StripedCellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scrollPane.getViewport().setBackground(Theme.SURFACE);
        return scrollPane;
    }

    private JPanel createSection(String title, JPanel content) {
        JPanel panel = new Theme.CardPanel(new BorderLayout(6, 10), 16);

        JLabel label = new JLabel(title.toUpperCase());
        label.setFont(Theme.smallCapsFont(11));
        label.setForeground(Theme.TEXT_SECONDARY);

        panel.add(label, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEventLog() {
        JList<String> eventList = new JList<>(eventListModel);
        eventList.setFont(Theme.font(Font.PLAIN, 13));
        eventList.setFixedCellHeight(30);
        eventList.setCellRenderer(new Theme.EventCellRenderer());
        eventList.setBackground(Theme.SURFACE);

        JPanel panel = new Theme.CardPanel(new BorderLayout(6, 10), 16);

        JLabel label = new JLabel("EVENT LOG");
        label.setFont(Theme.smallCapsFont(11));
        label.setForeground(Theme.TEXT_SECONDARY);

        JScrollPane scrollPane = new JScrollPane(eventList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.SURFACE);

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

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

    private JPanel createFormPanel(Object[] rows) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));

        for (int i = 0; i < rows.length; i += 2) {
            JLabel label = new JLabel(rows[i].toString());
            label.setFont(Theme.font(Font.BOLD, 12));
            label.setForeground(Theme.TEXT_SECONDARY);
            label.setAlignmentX(0f);
            label.setBorder(new EmptyBorder(i == 0 ? 0 : 10, 0, 3, 0));
            panel.add(label);

            JComponent field = (JComponent) rows[i + 1];
            field.setFont(Theme.font(Font.PLAIN, 13));
            if (field instanceof JTextField) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.BORDER),
                        new EmptyBorder(6, 8, 6, 8)));
            }
            field.setAlignmentX(0f);
            Dimension fieldSize = new Dimension(280, field.getPreferredSize().height);
            field.setPreferredSize(fieldSize);
            field.setMaximumSize(fieldSize);
            panel.add(field);
        }
        return panel;
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

        int result = JOptionPane.showConfirmDialog(frame, createFormPanel(fields), "Add Student",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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

        int result = JOptionPane.showConfirmDialog(frame, createFormPanel(fields), "Add Instructor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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

        while (true) {
            int result = JOptionPane.showConfirmDialog(frame, createFormPanel(fields), "Add Subject",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                int subjectId = Integer.parseInt(id.getText().trim());
                if (subjectIdExists(subjectId)) {
                    JOptionPane.showMessageDialog(frame,
                            "A subject with ID " + subjectId + " already exists. Please choose a different ID.",
                            "Duplicate ID", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                Subject subject = new Subject(
                        subjectId,
                        Integer.parseInt(specialism.getText().trim()),
                        Integer.parseInt(duration.getText().trim()),
                        description.getText().trim()
                );
                administrator.getSchool().add(subject);
                eventListModel.addEvent("Manual - Added subject " + subject.getDescription());
                refreshDashboard();
                return;
            } catch (Exception e) {
                showError("Could not add subject", e);
                return;
            }
        }
    }

    private boolean subjectIdExists(int id) {
        for (Subject subject : administrator.getSchool().getSubjects()) {
            if (subject.getID() == id) {
                return true;
            }
        }
        return false;
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
                    return course.getSize() + "/50";
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
            setPreferredSize(new Dimension(320, 200));
            setOpaque(false);
        }

        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int left = 10;
            int right = width - 10;
            int top = 16;
            int bottom = height - 30;

            g.setColor(Theme.CHART_GRID);
            for (int i = 0; i <= 4; i++) {
                int y = top + ((bottom - top) * i) / 4;
                g.drawLine(left, y, right, y);
            }

            if (history.size() < 2) {
                g.setFont(Theme.font(Font.PLAIN, 12));
                g.setColor(Theme.TEXT_MUTED);
                String message = "Run the simulation to build trend data.";
                int textWidth = g.getFontMetrics().stringWidth(message);
                g.drawString(message, (width - textWidth) / 2, (top + bottom) / 2);
                return;
            }

            int maxValue = 1;
            for (DailySnapshot snapshot : history) {
                maxValue = Math.max(maxValue, snapshot.students + snapshot.instructors);
                maxValue = Math.max(maxValue, snapshot.courses);
                maxValue = Math.max(maxValue, snapshot.certificates);
            }

            g.setFont(Theme.font(Font.PLAIN, 10));
            g.setColor(Theme.TEXT_MUTED);
            g.drawString(String.valueOf(maxValue), left + 2, top - 5);

            drawLine(g, left, right, top, bottom, maxValue, Theme.CHART_PEOPLE, Metric.PEOPLE);
            drawLine(g, left, right, top, bottom, maxValue, Theme.CHART_COURSES, Metric.COURSES);
            drawLine(g, left, right, top, bottom, maxValue, Theme.CHART_CERTIFICATES, Metric.CERTIFICATES);

            int legendY = bottom + 20;
            g.setFont(Theme.font(Font.PLAIN, 11));
            drawLegend(g, left, legendY, Theme.CHART_PEOPLE, "People");
            drawLegend(g, left + 80, legendY, Theme.CHART_COURSES, "Courses");
            drawLegend(g, left + 168, legendY, Theme.CHART_CERTIFICATES, "Certificates");
        }

        private void drawLine(Graphics2D g, int left, int right, int top, int bottom, int maxValue, Color color, Metric metric) {
            int count = history.size();
            int[] xs = new int[count];
            int[] ys = new int[count];

            for (int i = 0; i < count; i++) {
                DailySnapshot snapshot = history.get(i);
                int value = snapshot.value(metric);
                xs[i] = left + ((right - left) * i) / (count - 1);
                ys[i] = bottom - ((bottom - top) * value) / maxValue;
            }

            g.setColor(color);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 1; i < count; i++) {
                g.drawLine(xs[i - 1], ys[i - 1], xs[i], ys[i]);
            }

            g.setStroke(new BasicStroke(1.5f));
            for (int i = 0; i < count; i++) {
                g.setColor(Theme.SURFACE);
                g.fillOval(xs[i] - 3, ys[i] - 3, 6, 6);
                g.setColor(color);
                g.drawOval(xs[i] - 3, ys[i] - 3, 6, 6);
            }
            g.setStroke(new BasicStroke(1f));
        }

        private void drawLegend(Graphics2D g, int x, int y, Color color, String text) {
            g.setColor(color);
            g.fillOval(x, y - 8, 8, 8);
            g.setColor(Theme.TEXT_SECONDARY);
            g.drawString(text, x + 13, y);
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
