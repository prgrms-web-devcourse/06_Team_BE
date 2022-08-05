package com.devcourse.checkmoi.domain.study.service;

import com.devcourse.checkmoi.domain.study.converter.StudyConverter;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.Studies;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.StudyAppliers;
import com.devcourse.checkmoi.domain.study.dto.StudyResponse.StudyDetailWithMembers;
import com.devcourse.checkmoi.domain.study.repository.StudyRepository;
import com.devcourse.checkmoi.domain.study.service.validator.StudyServiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudyQueryServiceImpl implements StudyQueryService {

    private final StudyConverter studyConverter;

    private final StudyRepository studyRepository;

    private final StudyServiceValidator studyValidator;

    @Override
    public Studies getStudies(Long bookId, Pageable pageable) {
        return new Studies(
            studyRepository.findRecruitingStudyByBookId(bookId, pageable)
                .map(studyConverter::studyToStudyInfo)
        );
    }

    @Override
    public StudyDetailWithMembers getStudyInfoWithMembers(Long studyId) {
        return studyRepository.getStudyInfoWithMembers(studyId);
    }

    @Override
    public StudyAppliers getStudyAppliers(Long userId, Long studyId) {
        Long studyOwnerId = studyRepository.findStudyOwner(studyId);

        studyValidator.validateStudyOwner(userId, studyOwnerId,
            "스터디 신청 목록 조회 권한이 없습니다. 스터디 Id : " + studyId + "유저 Id : " + userId + " 스터디 장 Id : "
                + studyOwnerId
        );

        return studyRepository.getStudyAppliers(studyId);
    }

}