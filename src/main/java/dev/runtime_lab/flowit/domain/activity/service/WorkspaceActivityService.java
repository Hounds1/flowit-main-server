package dev.runtime_lab.flowit.domain.activity.service;

import dev.runtime_lab.flowit.domain.activity.dto.ActivityActorResponse;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityChangeResponse;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordDomain;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordListQuery;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordResponse;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityRecordTopic;
import dev.runtime_lab.flowit.domain.activity.dto.ActivityTargetResponse;
import dev.runtime_lab.flowit.domain.activity.entity.WorkspaceActivityRecord;
import dev.runtime_lab.flowit.domain.activity.repository.WorkspaceActivityRecordRepository;
import dev.runtime_lab.flowit.domain.workspace.service.internal.WorkspaceAccessService;
import dev.runtime_lab.flowit.global.security.authentication.CurrentUser;
import dev.runtime_lab.flowit.global.web.response.ApiListData;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class WorkspaceActivityService {

	private final WorkspaceAccessService workspaceAccessService;
	private final WorkspaceActivityRecordRepository workspaceActivityRecordRepository;
	private final JsonMapper jsonMapper;
	private final Clock clock;

	@Transactional(readOnly = true)
	public ApiListData<ActivityRecordResponse> activityRecords(
		CurrentUser currentUser,
		Long workspaceId,
		ActivityRecordListQuery query
	) {
		workspaceAccessService.resolveMemberAccess(currentUser, workspaceId);

		ActivityRecordTopic topic = query.topicOrDefault();
		ActivityRecordDomain domain = topic.domain();

		Long occurredFrom = occurredFrom(query.rangeDaysOrDefault());

		List<ActivityRecordResponse> records = workspaceActivityRecordRepository
			.findByWorkspaceId(workspaceId, domain, occurredFrom)
			.stream()
			.map(this::activityRecord)
			.toList();

		return ApiListData.of(records, records.size());
	}

	private ActivityRecordResponse activityRecord(WorkspaceActivityRecord record) {
		List<ActivityChangeResponse> changes = readChanges(record.getChangesJson());

		return new ActivityRecordResponse(
			record.getId(),
			record.getOccurredAt(),
			record.getDomain(),
			new ActivityActorResponse(
				record.getActorWorkspaceMember() == null ? null : record.getActorWorkspaceMember().getId(),
				record.getActorUser() == null ? null : record.getActorUser().getId(),
				record.getActorDisplayNameSnapshot()
			),
			new ActivityTargetResponse(record.getTargetType(), record.getTargetId(), record.getTargetDisplayNameSnapshot()),
			record.getAction(),
			changes.size(),
			changes.stream()
				.map(ActivityChangeResponse::element)
				.distinct()
				.toList(),
			changes
		);
	}

	private List<ActivityChangeResponse> readChanges(String changesJson) {
		try {
			return jsonMapper.readValue(changesJson, new TypeReference<>() {
			});
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Failed to deserialize workspace activity changes.", exception);
		}
	}

	private Long occurredFrom(Integer rangeDays) {
		return Instant.now(clock)
			.minus(rangeDays, ChronoUnit.DAYS)
			.getEpochSecond();
	}
}
