package com.guatequeriders.api.collaborators.repository;

import com.guatequeriders.api.collaborators.model.Collaborator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {

    List<Collaborator> findAllByActiveTrueOrderByFeaturedDescCreatedAtDescIdAsc();

    List<Collaborator> findAllByOrderByFeaturedDescActiveDescCreatedAtDescIdAsc();
}
