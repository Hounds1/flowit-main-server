package dev.runtime_lab.flowit.docs;

import dev.runtime_lab.flowit.global.web.response.ApiResponseBodyAdvice;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(RestDocumentationExtension.class)
class ApiResponseFormatDocsTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp(RestDocumentationContextProvider restDocumentation) {
		mockMvc = MockMvcBuilders
			.standaloneSetup(new ApiResponseFormatDocsController())
			.setControllerAdvice(new ApiResponseBodyAdvice())
			.apply(documentationConfiguration(restDocumentation))
			.build();
	}

	@Test
	void objectResponse() throws Exception {
		mockMvc.perform(get("/docs/examples/response/object")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("api-response-object",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				responseFields(
					fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 처리 성공 여부입니다."),
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 본문 데이터입니다."),
					fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("샘플 자원 식별자입니다."),
					fieldWithPath("data.name").type(JsonFieldType.STRING).description("샘플 자원 이름입니다."),
					fieldWithPath("data.status").type(JsonFieldType.STRING).description("샘플 자원 상태입니다."),
					fieldWithPath("extensions").type(JsonFieldType.OBJECT).description("응답 보조 정보입니다.")
				)
			));
	}

	@Test
	void listResponse() throws Exception {
		mockMvc.perform(get("/docs/examples/response/list")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("api-response-list",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				responseFields(
					fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 처리 성공 여부입니다."),
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("목록 응답 데이터입니다."),
					fieldWithPath("data.items").type(JsonFieldType.ARRAY).description("응답 항목 목록입니다."),
					fieldWithPath("data.items[].id").type(JsonFieldType.NUMBER).description("샘플 자원 식별자입니다."),
					fieldWithPath("data.items[].name").type(JsonFieldType.STRING).description("샘플 자원 이름입니다."),
					fieldWithPath("data.totalCount").type(JsonFieldType.NUMBER).description("응답 항목 개수입니다."),
					fieldWithPath("extensions").type(JsonFieldType.OBJECT).description("응답 보조 정보입니다.")
				)
			));
	}

	@Test
	void emptyResponse() throws Exception {
		mockMvc.perform(get("/docs/examples/response/empty")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("api-response-empty",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				responseFields(
					fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 처리 성공 여부입니다."),
					fieldWithPath("data").type(JsonFieldType.OBJECT).description("데이터가 없는 성공 응답입니다."),
					fieldWithPath("extensions").type(JsonFieldType.OBJECT).description("응답 보조 정보입니다.")
				)
			));
	}

	@RestController
	private static class ApiResponseFormatDocsController {

		@GetMapping("/docs/examples/response/object")
		SampleAsset objectResponse() {
			return new SampleAsset(1001L, "Sample Asset", "ACTIVE");
		}

		@GetMapping("/docs/examples/response/list")
		List<SampleListItem> listResponse() {
			return List.of(
				new SampleListItem(1001L, "Sample Asset"),
				new SampleListItem(1002L, "Another Asset")
			);
		}

		@GetMapping("/docs/examples/response/empty")
		Object emptyResponse() {
			return null;
		}
	}

	private record SampleAsset(Long id, String name, String status) {
	}

	private record SampleListItem(Long id, String name) {
	}
}
