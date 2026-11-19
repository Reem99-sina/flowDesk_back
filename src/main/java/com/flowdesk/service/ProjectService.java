
package com.flowdesk.service;

import com.flowdesk.dto.project.CreateProjectRequest;
import com.flowdesk.entity.Customer;
import com.flowdesk.entity.Organization;
import com.flowdesk.entity.Project;
import com.flowdesk.enums.ProjectStatus;
import com.flowdesk.repository.CustomerRepository;
import com.flowdesk.repository.OrganizationRepository;
import com.flowdesk.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.flowdesk.dto.project.ProjectResponse;
import com.flowdesk.dto.project.UpdateProjectRequest;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final CustomerRepository customerRepository;
    private ProjectResponse mapToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .customerId(
                        project.getCustomer() != null
                                ? project.getCustomer().getId()
                                : null
                )
                .customerName(
                        project.getCustomer() != null
                                ? project.getCustomer().getName()
                                : null
                )
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .dueDate(project.getDueDate())
                .budget(project.getBudget())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    @Transactional
    public ProjectResponse createProject(Long organizationId, CreateProjectRequest request) {

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        Customer customer = null;

        if (request.getCustomerId() != null) {
            customer = customerRepository
                    .findByIdAndOrganization_Id(request.getCustomerId(), organizationId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        }

        if (request.getStartDate() != null
                && request.getDueDate() != null
                && request.getDueDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Due date cannot be before start date");
        }

        Project project = new Project();

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCustomer(customer);
        project.setOrganization(organization);
        project.setStatus(
                request.getStatus() != null
                        ? request.getStatus()
                        : ProjectStatus.PLANNING
        );
        project.setStartDate(request.getStartDate());
        project.setDueDate(request.getDueDate());
        project.setBudget(request.getBudget());

        Project savedProject = projectRepository.save(project);

        return mapToResponse(savedProject);
    }
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(Long organizationId) {

        return projectRepository.findAllByOrganization_Id(organizationId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, Long organizationId) {

        Project project = projectRepository
                .findByIdAndOrganization_Id(projectId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        return mapToResponse(project);
    }

@Transactional
public ProjectResponse updateProject(
        Long projectId,
        Long organizationId,
        UpdateProjectRequest request
) {
    Project project = projectRepository
            .findByIdAndOrganization_Id(projectId, organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));

    if (request.getName() != null) {
        if (request.getName().isBlank()) {
            throw new IllegalArgumentException("Project name cannot be blank");
        }
        project.setName(request.getName());
    }

    if (request.getDescription() != null) {
        project.setDescription(request.getDescription());
    }

    if (request.getCustomerId() != null) {
        Customer customer = customerRepository
                .findByIdAndOrganization_Id(
                        request.getCustomerId(),
                        organizationId
                )
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        project.setCustomer(customer);
    }

    if (request.getStatus() != null) {
        project.setStatus(request.getStatus());
    }

    if (request.getStartDate() != null) {
        project.setStartDate(request.getStartDate());
    }

    if (request.getDueDate() != null) {
        project.setDueDate(request.getDueDate());
    }

    LocalDate startDate = project.getStartDate();
    LocalDate dueDate = project.getDueDate();

    if (startDate != null && dueDate != null
            && dueDate.isBefore(startDate)) {
        throw new IllegalArgumentException(
                "Due date cannot be before start date"
        );
    }

    if (request.getBudget() != null) {
        project.setBudget(request.getBudget());
    }

    Project updatedProject = projectRepository.save(project);

    return mapToResponse(updatedProject);
}
@Transactional
public void deleteProject(Long projectId, Long organizationId) {

    Project project = projectRepository
            .findByIdAndOrganization_Id(projectId, organizationId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));

    projectRepository.delete(project);
}
@Transactional(readOnly = true)
public Page<ProjectResponse> searchProjects(
        Long organizationId,
        String search,
        Pageable pageable
) {
    String searchTerm = search == null ? "" : search.trim();

    return projectRepository
            .searchProjects(organizationId, searchTerm, pageable)
            .map(this::mapToResponse);
}
}
