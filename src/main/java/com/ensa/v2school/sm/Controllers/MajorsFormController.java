package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.Models.Major;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MajorsFormController implements Initializable {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;

    private Major major;

    public void setMajor(Major major) {
        this.major = major;

        if (major != null) {
            // Editing existing major
            idField.setText(String.valueOf(major.getId()));
            idField.setDisable(true);
            idField.setVisible(true);
            idField.setManaged(true);

            nameField.setText(major.getMajorName());
            descriptionField.setText(major.getDescription());
        } else {
            // Adding new major - hide ID field
            idField.setVisible(false);
            idField.setManaged(false);
        }
    }

    @FXML
    private void handleSave() {

        if (nameField.getText().isEmpty()) {
            showAlert("Error", "Please enter a major name!");
            return;
        }

        MajorRepository repo = new MajorRepository();

        try {
            if (major != null) {
                // Update existing major
                major.setMajorName(nameField.getText().trim());
                major.setDescription(descriptionField.getText().trim());
                repo.update(major);
                showAlert("Success", "Major modified successfully");
            } else {
                // Create new major
                major = new Major();
                major.setMajorName(nameField.getText().trim());
                major.setDescription(descriptionField.getText().trim());
                repo.create(major);
                showAlert("Success", "Major created successfully");
            }
            close();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showAlert("Error", "This major name already exists!");
            } else {
                showAlert("Error", "An error occurred: " + e.getMessage());
            }
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
        //
    }
}