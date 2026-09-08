package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String caption;

    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(
            mappedBy = "post",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("position ASC")
    private List<PostMedia> media = new ArrayList<>();

    public PostStatus getOverallStatus() {
        if (media.isEmpty()) {
            return PostStatus.PROCESSING;
        }

        boolean hasProcessing = false;
        boolean hasFailed = false;

        for (PostMedia m : media) {
            if (m.getStatus() == PostStatus.PROCESSING) {
                hasProcessing = true;
            }
            if (m.getStatus() == PostStatus.FAILED) {
                hasFailed = true;
            }
        }

        if (hasProcessing) {
            return PostStatus.PROCESSING;
        } else if (hasFailed) {
            return PostStatus.FAILED;
        } else {
            return PostStatus.READY;
        }
    }
}
