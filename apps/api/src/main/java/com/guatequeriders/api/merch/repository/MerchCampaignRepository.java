package com.guatequeriders.api.merch.repository;

import com.guatequeriders.api.merch.model.CampaignStatus;
import com.guatequeriders.api.merch.model.MerchCampaign;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MerchCampaignRepository extends JpaRepository<MerchCampaign, Long> {

    Optional<MerchCampaign> findTopByOrderByIdDesc();

    List<MerchCampaign> findAllByOrderByIdDesc();

    boolean existsByStatus(CampaignStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select campaign from MerchCampaign campaign where campaign.id = :id")
    Optional<MerchCampaign> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select campaign from MerchCampaign campaign where campaign.status = :status")
    Optional<MerchCampaign> findByStatusForUpdate(@Param("status") CampaignStatus status);
}
