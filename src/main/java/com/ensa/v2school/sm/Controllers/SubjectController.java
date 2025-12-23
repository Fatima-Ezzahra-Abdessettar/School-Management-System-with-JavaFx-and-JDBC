package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.DAO.SubjectRepository;
import com.ensa.v2school.sm.Models.Major;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SubjectController implements Initializable {

    @FXML private Button addBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private TextField searchId;
    @FXML private Button searchBtn;
    @FXML private Button cancelBtn;
    @FXML private ComboBox<Major> filterMajorBtn;
    @FXML private TableView<Subject> TableView;
    @FXML private TableColumn<Subject, Integer> idCol;
    @FXML private TableColumn<Subject, String> nameCol;
    @FXML private TableColumn<Subject, String> majorCol;

    private final SubjectRepository subjectRepository = new SubjectRepository();
    private final MajorRepository majorRepository = new MajorRepository();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("SubjectController initialized!");

        // Set up table columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        // For the majors column, we need a custom cell value factory
        majorCol.setCellValueFactory(cellData -> {
            Subject subject = cellData.getValue();
            if (subject.getMajors() != null && !subject.getMajors().isEmpty()) {
                String majorNames = subject.getMajors().stream()
                        .map(Major::getMajorName)
                        .collect(Collectors.joining(", "));
                return new javafx.beans.property.SimpleStringProperty(majorNames);
            }
            return new javafx.beans.property.SimpleStringProperty("No majors");
        });

        // Load data
        loadTableView();
        loadMajorFilter();
    }

    private void loadTableView() {
        try {
            List<Subject> subjects = subjectRepository.getAll();
            TableView.getItems().clear();
            TableView.getItems().addAll(subjects);
        } catch (SQLException e) {
            System.err.println("Error loading subjects: " + e.getMessage());
            showAlert("Database Error", "Error", "Could not load subjects!");
        }
    }

    private void loadMajorFilter() {
        try {
            List<Major> majors = majorRepository.getAll();
            filterMajorBtn.getItems().clear();
            filterMajorBtn.getItems().add(null); // Add "All" option
            filterMajorBtn.getItems().addAll(majors);

            // Set prompt text for null value
            filterMajorBtn.setPromptText("All Majors");
        } catch (SQLException e) {
            System.err.println("Error loading majors for filter: " + e.getMessage());
        }
    }

    @FXML
    public void loadMajorTableView() {
        Major selectedMajor = filterMajorBtn.getValue();

        try {
            if (selectedMajor == null) {
                // Show all subjects
                loadTableView();
            } else {
                // Filter by selected major
                List<Subject> subjects = subjectRepository.findByMajorId(selectedMajor.getId());
                TableView.getItems().clear();
                TableView.getItems().addAll(subjects);
            }
        } catch (SQLException e) {
            System.err.println("Error filtering subjects: " + e.getMessage());
            showAlert("Database Error", "Error", "Could not filter subjects!");
        }
    }

    @FXML
    public void handleSearch() {
        String id = searchId.getText();
        if (id == null || id.trim().isEmpty()) {
            showAlert("Error", "Empty Search", "Please enter a subject ID!");
            return;
        }

        try {
            Optional<Subject> subject = subjectRepository.get(Integer.parseInt(id));
            if (subject.isPresent()) {
                TableView.getItems().clear();
                TableView.getItems().add(subject.get());
            } else {
                showAlert("Error", "Subject not found", "Subject ID: " + id + " not found!");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid ID", "Please enter a valid numeric ID!");
        } catch (SQLException e) {
            System.err.println("Error searching subject: " + e.getMessage());
            showAlert("Database Error", "Error", "Could not search for subject!");
        }
    }

    @FXML
    public void handleCancelSearch() {
        searchId.clear();
        filterMajorBtn.setValue(null);
        loadTableView();
    }

    @FXML
    public void handleAdd() {
        try {
            openSubjectForm(null);
        } catch (IOException e) {
            System.err.println("Failed to open add form: " + e.getMessage());
            showAlert("Error", "Cannot open form", "Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleEdit() {
        Subject selected = TableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No Selection", "Select a subject first", "");
            return;
        }

        try {
            openSubjectForm(selected);
        } catch (IOException e) {
            System.err.println("Failed to open edit form: " + e.getMessage());
            showAlert("Error", "Cannot open form", "Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        Subject selected = TableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Error", "Select a subject first", "");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Subject");
        alert.setHeaderText("Delete Subject");
        alert.setContentText("Are you sure you want to delete this subject?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                subjectRepository.delete(selected);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Subject successfully deleted!");
                successAlert.showAndWait();
                loadTableView();
            } catch (SQLException e) {
                System.err.println("Error deleting subject: " + e.getMessage());
                showAlert("Database Error", "Error", "Could not delete subject!");
            }
        }
    }

    private void openSubjectForm(Subject subject) throws IOException {
        System.out.println("=== Opening Subject Form ===");

        String fxmlPath = "/com/ensa/v2school/sm/SubjectForm.fxml";
        System.out.println("Looking for FXML at: " + fxmlPath);

        try {
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);

            if (fxmlUrl == null) {
                System.err.println("ERROR: FXML file not found at path: " + fxmlPath);
                System.err.println("Check your resources folder structure!");
                showAlert("Error", "File Not Found", "SubjectForm.fxml not found in resources!");
                return;
            }

            System.out.println("FXML found at: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            System.out.println("FXML loaded successfully");

            SubjectFormController controller = loader.getController();
            controller.setSubject(subject);

            Stage stage = new Stage();
            stage.setTitle(subject == null ? "Add Subject" : "Edit Subject");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadTableView();
        } catch (IOException e) {
            System.err.println("ERROR loading subject form:");
            System.err.println("Message: " + e.getMessage());
            throw e;
        }
    }
    private void showAlert(String title, String header, String body) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(body);
        alert.showAndWait();
    }
}