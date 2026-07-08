package dev.runtime_lab.flowit.domain.workspace.service.internal;

import dev.runtime_lab.flowit.domain.workspace.dto.WorkspaceMemberResponse;
import dev.runtime_lab.flowit.domain.workspace.repository.WorkspaceMemberRepository;
import dev.runtime_lab.flowit.domain.workspace.repository.projection.WorkspaceMemberProfileProjection;
import dev.runtime_lab.flowit.domain.workspace.repository.projection.WorkspaceMembershipProjection;
import dev.runtime_lab.flowit.domain.workspace.service.internal.contract.WorkspaceMembershipSummary;
import dev.runtime_lab.flowit.global.stereotype.InternalService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@InternalService
@RequiredArgsConstructor
public class WorkspaceMembershipQueryService {

	private final WorkspaceMemberRepository workspaceMemberRepository;

	@Transactional(readOnly = true)
	public List<WorkspaceMembershipSummary> findActiveMembershipSummaries(Long userId) {
		return workspaceMemberRepository.findActiveMembershipsByUserId(userId)
			.stream()
			.map(this::summary)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<Long> findActiveMemberUserIds(Long workspaceId) {
		return workspaceMemberRepository.findActiveUserIdsByWorkspaceId(workspaceId);
	}

	@Transactional(readOnly = true)
	public Optional<Long> findMemberUserId(Long workspaceId, Long memberId) {
		return workspaceMemberRepository.findUserIdByWorkspaceIdAndMemberId(workspaceId, memberId);
	}

	@Transactional(readOnly = true)
	public Optional<String> findActiveProfileImageUrlByWorkspaceIdAndMemberId(Long workspaceId, Long memberId) {
		return workspaceMemberRepository.findActiveProfileByWorkspaceIdAndMemberId(workspaceId, memberId)
			.map(profile -> profileImageUrl(workspaceId, profile));
	}

	@Transactional(readOnly = true)
	public Optional<String> findActiveProfileImageUrlByWorkspaceIdAndUserId(Long workspaceId, Long userId) {
		return workspaceMemberRepository.findActiveProfileByWorkspaceIdAndUserId(workspaceId, userId)
			.map(profile -> profileImageUrl(workspaceId, profile));
	}

	private WorkspaceMembershipSummary summary(WorkspaceMembershipProjection projection) {
		return new WorkspaceMembershipSummary(
			projection.workspaceId(),
			projection.workspaceName(),
			projection.workspaceDescription(),
			projection.memberCount(),
			projection.role(),
			projection.joinedAt()
		);
	}

	private String profileImageUrl(Long workspaceId, WorkspaceMemberProfileProjection profile) {
		return WorkspaceMemberResponse.profileImageUrl(
			workspaceId,
			profile.memberId(),
			profile.profileImageFileId()
		);
	}
}
