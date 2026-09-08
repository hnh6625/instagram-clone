package com.example.worker.repository;

import com.example.worker.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostMediaRepository extends JpaRepository<PostMedia,Long> {
    
}
