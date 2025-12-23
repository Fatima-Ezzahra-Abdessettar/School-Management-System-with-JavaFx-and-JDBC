package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.DAO.StudentRepository;
import com.ensa.v2school.sm.DAO.SubjectRepository;
import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Student;
import com.ensa.v2school.sm.Models.Subject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MajorDetailsController implements Initializable {

    @FXML private Label majorTitleLabel;
    @FXML private Label majorNameLabel;
    @FXML private Label majorDescLabel;
    @FXML private Label studentCountLabel;
    @FXML private Label subjectCountLabel;

    @FXML private TableView<Subject> subjectsTable;
    @FXML private TableColumn<Subject, Integer> subjectIdCol;
    @FXML private TableColumn<Subject, String> subjectNameCol;

    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> studentIdCol;
    @FXML private TableColumn<Student, String> firstNameCol;
    @FXML private TableColumn<Student, String> lastNameCol;
    @FXML private TableColumn<Student, Float> averageCol;

    private Major major;
    private final StudentRepository studentRepository = new StudentRepository();
    private final SubjectRepository subjectRepository = new SubjectRepository();
    private final MajorRepository majorRepository = new MajorRepository();

    public void setMajor(Major major) {
        this.major = major;
        loadMajorDetails();
        loadSubjects();
        loadStudents();
    }

    private void loadMajorDetails() {
        if (major != null) {
            majorTitleLabel.setText(major.getMajorName() + " Details");
            majorNameLabel.setText("Name: " + major.getMajorName());
            majorDescLabel.setText("Description: " + (major.getDescription() != null ? major.getDescription() : "No description available"));
        }
    }

    private void loadSubjects() {
        try {
            List<Subject> subjects = subjectRepository.findByMajorId(major.getId());
            subjectsTable.getItems().clear();
            subjectsTable.getItems().addAll(subjects);
            subjectCountLabel.setText("Total subjects: " + subjects.size());

            if (subjects.isEmpty()) {
                System.out.println("No subjects are currently assigned to this major.");
            }
        } catch (SQLException e) {
            System.err.println("Error loading subjects: " + e.getMessage());
            showAlert("Database Error", "Could not load subjects for this major!");
        }
    }

    private void loadStudents() {
        try {
            List<Student> students = studentRepository.findByMajor(major.getId());
            studentsTable.getItems().clear();
            studentsTable.getItems().addAll(students);
            studentCountLabel.setText("Total students: " + students.size());

            if (students.isEmpty()) {
                System.out.println("No students are currently enrolled in this major.");
            }
        } catch (SQLException e) {
            System.err.println("Error loading students: " + e.getMessage());
            showAlert("Database Error", "Could not load students for this major!");
        }
    }

    @FXML
    private void handleAddSubject() {
        try {
            // Get all subjects
            List<Subject> allSubjects = subjectRepository.getAll();

            // Get subjects already in this major
            List<Subject> currentSubjects = subjectRepository.findByMajorId(major.getId());
            List<Integer> currentSubjectIds = currentSubjects.stream()
                    .map(Subject::getId)
                    .toList();

            // Filter out subjects already in this major
            List<Subject> availableSubjects = allSubjects.stream()
                    .filter(s -> !currentSubjectIds.contains(s.getId()))
                    .toList();

            if (availableSubjects.isEmpty()) {
                showInfoAlert("No Available Subjects", "All subjects are already assigned to this major.");
                return;
            }

            // Create choice dialog
            ChoiceDialog<Subject> dialog = new ChoiceDialog<>(availableSubjects.getFirst(), availableSubjects);
            dialog.setTitle("Add Subject");
            dialog.setHeaderText("Add Subject to " + major.getMajorName());
            dialog.setContentText("Choose a subject:");

            Optional<Subject> result = dialog.showAndWait();

            if (result.isPresent()) {
                Subject selectedSubject = result.get();

                // Add this major to the subject's list of majors
                selectedSubject.setMajors(subjectRepository.get(selectedSubject.getId())
                        .orElse(selectedSubject).getMajors());

                if (selectedSubject.getMajors() == null) {
                    selectedSubject.setMajors(new ArrayList<>());
                }

                // Check if major is not already in the list
                boolean majorExists = selectedSubject.getMajors().stream()
                        .anyMatch(m -> m.getId() == major.getId());

                if (!majorExists) {
                    selectedSubject.getMajors().add(major);
                    subjectRepository.update(selectedSubject);

                    showSuccessAlert("Subject added successfully to " + major.getMajorName());
                    loadSubjects();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error adding subject: " + e.getMessage());
            showAlert("Database Error", "Could not add subject to this major!");
        }
    }

    @FXML
    private void handleRemoveSubject() {
        Subject selected = subjectsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Selection", "Please select a subject to remove!");
            return;
        }

        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Remove Subject");
        confirmAlert.setHeaderText("Remove " + selected.getName() + " from " + major.getMajorName() + "?");
        confirmAlert.setContentText("This will remove the association between this subject and major. The subject itself will not be deleted.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Get the full subject with all its majors
                Optional<Subject> fullSubject = subjectRepository.get(selected.getId());

                if (fullSubject.isPresent()) {
                    Subject subjectToUpdate = fullSubject.get();

                    // Remove this major from the subject's list
                    List<Major> updatedMajors = subjectToUpdate.getMajors().stream()
                            .filter(m -> m.getId() != major.getId())
                            .collect(Collectors.toList());

                    subjectToUpdate.setMajors(updatedMajors);

                    // Update in database
                    subjectRepository.update(subjectToUpdate);

                    showSuccessAlert("Subject removed successfully from " + major.getMajorName());
                    loadSubjects();
                } else {
                    showAlert("Error", "Subject not found in database!");
                }

            } catch (SQLException e) {
                System.err.println("Error removing subject: " + e.getMessage());
                e.printStackTrace();
                showAlert("Database Error", "Could not remove subject from this major!");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) majorTitleLabel.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Subjects Table setup
        subjectIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        subjectNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Students Table setup
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        averageCol.setCellValueFactory(new PropertyValueFactory<>("average"));
    }
}