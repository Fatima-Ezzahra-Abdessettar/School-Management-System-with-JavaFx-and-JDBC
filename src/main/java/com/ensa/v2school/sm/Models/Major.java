package com.ensa.v2school.sm.Models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Major {
    private int id;
    private String majorName;
    private String description;
    @Override
    public String toString() {
        return majorName;
    }
}
