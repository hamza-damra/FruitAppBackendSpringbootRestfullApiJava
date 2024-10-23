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
@Table(indexes = {
        @Index(name = "idx_otp", columnList = "otp"),
        @Index(name = "idx_expiration_time", columnList = "expirationTime"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
public class AccountVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer verificationId;

    @Column(nullable = false)
    private Integer otp;

    @Column(nullable = false)
    private Date expirationTime;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
