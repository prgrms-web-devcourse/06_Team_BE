package com.devcourse.checkmoi.domain.study.api;

import static com.devcourse.checkmoi.global.util.ApiUtil.generatedUri;
import com.devcourse.checkmoi.domain.study.dto.StudyRequest.Audit;
import com.devcourse.checkmoi.domain.study.dto.StudyRequest.Create;
import com.devcourse.checkmoi.domain.study.dto.StudyRequest.Edit;
import com.devcourse.checkmoi.domain.study.dto.StudyRequest.Search;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.MyStudies;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.Studies;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.StudyDetailWithMembers;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.StudyMembers;
import com.devcourse.checkmoi.domain.study.facade.StudyFacade;
import com.devcourse.checkmoi.domain.study.service.StudyCommandService;
import com.devcourse.checkmoi.domain.study.service.StudyQueryService;
import com.devcourse.checkmoi.global.model.SimplePage;
import com.devcourse.checkmoi.global.model.SuccessResponse;
import com.devcourse.checkmoi.global.security.jwt.JwtAuthentication;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudyApi {

    private final StudyCommandService studyCommandService;

    private final StudyQueryService studyQueryService;

    private final StudyFacade studyFacade;

    @GetMapping("/studies/{studyId}")
    public ResponseEntity<SuccessResponse<StudyDetailWithMembers>> getDetailInfo(
        @PathVariable Long studyId
    ) {
        return ResponseEntity.ok()
            .body(new SuccessResponse<>(studyQueryService.getStudyInfoWithMembers(studyId)));
    }


    @PostMapping("/studies")
    public ResponseEntity<SuccessResponse<Long>> createStudy(
        @Valid @RequestBody Create request,
        @AuthenticationPrincipal JwtAuthentication user) {
        Long studyId = studyFacade.createStudy(request, user.id());
        return ResponseEntity
            .created(generatedUri(studyId))
            .body(new SuccessResponse<>(studyId));
    }

    @PutMapping("/studies/{studyId}")
    public ResponseEntity<SuccessResponse<Long>> editStudyInfo(
        @PathVariable Long studyId,
        @Valid @RequestBody Edit request,
        @AuthenticationPrincipal JwtAuthentication user) {
        return ResponseEntity.ok(
            new SuccessResponse<>(studyCommandService.editStudyInfo(studyId, user.id(), request)));
    }


    @GetMapping("/studies/me")
    public ResponseEntity<SuccessResponse<MyStudies>> getMyStudies(
        @AuthenticationPrincipal JwtAuthentication user
    ) {
        MyStudies response = studyFacade.getMyStudies(user.id());

        return ResponseEntity.ok(
            new SuccessResponse<>(response)
        );
    }

    @GetMapping("/studies")
    public ResponseEntity<SuccessResponse<Studies>> getStudies(
        @RequestParam Long bookId,
        SimplePage simplePage
    ) {
        Studies response = studyFacade.getStudies(bookId, simplePage.pageRequest());
        return ResponseEntity.ok(new SuccessResponse<>(response));
    }

    @PutMapping("/studies/{studyId}/members")
    public ResponseEntity<SuccessResponse<Long>> requestStudyJoin(
        @PathVariable Long studyId,
        @AuthenticationPrincipal JwtAuthentication user
    ) {
        Long studyMemberId = studyFacade.requestStudyJoin(studyId, user.id());
        return ResponseEntity.ok()
            .body(new SuccessResponse<>(studyMemberId));
    }

    /********************************* StudyMember  ****************************************/

    @GetMapping("/studies/{studyId}/members")
    public ResponseEntity<SuccessResponse<StudyMembers>> getStudyAppliers(
        @PathVariable Long studyId,
        @AuthenticationPrincipal JwtAuthentication user
    ) {
        return ResponseEntity.ok()
            .body(new SuccessResponse<>(studyQueryService.getStudyAppliers(user.id(), studyId)));
    }

    @PutMapping("/studies/{studyId}/members/{memberId}")
    public ResponseEntity<Void> auditStudyParticipation(
        @PathVariable Long studyId,
        @PathVariable Long memberId,
        @AuthenticationPrincipal JwtAuthentication user,
        @Valid @RequestBody Audit request
    ) {
        studyCommandService.auditStudyParticipation(studyId, memberId, user.id(), request);
        return ResponseEntity
            .noContent()
            .build();
    }

    /********************************* API v2  ****************************************/

    @GetMapping("/v2/studies")
    public ResponseEntity<SuccessResponse<Studies>> getDetailInfo(
        @Valid Search search,
        SimplePage pageable
    ) {
        return ResponseEntity.ok()
            .body(new SuccessResponse<>(
                studyQueryService.findAllByCondition(search, pageable.pageRequest())));
    }

}
