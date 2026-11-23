
package com.flowdesk.repository;

import com.flowdesk.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findTop10ByOrganization_IdOrderByCreatedAtDesc(
        Long organizationId
    );
}
