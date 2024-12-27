package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;


@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @Positive
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Name can't be null.")
    @NotEmpty(message = "Name can't be empty.")
    @Length(max = 255)
    private String name;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    @NotNull(message = "User email can't be null.")
    @NotEmpty(message = "User email can't be empty.")
    @Email
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Incorrect email format.")
    private String email;
}