package com.devcourse.checkmoi.domain.study.service;

import com.devcourse.checkmoi.domain.study.dto.StudyResponse.Studies;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.StudyAppliers;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.StudyDetailWithMembers;
import org.springframework.data.domain.Pageable;

public interface StudyQueryService {

    Studies getStudies(Long bookId, Pageable pageable);

    StudyDetailWithMembers getStudyInfoWithMembers(Long studyId);

    StudyAppliers getStudyAppliers(Long userId, Long studyId);

    Studies getParticipationStudies(Long userId);

    Studies getFinishedStudies(Long userId);

    Studies getOwnedStudies(Long userId);

    void ongoingStudy(Long studyId);

    void participateUser(Long aLong, Long userId);
}
