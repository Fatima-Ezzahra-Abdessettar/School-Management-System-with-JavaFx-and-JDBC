package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.DAO.StudentRepository;
import com.ensa.v2school.sm.Models.Major;
import com.ensa.v2school.sm.Models.Student;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;


import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class StudentsController implements Initializable {

    @FXML public Button editBtn;
    @FXML public Button deleteBtn;
    @FXML private Button addBtn;
    @FXML private TextField searchId;
    @FXML private Button searchBtn;
    @FXML private Button cancelBtn;
    @FXML private ComboBox<Major> filterMajorBtn;
    @FXML private TableView<Student> TableView;
    @FXML private TableColumn<Student, String> MajorCol;
    @FXML private TableColumn<Student, Float> averageCol;
    @FXML private TableColumn<Student, String> firstNameCol;
    @FXML private TableColumn<Student, String> idCol;
    @FXML private TableColumn<Student, String> lastNameCol;

    StudentRepository studentRepository = new StudentRepository();

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

        // Major column
        MajorCol.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            if (student.getMajor() != null) {
                return new SimpleStringProperty(student.getMajor().getMajorName());
            }
            return new SimpleStringProperty("N/A");
        });

        // Filter ComboBox display
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

        // Load majors list
        MajorRepository majorRepository = new MajorRepository();
        try {
            List<Major> majors = majorRepository.getAll();
            filterMajorBtn.getItems().addAll(majors);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        // Load data
        loadTableView();
        addDossierButtonColumn();

    }

    public void handleSearch() {
        String id = searchId.getText();
        if (id == null || id.trim().isEmpty()) {
            showAlert("Error", "Empty Search", "Please enter a student ID!");
            return;
        }
        try {
            Optional<Student> std = studentRepository.get(id);
            if (std.isPresent()) {
                TableView.getItems().clear();
                TableView.getItems().add(std.get());
            } else {
                showAlert("Error", "Student not found", "Student ID: " + id + " is not found!");
            }
        } catch (SQLException e) {
            System.err.println("Error searching student: " + e.getMessage());
            showAlert("Database Error", "Error", "Could not search for student!");
        }
    }

    public void handleCancelSearch() {
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

    public void loadTableView() {
        try {
            List<Student> stds = studentRepository.getAll();
            TableView.getItems().clear();
            if (!stds.isEmpty()) {
                TableView.getItems().addAll(stds);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void loadMajorTableView() {
        Major major = filterMajorBtn.getSelectionModel().getSelectedItem();
        if (major == null) {
            return;
        }
        try {
            List<Student> stds = studentRepository.findByMajor(major.getId());
            TableView.getItems().clear();
            TableView.getItems().addAll(stds);
            if (stds.isEmpty()) {
                showAlert("Info", "No students found", "No students are pursuing this major!");
            }
        } catch (SQLException e) {
            System.err.println("Error filtering students by major: " + e.getMessage());
            showAlert("Database Error", "Error", "Could not filter students!");
        }
    }

    @FXML
    public void handleAdd() {
        try {
            openStudentForm(null);
        } catch (IOException e) {
            System.err.println("Failed to open add form: " + e.getMessage());
        }
    }

    @FXML
    public void handleEdit() {
        Student selected = TableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Selection", "Select a student first", "");
            return;
        }

        try {
            openStudentForm(selected);
        } catch (IOException e) {
            System.err.println("Failed to open edit form: " + e.getMessage());
        }
    }

    private void openStudentForm(Student student) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/ensa/v2school/sm/student-form.fxml")
        );

        Parent root = loader.load();

        StudentFormController controller = loader.getController();
        controller.setStudent(student);

        Stage stage = new Stage();
        stage.setTitle(student == null ? "Add Student" : "Edit Student");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        loadTableView();
    }

    public void handleDelete() {
        Student selected = TableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Select a student first", "");
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Student");
            alert.setHeaderText("Delete Student");
            alert.setContentText("Are you sure you want to delete this student?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    studentRepository.delete(selected);
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText("Student successfully deleted!");
                    successAlert.showAndWait();
                    loadTableView();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    showAlert("Database Error", "Error", "Could not delete student!");
                }
            }
        }
    }
    private void addDossierButtonColumn() {

        TableColumn<Student, Void> dossierCol = new TableColumn<>("Dossier");

        dossierCol.setCellFactory(col -> new TableCell<>() {

            private final Button btn = new Button();

            {
                btn.setOnAction(e -> {
                    Student student = getTableView()
                            .getItems()
                            .get(getIndex());
                    openDossierForm(student);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Student student = getTableView()
                            .getItems()
                            .get(getIndex());

                    if (student.getDossierAdministratif() == null) {
                        btn.setText("Créer");
                        btn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                    } else {
                        btn.setText("Voir");
                        btn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");
                    }

                    setGraphic(btn);
                }
            }
        });

        TableView.getColumns().add(dossierCol);
    }

    private void openDossierForm(Student student) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/ensa/v2school/sm/dossier-form.fxml")
            );

            Parent root = loader.load();

            DossierFormController controller = loader.getController();
            controller.setStudent(student);

            Stage stage = new Stage();
            stage.setTitle("Dossier Administratif");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadTableView(); // refresh

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    // Add this method to your StudentsController class

    @FXML
    public void handleEnroll() {
        Student selected = TableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Selection", "Select a student first", "");
            return;
        }

        if (selected.getMajor() == null) {
            showAlert("Error", "Cannot Enroll", "This student has no major assigned. Please assign a major first.");
            return;
        }

        try {
            openEnrollForm(selected);
        } catch (IOException e) {
            System.err.println("Failed to open enroll form: " + e.getMessage());
            showAlert("Error", "Cannot open form", "Error: " + e.getMessage());
        }
    }

    private void openEnrollForm(Student student) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/ensa/v2school/sm/EnrollStudent.fxml")
        );

        Parent root = loader.load();

        EnrollFormController controller = loader.getController();
        controller.setStudent(student);

        Stage stage = new Stage();
        stage.setTitle("Enroll Student in Subjects");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

        loadTableView(); // Refresh after enrollment
    }



}