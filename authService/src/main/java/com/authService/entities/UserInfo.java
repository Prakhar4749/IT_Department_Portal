package com.authService.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name="UserLoginDetails")
public class UserInfo {
    private String name;
    @Id
    private String email;
    private String password;
    private String roles;


}
