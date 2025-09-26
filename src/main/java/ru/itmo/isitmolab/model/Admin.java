package ru.itmo.isitmolab.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "pass_hash", nullable = false)
    private String passHash;

    private String salt;

    @Column(name = "creation_time", nullable = false, updatable = false)
    private LocalDateTime creationDateTime;

    @PrePersist
    void onCreate() {
        if (creationDateTime == null) {
            creationDateTime = LocalDateTime.now();
        }
    }

}
