1. Parts Attempted

Part 1 – Basic class structure and system modelling
Part 2 – Subjects and courses
Part 3 – Students and certificates
Part 4 – Instructor hierarchy and teaching specialisations
Part 5 – Course enrolment and instructor assignment
Part 6 – Simulation execution and daily reporting
Part 7 – Loading the school configuration from a file
Part 8 – Saving and resuming the simulation state and the exception handling
Part 9 – Use case diagram, class diagram, and 2 sequence diagrams

2. Extensions

Restoring saved courses:

During simulation resumption (loadSimulation), the program must reconstruct courses exactly as they existed at the moment the simulation was saved.
To support this, two helper methods were introduced in the Course class.

a. addStudentFromSave(Student student):

This method is used only when loading a saved simulation.
It restores the student list of a course directly without triggering the normal enrolment rules.
The standard method enrolStudent(Student) performs checks such as course capacity, course status, and student certificates.
When restoring a saved simulation these checks would incorrectly reject previously valid state, so this helper method allows the saved enrolment list to be reconstructed safely.

b. loadCourseState(int daysUntilStarts, int daysToRun, boolean cancelled)

This method restores the internal runtime state of a course when loading a saved simulation.
The constructor is used for normal course creation, but when restoring a saved course the following state must also be restored:
	- number of days until the course starts
	- number of days remaining for the course to run
	- whether the course has been cancelled
This method allows the saved course state to be reconstructed exactly.


