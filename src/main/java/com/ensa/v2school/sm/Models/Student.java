package com.ensa.v2school.sm.Models;

import lombok.*;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class Student {
    private String id;
    private String firstName;
    private String lastName;
    private User user;
    private float average;
    private Major major;
    private DossierAdministratif dossierAdministratif;

    // Constructor without DossierAdministratif
    public Student(String id, String firstName, String lastName, User user, float average, Major major) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.user = user;
        this.average = average;
        this.major = major;
        this.dossierAdministratif = null;
    }

    // Constructor with DossierAdministratif
    public Student(String id, String firstName, String lastName, User user, float average, Major major, DossierAdministratif dossierAdministratif) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.user = user;
        this.average = average;
        this.major = major;
        this.dossierAdministratif = dossierAdministratif;
    }
}