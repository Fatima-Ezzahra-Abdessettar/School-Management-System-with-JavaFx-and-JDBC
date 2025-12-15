package com.ensa.v2school.sm.Models;


import lombok.*;

@AllArgsConstructor
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
}
