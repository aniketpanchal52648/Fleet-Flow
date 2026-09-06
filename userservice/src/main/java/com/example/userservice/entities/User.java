package com.example.userservice.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USERS")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "USER_ID", unique = true)
    private String userId;

    private String keycloakId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "PHONE_NO", unique = true)
    private String phoneNo;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER_ROLE;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserAddress> userAddressList = new ArrayList<>();

    public void addAddress(UserAddress address) {
        userAddressList.add(address);
        address.setUser(this);
    }

    public void removeAddress(UserAddress address) {
        userAddressList.remove(address);
        address.setUser(null);
    }
}
