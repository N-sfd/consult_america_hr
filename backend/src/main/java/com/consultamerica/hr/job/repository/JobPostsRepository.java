package com.consultamerica.hr.job.repository;

import com.consultamerica.hr.job.entity.JobPosts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostsRepository extends JpaRepository<JobPosts, Long> {
}
