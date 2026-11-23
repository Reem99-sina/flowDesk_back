
package com.flowdesk.service;

import com.flowdesk.dto.activity.ActivityResponse;
import com.flowdesk.entity.Activity;
import com.flowdesk.entity.Organization;
import com.flowdesk.entity.User;
import com.flowdesk.enums.ActivityType;
import com.flowdesk.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;

    public void createActivity(
            User user,
            Organization organization,
            ActivityType type,
            String description,
            String targetName,
            String targetType,
            Long targetId
    ) {
        Activity activity = new Activity();

        activity.setUser(user);
        activity.setOrganization(organization);
        activity.setType(type);
        activity.setDescription(description);
        activity.setTargetName(targetName);
        activity.setTargetType(targetType);
        activity.setTargetId(targetId);

        activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getRecentActivities(Long organizationId) {

        return activityRepository
                .findTop10ByOrganization_IdOrderByCreatedAtDesc(organizationId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ActivityResponse mapToResponse(Activity activity) {

        return ActivityResponse.builder()
                .id(activity.getId())
                .userId(activity.getUser().getId())
                .userName(activity.getUser().getFullName())
                .type(activity.getType())
                .description(activity.getDescription())
                .targetName(activity.getTargetName())
                .targetType(activity.getTargetType())
                .targetId(activity.getTargetId())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
