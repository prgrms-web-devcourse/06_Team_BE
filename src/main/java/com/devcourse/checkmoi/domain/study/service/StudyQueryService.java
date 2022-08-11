package com.devcourse.checkmoi.domain.study.service;

import com.devcourse.checkmoi.domain.study.dto.StudyRequest.Search;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.ExpiredStudies;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.Studies;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.StudyAppliers;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.StudyDetailWithMembers;
import com.devcourse.checkmoi.domain.study.model.StudyStatus;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

public interface StudyQueryService {

    Studies getStudies(Long bookId, Pageable pageable);

    StudyDetailWithMembers getStudyInfoWithMembers(Long studyId);

    StudyAppliers getStudyAppliers(Long userId, Long studyId);

    Studies getParticipationStudies(Long userId);

    Studies getFinishedStudies(Long userId);

    Studies getOwnedStudies(Long userId);

    void ongoingStudy(Long studyId);

    void participateUser(Long studyId, Long userId);

    Studies findAllByCondition(Long userId, Search search, Pageable pageable);

    ExpiredStudies getAllExpiredStudies(LocalDate criteriaTime, StudyStatus toStatus);
}
