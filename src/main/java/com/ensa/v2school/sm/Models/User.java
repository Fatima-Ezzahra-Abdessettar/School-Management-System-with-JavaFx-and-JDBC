package com.ensa.v2school.sm.Models;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class User {
    private int id;
    private String userName;
    private String password;
    private ROLE role;

    public boolean isStudent() {
        return role == ROLE.Student;
    }
    public boolean isAdmin() {
        return role == ROLE.ADMIN;
    }


}
