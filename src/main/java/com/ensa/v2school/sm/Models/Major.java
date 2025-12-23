package com.ensa.v2school.sm.Models;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Major {
    private int id;
    private String majorName;
    private String description;
    private List<Subject> subjects = new ArrayList<>();
    @Override
    public String toString() {
        return majorName;
    }
}
