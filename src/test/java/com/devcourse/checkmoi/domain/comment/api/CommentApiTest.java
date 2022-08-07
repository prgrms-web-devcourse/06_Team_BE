package com.devcourse.checkmoi.domain.comment.api;

import static com.devcourse.checkmoi.domain.study.model.StudyStatus.IN_PROGRESS;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeBook;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makePostWithId;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeStudyWithId;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeUserWithId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.devcourse.checkmoi.domain.comment.dto.CommentRequest.Search;
import com.devcourse.checkmoi.domain.comment.dto.CommentResponse.CommentInfo;
import com.devcourse.checkmoi.domain.comment.service.CommentQueryService;
import com.devcourse.checkmoi.domain.post.model.Post;
import com.devcourse.checkmoi.domain.post.model.PostCategory;
import com.devcourse.checkmoi.domain.study.model.Study;
import com.devcourse.checkmoi.domain.token.dto.TokenResponse.TokenWithUserInfo;
import com.devcourse.checkmoi.domain.user.model.User;
import com.devcourse.checkmoi.template.IntegrationTest;
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class CommentApiTest extends IntegrationTest {

    @MockBean
    private CommentQueryService commentQueryService;

    @Nested
    @DisplayName("댓글 목록 조회 #130")
    class FindAllComments {

        @Test
        @DisplayName("S 검색조건(ex- 포스트ID)에 따라 작성된 댓글을 조회할 수 있다")
        void findAllComments() throws Exception {
            TokenWithUserInfo givenUser = getTokenWithUserInfo();
            User writer = makeUserWithId(1L);
            Study study = makeStudyWithId(makeBook(), IN_PROGRESS, 2L);
            Post post = makePostWithId(PostCategory.GENERAL, study, writer, 1L);
            List<CommentInfo> response = List.of(
                makeCommentInfoWithId(writer, post, 1L),
                makeCommentInfoWithId(writer, post, 2L),
                makeCommentInfoWithId(writer, post, 3L)
            );

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("postId", String.valueOf(1L));

            when(commentQueryService.findAllComments(anyLong(), any(Search.class)))
                .thenReturn(response);

            mockMvc.perform(get("/api/comments")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + givenUser.accessToken())
                    .params(params))
                .andExpect(status().isOk())
                .andDo(documentation());
        }

        private RestDocumentationResultHandler documentation() {
            return MockMvcRestDocumentationWrapper.document("find-comments",
                ResourceSnippetParameters.builder()
                    .tag("Comment API")
                    .summary("댓글 검색")
                    .description("댓글 검색에 사용되는 API")
                    .requestSchema(Schema.schema("댓글 검색 요청"))
                    .responseSchema(Schema.schema("댓글 검색 응답")),
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                tokenRequestHeader(),
                requestParameters(
                    parameterWithName("postId").description("게시글 아이디").optional()
                ),
                responseFields(
                    fieldWithPath("data[].id").type(JsonFieldType.NUMBER)
                        .description("댓글 아이디"),
                    fieldWithPath("data[].userId").type(JsonFieldType.NUMBER)
                        .description("댓글 작성자 아이디"),
                    fieldWithPath("data[].postId").type(JsonFieldType.NUMBER)
                        .description("게시글 아이디"),
                    fieldWithPath("data[].content").type(JsonFieldType.STRING)
                        .description("댓글 본문"),
                    fieldWithPath("data[].createdAt").type(JsonFieldType.STRING)
                        .description("댓글 작성일자"),
                    fieldWithPath("data[].updatedAt").type(JsonFieldType.STRING)
                        .description("댓글 수정일자")
                )
            );
        }

        private CommentInfo makeCommentInfoWithId(User user, Post post, Long commentId) {
            return CommentInfo.builder()
                .id(commentId)
                .userId(user.getId())
                .postId(post.getId())
                .content("댓글 - " + UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        }
    }

}