package com.ensa.v2school.sm.Controllers;

import com.ensa.v2school.sm.DAO.MajorRepository;
import com.ensa.v2school.sm.DAO.StudentRepository;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.sql.SQLException;
import java.util.Map;

public class StatsController {

    public BarChart avgPerMajorChart;
    @FXML
    private Label studentsCountLbl;
    @FXML private Label majorsCountLbl;
    @FXML private Label avgLbl;

    @FXML
    public void initialize() {
        MajorRepository majorRepository = new MajorRepository();
        StudentRepository studentRepository = new StudentRepository();
        try{
            int StudentsCount = (int)studentRepository.getCount();
            int MajorsCount = (int)majorRepository.getCount();
            Float Avg = studentRepository.getAverage();
            studentsCountLbl.setText(StudentsCount + "");
            majorsCountLbl.setText(MajorsCount + "");
            avgLbl.setText(Avg + "/20");
            Map<String, Float> data = studentRepository.getAverageByMajor();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Average per Major");

            data.forEach((major, avg) ->
                    series.getData().add(new XYChart.Data<>(major, avg))
            );
            avgPerMajorChart.getData().clear();
            avgPerMajorChart.getData().add(series);
        }catch(SQLException e){
            System.out.println("SQL Error"+e.getMessage());
        }
    }


}

