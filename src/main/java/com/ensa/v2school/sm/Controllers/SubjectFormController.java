package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.DAO.SubjectRepository;
import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Subject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class SubjectFormController implements Initializable {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private ComboBox<Major> majorComboBox;

    private Subject subject;

    public void setSubject(Subject subject) {
        this.subject = subject;

        if (subject != null) {
            // Editing existing subject
            idField.setText(String.valueOf(subject.getId()));
            idField.setDisable(true);
            idField.setVisible(true);
            idField.setManaged(true);

            nameField.setText(subject.getName());
            majorComboBox.setValue(subject.getMajor());
        } else {
            // Adding new subject - hide ID field
            idField.setVisible(false);
            idField.setManaged(false);
        }
    }

    @FXML
    private void handleSave() {

        if (nameField.getText().isEmpty() || majorComboBox.getValue() == null) {
            showAlert("Error", "Please fill all fields!");
            return;
        }

        SubjectRepository repo = new SubjectRepository();

        try {
            if (subject != null) {
                // Update existing subject
                subject.setName(nameField.getText().trim());
                subject.setMajor(majorComboBox.getValue());
                repo.update(subject);
                showAlert("Success", "Subject modified successfully");
            } else {
                // Create new subject
                subject = new Subject();
                subject.setName(nameField.getText().trim());
                subject.setMajor(majorComboBox.getValue());
                repo.create(subject);
                showAlert("Success", "Subject created successfully");
            }
            close();
        } catch (SQLException e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
            System.err.println(e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load majors into ComboBox
        MajorRepository majorRepository = new MajorRepository();
        try {
            List<Major> majors = majorRepository.getAll();
            majorComboBox.getItems().addAll(majors);
        } catch (SQLException e) {
            showAlert("Error", "Could not load majors: " + e.getMessage());
            System.err.println(e.getMessage());
        }
    }
}