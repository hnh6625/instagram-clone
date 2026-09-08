package com.example.worker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Post {
    @Id
    private Long id;

    private String caption;

    @CreationTimestamp
    private LocalDateTime createdAt;
}