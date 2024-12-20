package com.hamza.fruitsappbackend.modules.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ResetEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resetEmailId;

    @Column(nullable = false)
    private Integer otp;

    @Column(nullable = false)
    private Date expirationTime;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;
}
