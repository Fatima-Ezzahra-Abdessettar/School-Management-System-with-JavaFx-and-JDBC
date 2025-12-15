package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.DAO.StudentRepository;
import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Student;
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

public class StudentFormController implements Initializable {

    @FXML private TextField idField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField averageField;
    @FXML private ComboBox<Major> majorBox;

    private Student student;

    public void setStudent(Student student) {
        this.student = student;

        if (student != null) {
            idField.setText(student.getId());
            idField.setDisable(true);
            firstNameField.setText(student.getFirstName());
            lastNameField.setText(student.getLastName());
            averageField.setText(String.valueOf(student.getAverage()));
            majorBox.setValue(student.getMajor());
        }
    }

    @FXML
    private void handleSave() {

        if (idField.getText().isEmpty() ||
                firstNameField.getText().isEmpty() ||
                lastNameField.getText().isEmpty() ||
                averageField.getText().isEmpty() ||
                majorBox.getValue() == null) {

            showAlert("Error", "Please fill all the fields");
            return;
        }

        float avg;
        try {
            avg = Float.parseFloat(averageField.getText());
            if (avg <= 0 || avg > 20) {
                showAlert("Error", "Please enter a number between 0 and 20 !");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Average must be a number");
            return;
        }

        StudentRepository repo = new StudentRepository();

        try {
            if (student != null) {
                student.setFirstName(firstNameField.getText());
                student.setLastName(lastNameField.getText());
                student.setAverage(avg);
                student.setMajor(majorBox.getValue());
                repo.update(student);
                showAlert("Success", "Student modified successfully");
            } else {
                student = new Student();
                student.setId(idField.getText());
                student.setFirstName(firstNameField.getText());
                student.setLastName(lastNameField.getText());
                student.setAverage(avg);
                student.setMajor(majorBox.getValue());
                repo.create(student);
                showAlert("Success", "Student created successfully");
            }
            close();
        } catch (SQLException e) {
            showAlert("Error", "An error occurred");
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
        ((Stage) idField.getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MajorRepository majorRepository = new MajorRepository();
        try{
            List<Major> majorList = majorRepository.getAll();
            majorBox.getItems().addAll(majorList);
        }catch (SQLException e){
            showAlert("Error", "An error occurred");
            System.err.println(e.getMessage());
        }
    }
}

