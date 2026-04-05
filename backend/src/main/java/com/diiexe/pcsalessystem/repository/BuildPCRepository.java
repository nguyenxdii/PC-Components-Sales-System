package com.diiexe.pcsalessystem.repository;

import com.diiexe.pcsalessystem.entity.BuildPC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BuildPCRepository extends JpaRepository<BuildPC, Long> {
    List<BuildPC> findByUserId(Long userId);
}
