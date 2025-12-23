package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.StudentRepository;
import com.ensa.v2school.sm.DAO.SubjectRepository;
import com.ensa.v2school.sm.Models.Student;
import com.ensa.v2school.sm.Models.Subject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class EnrollFormController implements Initializable {

    @FXML private Label studentNameLabel;
    @FXML private Label majorNameLabel;
    @FXML private ListView<Subject> subjectsListView;
    @FXML private Label statusLabel;

    private Student student;
    private StudentRepository studentRepository = new StudentRepository();
    private SubjectRepository subjectRepository = new SubjectRepository();

    // Map to track which subjects are selected
    private Map<Subject, javafx.beans.property.BooleanProperty> subjectSelectionMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("EnrollFormController initialized!");

        // Configure ListView with checkboxes
        if (subjectsListView != null) {
            setupCheckBoxListView();
        } else {
            System.err.println("ERROR: subjectsListView is NULL!");
        }
    }

    private void setupCheckBoxListView() {
        subjectsListView.setCellFactory(CheckBoxListCell.forListView(subject -> {
            javafx.beans.property.BooleanProperty observable = new javafx.beans.property.SimpleBooleanProperty();
            subjectSelectionMap.put(subject, observable);
            return observable;
        }));
    }

    public void setStudent(Student student) {
        this.student = student;

        if (student != null) {
            // Display student info
            studentNameLabel.setText(student.getFirstName() + " " + student.getLastName() + " (" + student.getId() + ")");

            if (student.getMajor() != null) {
                majorNameLabel.setText(student.getMajor().getMajorName());

                // Load subjects for this major
                loadSubjectsForMajor();
            } else {
                majorNameLabel.setText("No major assigned");
                showAlert("Warning", "This student has no major assigned!");
            }
        }
    }

    private void loadSubjectsForMajor() {
        try {
            // Get all subjects for the student's major
            List<Subject> availableSubjects = subjectRepository.findByMajorId(student.getMajor().getId());

            // Get subjects the student is already enrolled in
            List<Subject> enrolledSubjects = studentRepository.getEnrolledSubjects(student.getId());
            Set<Integer> enrolledSubjectIds = new HashSet<>();
            for (Subject s : enrolledSubjects) {
                enrolledSubjectIds.add(s.getId());
            }

            // Clear and populate the list
            subjectsListView.getItems().clear();
            subjectSelectionMap.clear();

            if (availableSubjects.isEmpty()) {
                statusLabel.setText("No subjects available for this major");
                statusLabel.setStyle("-fx-text-fill: #ef4444;");
                return;
            }

            subjectsListView.getItems().addAll(availableSubjects);

            // Pre-check already enrolled subjects
            javafx.application.Platform.runLater(() -> {
                for (Map.Entry<Subject, javafx.beans.property.BooleanProperty> entry : subjectSelectionMap.entrySet()) {
                    if (enrolledSubjectIds.contains(entry.getKey().getId())) {
                        entry.getValue().set(true);
                    }
                }
            });

            System.out.println("Loaded " + availableSubjects.size() + " subjects for major: " + student.getMajor().getMajorName());

        } catch (SQLException e) {
            System.err.println("Error loading subjects: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not load subjects: " + e.getMessage());
        }
    }

    @FXML
    private void handleEnroll() {
        // Get selected subjects
        List<Integer> selectedSubjectIds = new ArrayList<>();
        List<String> selectedSubjectNames = new ArrayList<>();

        for (Map.Entry<Subject, javafx.beans.property.BooleanProperty> entry : subjectSelectionMap.entrySet()) {
            if (entry.getValue().get()) {
                selectedSubjectIds.add(entry.getKey().getId());
                selectedSubjectNames.add(entry.getKey().getName());
            }
        }

        if (selectedSubjectIds.isEmpty()) {
            showAlert("Error", "Please select at least one subject!");
            return;
        }

        try {
            // Get currently enrolled subjects
            List<Subject> currentlyEnrolled = studentRepository.getEnrolledSubjects(student.getId());
            Set<Integer> currentlyEnrolledIds = new HashSet<>();
            for (Subject s : currentlyEnrolled) {
                currentlyEnrolledIds.add(s.getId());
            }

            // Find subjects to add (selected but not currently enrolled)
            List<Integer> subjectsToAdd = new ArrayList<>();
            for (Integer id : selectedSubjectIds) {
                if (!currentlyEnrolledIds.contains(id)) {
                    subjectsToAdd.add(id);
                }
            }

            // Find subjects to remove (currently enrolled but not selected)
            List<Integer> subjectsToRemove = new ArrayList<>();
            for (Integer id : currentlyEnrolledIds) {
                if (!selectedSubjectIds.contains(id)) {
                    subjectsToRemove.add(id);
                }
            }

            // Perform enrollment/unenrollment
            if (!subjectsToAdd.isEmpty()) {
                studentRepository.enrollInSubjects(student.getId(), subjectsToAdd);
            }

            if (!subjectsToRemove.isEmpty()) {
                studentRepository.unenrollFromSubjects(student.getId(), subjectsToRemove);
            }

            // Show success message
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText("Enrollment Updated");
            successAlert.setContentText(
                    "Student enrolled in " + selectedSubjectIds.size() + " subject(s):\n" +
                            String.join(", ", selectedSubjectNames)
            );
            successAlert.showAndWait();

            close();

        } catch (SQLException e) {
            System.err.println("SQL Error during enrollment: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to enroll student: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) studentNameLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}