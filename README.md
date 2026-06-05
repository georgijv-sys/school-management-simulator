# School Management Simulator

Object-oriented Java simulation of a training school. It models students, instructors, subjects, course scheduling, enrolment, course completion, certificates, and simulation persistence through both a Swing dashboard and a console runner.

## Features

- Swing dashboard with live metrics, tables, event log, and trend chart.
- Day-by-day simulation of student arrivals, instructor changes, enrolment, course starts, completions, cancellations, and graduations.
- Instructor assignment based on subject specialisms.
- Save/load support for resuming simulations, including active courses and current day.
- Configuration-file loading with validation and clear error messages.
- Console runner for fixed-length or continuous simulations.

## Tech

- Java
- Java Swing
- Object-oriented design: inheritance, abstraction, polymorphism, encapsulation, collections, exceptions, and file I/O
- No external dependencies

## Quick Start

Compile the project:

```bash
javac -d out src/*.java
```

Run the dashboard:

```bash
java -cp out SchoolDashboardApp
```

Run a fixed-length console simulation from a configuration file:

```bash
java -cp out Administrator school.txt 30
```

Run the default console simulation continuously:

```bash
java -cp out Administrator
```

## Configuration

Example `school.txt`:

```text
school:Java Training School
subject:Basics,1,1,5
subject:Lab 1,2,2,2
student:Alice,F,20
Teacher:Dr Smith,F,35
Demonstrator:Alex,M,28
OOTrainer:Casey,F,31
GUITrainer:Morgan,M,33
```

Subject format:

```text
subject:<description>,<id>,<specialism>,<duration>
```

Specialisms:

```text
1 = Core
2 = Lab
3 = Object-oriented programming
4 = GUI programming
```

## Structure

```text
src/
  Administrator.java        Simulation runner, save/load logic, CLI entry point
  SchoolDashboardApp.java   Swing dashboard entry point
  School.java               School state and daily simulation logic
  Course.java               Course lifecycle and enrolment rules
  Person.java               Base person class
  Student.java              Student certificate state
  Instructor.java           Abstract instructor type
  Teacher.java              Core/lab instructor
  Demonstrator.java         Lab instructor
  OOTrainer.java            Object-oriented programming trainer
  GUITrainer.java           GUI trainer
  Subject.java              Subject metadata
  SimulationEvent.java      Simulation event model
```

## Verification

Checked by compiling all source files:

```bash
javac -d out src/*.java
```

Also verified:

- Running a 3-day console simulation from the example configuration.
- Saving and loading a simulation restores the current day.
