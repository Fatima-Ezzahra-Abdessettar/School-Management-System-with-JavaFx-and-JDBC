package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.StudentRepository;
import com.ensa.v2school.sm.DAO.SubjectRepository;
import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Student;
import com.ensa.v2school.sm.Models.Subject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

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
    private StudentRepository studentRepository = new StudentRepository();
    private SubjectRepository subjectRepository = new SubjectRepository();

    public void setMajor(Major major) {
        this.major = major;
        loadMajorDetails();
        loadSubjects();
        loadStudents();
    }

    private void loadMajorDetails() {
        if (major != null) {
            majorTitleLabel.setText(major.getMajorName());
            majorNameLabel.setText("Name: " + major.getMajorName());
            majorDescLabel.setText("Description: " + (major.getDescription() != null ? major.getDescription() : "No description available"));
        }
    }

    private void loadSubjects() {
        try {
            List<Subject> subjects = subjectRepository.findByMajor(major.getId());
            subjectsTable.getItems().clear();
            subjectsTable.getItems().addAll(subjects);
            subjectCountLabel.setText("Total subjects: " + subjects.size());

            if (subjects.isEmpty()) {
                showInfo("No Subjects", "No subjects are currently assigned to this major.");
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
                showInfo("No Students", "No students are currently enrolled in this major.");
            }
        } catch (SQLException e) {
            System.err.println("Error loading students: " + e.getMessage());
            showAlert("Database Error", "Could not load students for this major!");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
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
        subjectIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        subjectNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        averageCol.setCellValueFactory(new PropertyValueFactory<>("average"));
    }
}