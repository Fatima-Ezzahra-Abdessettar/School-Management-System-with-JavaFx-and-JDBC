package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.DAO.SubjectRepository;
import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Subject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SubjectFormController implements Initializable {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private ListView<Major> majorListView;

    private Subject subject;
    private SubjectRepository subjectRepository = new SubjectRepository();
    private MajorRepository majorRepository = new MajorRepository();

    // Map to track which majors are selected
    private Map<Major, javafx.beans.property.BooleanProperty> majorSelectionMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("SubjectFormController initialized!");
        System.out.println("ListView: " + (majorListView != null ? "OK" : "NULL"));

        // Configure ListView with checkboxes
        if (majorListView != null) {
            setupCheckBoxListView();
            loadAllMajors();
        } else {
            System.err.println("ERROR: majorListView is NULL!");
        }
    }

    private void setupCheckBoxListView() {
        majorListView.setCellFactory(CheckBoxListCell.forListView(major -> {
            javafx.beans.property.BooleanProperty observable = new javafx.beans.property.SimpleBooleanProperty();
            majorSelectionMap.put(major, observable);
            return observable;
        }));
    }

    public void setSubject(Subject subject) {
        this.subject = subject;

        if (subject != null) {
            // Editing existing subject
            idField.setText(String.valueOf(subject.getId()));
            idField.setDisable(true);
            idField.setVisible(true);
            idField.setManaged(true);

            nameField.setText(subject.getName());

            // Select the majors already associated with the subject
            if (subject.getMajors() != null && !subject.getMajors().isEmpty()) {
                javafx.application.Platform.runLater(() -> {
                    for (Major selectedMajor : subject.getMajors()) {
                        for (Map.Entry<Major, javafx.beans.property.BooleanProperty> entry : majorSelectionMap.entrySet()) {
                            if (entry.getKey().getId() == selectedMajor.getId()) {
                                entry.getValue().set(true);
                                break;
                            }
                        }
                    }
                });
            }
        } else {
            // Adding new subject - hide ID field
            idField.setVisible(false);
            idField.setManaged(false);
        }
    }

    @FXML
    private void handleSave() {
        // Validate input
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter a subject name!");
            return;
        }

        // Get selected majors from checkbox map
        List<Major> selectedMajors = new ArrayList<>();
        for (Map.Entry<Major, javafx.beans.property.BooleanProperty> entry : majorSelectionMap.entrySet()) {
            if (entry.getValue().get()) {
                selectedMajors.add(entry.getKey());
            }
        }

        if (selectedMajors.isEmpty()) {
            showAlert("Error", "Please select at least one major!");
            return;
        }

        try {
            if (subject != null) {
                // Update existing subject
                subject.setName(nameField.getText().trim());
                subject.setMajors(selectedMajors);
                subjectRepository.update(subject);
                showSuccessAlert("Subject modified successfully");
            } else {
                // Create new subject
                subject = new Subject();
                subject.setName(nameField.getText().trim());
                subject.setMajors(selectedMajors);
                subjectRepository.create(subject);
                showSuccessAlert("Subject created successfully");
            }
            close();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void loadAllMajors() {
        try {
            List<Major> majors = majorRepository.getAll();
            majorListView.getItems().clear();
            majorSelectionMap.clear();
            majorListView.getItems().addAll(majors);
            System.out.println("Loaded " + majors.size() + " majors into ListView");
        } catch (SQLException e) {
            System.err.println("Error loading majors: " + e.getMessage());
            showAlert("Error", "Could not load majors: " + e.getMessage());
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
}