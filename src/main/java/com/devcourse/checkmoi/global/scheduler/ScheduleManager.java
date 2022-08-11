package com.devcourse.checkmoi.global.scheduler;

import com.devcourse.checkmoi.domain.study.dto.StudyResponse.ExpiredStudies;
import com.devcourse.checkmoi.domain.study.model.StudyMemberStatus;
import com.devcourse.checkmoi.domain.study.model.StudyStatus;

public interface ScheduleManager {

    void updateStudyWithMembers(Long studyId, StudyStatus studyStatus,
        StudyMemberStatus memberStatus);

    ExpiredStudies getAllStudiesToBeProgressed();
}
