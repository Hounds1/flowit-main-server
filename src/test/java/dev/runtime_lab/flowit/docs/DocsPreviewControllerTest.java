package dev.runtime_lab.flowit.docs;

import static dev.runtime_lab.flowit.docs.support.DocumentedTypes.booleanParameter;
import static dev.runtime_lab.flowit.docs.support.DocumentedTypes.numberParameter;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocsPreviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class DocsPreviewControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createTask() throws Exception {
		String requestBody = """
			{
			  "title": "API 문서 미리보기",
			  "description": "현대적인 REST Docs 스니펫을 확인하기 위한 샘플 작업입니다.",
			  "priority": "HIGH",
			  "assigneeEmail": "owner@example.com"
			}
			""";

		this.mockMvc.perform(post("/api/docs-preview/projects/{projectId}/tasks", 42L)
				.queryParam("notify", "true")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(requestBody))
			.andExpect(status().isCreated())
			.andExpect(header().string(HttpHeaders.LOCATION, "/api/docs-preview/projects/42/tasks/1001"))
			.andDo(document("docs-preview-create-task",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				pathParameters(
					numberParameter("projectId").description("작업이 속한 프로젝트 식별자입니다.")
				),
				queryParameters(
					booleanParameter("notify").description("작업 알림 발송 여부입니다.")
				),
				requestHeaders(
					headerWithName(HttpHeaders.CONTENT_TYPE).description("요청 본문의 미디어 타입입니다."),
					headerWithName(HttpHeaders.ACCEPT).description("클라이언트가 기대하는 응답 미디어 타입입니다.").optional()
				),
				responseHeaders(
					headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 본문의 미디어 타입입니다."),
					headerWithName(HttpHeaders.LOCATION).description("생성된 작업 리소스의 URI입니다.")
				),
				requestFields(
					fieldWithPath("title").type(JsonFieldType.STRING).description("작업 제목입니다."),
					fieldWithPath("description").type(JsonFieldType.STRING).description("작업 상세 설명입니다.").optional(),
					fieldWithPath("priority").type(JsonFieldType.STRING).description("작업 우선순위 코드입니다."),
					fieldWithPath("assigneeEmail").type(JsonFieldType.STRING).description("담당자 이메일 주소입니다.")
						.optional()
				),
				responseFields(
					fieldWithPath("id").type(JsonFieldType.NUMBER).description("생성된 작업 식별자입니다."),
					fieldWithPath("projectId").type(JsonFieldType.NUMBER).description("작업이 속한 프로젝트 식별자입니다."),
					fieldWithPath("title").type(JsonFieldType.STRING).description("작업 제목입니다."),
					fieldWithPath("status").type(JsonFieldType.STRING).description("현재 작업 상태입니다."),
					fieldWithPath("priority").type(JsonFieldType.STRING).description("작업 우선순위 코드입니다."),
					fieldWithPath("notificationEnabled").type(JsonFieldType.BOOLEAN)
						.description("작업 알림 발송 요청 여부입니다."),
					fieldWithPath("assigneeEmail").type(JsonFieldType.STRING).description("담당자 이메일 주소입니다."),
					fieldWithPath("createdAt").type(JsonFieldType.NUMBER)
						.description("작업 생성 일시입니다. Unix epoch seconds 형식의 long 값입니다.")
				)
			));
	}
}
