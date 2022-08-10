package com.devcourse.checkmoi.domain.study.service;

import com.devcourse.checkmoi.domain.study.dto.StudyRequest.Audit;
import com.devcourse.checkmoi.domain.study.dto.StudyRequest.Create;
import com.devcourse.checkmoi.domain.study.dto.StudyRequest.Edit;

public interface StudyCommandService {

    Long createStudy(Create request, Long userId);

    Long editStudyInfo(Long studyId, Long userId, Edit request);

    void auditStudyParticipation(Long studyId, Long memberId, Long userId, Audit request);

    Long requestStudyJoin(Long studyId, Long userId);
}
