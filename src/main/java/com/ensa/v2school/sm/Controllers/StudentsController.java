package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.DAO.StudentRepository;
import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Student;
import com.ensa.v2school.sm.Models.User;
import javafx.beans.property.SimpleStringProperty;
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

public class StudentsController  implements Initializable {

    @FXML
    public Button editBtn;

    @FXML
    public Button deleteBtn;

    @FXML
    private Button addBtn;

    @FXML
    private TextField searchId;

    @FXML
    private Button searchBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private ComboBox<Major> filterMajorBtn;

    @FXML
    private TableView<Student> TableView;

    @FXML
    private TableColumn<Student , String > MajorCol;

    @FXML
    private TableColumn<Student, Float> averageCol;

    @FXML
    private TableColumn<Student, String> firstNameCol;

    @FXML
    private TableColumn<Student, String> idCol;

    @FXML
    private TableColumn<Student, String> lastNameCol;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Controller initialized!");
        System.out.println("TableView: " + (TableView != null ? "OK" : "NULL"));
        System.out.println("StudentRepository: " + (studentRepository != null ? "OK" : "NULL"));
        // Set up the columns to match Student properties
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        averageCol.setCellValueFactory(new PropertyValueFactory<>("average"));

        MajorCol.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            if (student.getMajor() != null) {
                return new SimpleStringProperty(student.getMajor().getMajorName());
            }
            return new SimpleStringProperty("N/A");
        });
        /* How the selected value is displayed */
        filterMajorBtn.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Major item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Filter by major");
                } else {
                    setText(item.getMajorName());
                }
            }
        });
        // loading the majors list
        MajorRepository majorRepository = new MajorRepository();
        try {
            List<Major> majors = majorRepository.getAll();
            filterMajorBtn.getItems().addAll(majors);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        // Load data
        loadTableView();


    }

    StudentRepository studentRepository = new StudentRepository();

    public void handleSearch(){
        String id = searchId.getText();
        if (id == null || id.trim().isEmpty()) {
            showAlert("Error", "Empty Search", "Please enter a student ID!");
            return;
        }
        try{
            Optional<Student> std = studentRepository.get(id);
            if(std.isPresent()){
                TableView.getItems().clear();
                TableView.getItems().addAll(std.get());
            }
            else {
                showAlert("Error", "Student not found", "Student ID: " + id + "is not found, please make sure you entered a correct id !");
            }
        }catch(SQLException e){
            System.err.println("Error searching student: " + e.getMessage());
            showAlert("Database Error", "Error", "Could not search for student!");
        }
    }
    public void handleCancelSearch(){
        searchId.clear();
        filterMajorBtn.getSelectionModel().clearSelection();
        filterMajorBtn.setValue(null);
        loadTableView();
    }

    private void showAlert(String title, String header, String body) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(body);
        alert.showAndWait();
    }

    public void loadTableView(){
        studentRepository = new StudentRepository();
        try{
            List<Student> stds = studentRepository.getAll();
            if (!stds.isEmpty()) {
                TableView.getItems().clear();
                TableView.getItems().addAll(stds);
            }
        }
        catch(SQLException e){
            System.err.println(e.getMessage());
        }
    }

    public void loadMajorTableView(){
        Major major = filterMajorBtn.getSelectionModel().getSelectedItem();
        int majorId = major.getId();
        try{
            List<Student> stds = studentRepository.findByMajor(majorId);
            TableView.getItems().clear();
            TableView.getItems().addAll(stds);
           if(stds.isEmpty()) {
               showAlert("Error", "Major not populated...", "no students are pursuing this major!");
           }
        }catch(SQLException e){
            System.err.println("Error searching students with the major: " + e.getMessage());
            showAlert("Database Error", "Error", "Could not search for student!");
        }
    }
    @FXML
    public void handleAdd() throws IOException {
        openStudentForm(null);
    }

    @FXML
    public void handleEdit() throws IOException {
        Student selected = TableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Selection", "Select a student first", "");
            return;
        }

        openStudentForm(selected);
    }
    private void openStudentForm(Student student) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/ensa/v2school/sm/student-form.fxml")
        );

        Parent root = loader.load();

        StudentFormController controller = loader.getController();
        controller.setStudent(student); // null = add, not null = edit

        Stage stage = new Stage();
        stage.setTitle(student == null ? "Add Student" : "Edit Student");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        loadTableView(); // refresh after close
    }
    public void handleDelete(){
        Student selected = TableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select a student first", "");
        }
        else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Student");
            alert.setHeaderText("Delete Student");
            alert.setContentText("Are you sure you want to delete this student?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                StudentRepository studentRepository = new StudentRepository();
                try {
                    studentRepository.delete(selected);
                    showAlert("Success", "Student successfully deleted!", "Student successfully deleted!");
                    loadTableView();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                showAlert("Cancel delete", "Delete student cancelled", "Student will no longer be deleted !");
            }
        }
    }





}
