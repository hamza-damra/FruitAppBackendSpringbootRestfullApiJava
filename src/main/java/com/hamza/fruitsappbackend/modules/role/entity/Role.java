package com.hamza.fruitsappbackend.modules.role.entity;

import com.hamza.fruitsappbackend.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;
}