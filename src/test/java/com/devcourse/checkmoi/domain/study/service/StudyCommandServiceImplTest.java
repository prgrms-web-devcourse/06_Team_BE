package com.devcourse.checkmoi.domain.study.service;

import static com.devcourse.checkmoi.domain.study.model.StudyMemberStatus.OWNED;
import static com.devcourse.checkmoi.domain.study.model.StudyMemberStatus.PENDING;
import static com.devcourse.checkmoi.domain.study.model.StudyStatus.IN_PROGRESS;
import static com.devcourse.checkmoi.domain.study.model.StudyStatus.RECRUITING;
import static com.devcourse.checkmoi.global.exception.error.ErrorMessage.ACCESS_DENIED;
import static com.devcourse.checkmoi.global.exception.error.ErrorMessage.STUDY_JOIN_REQUEST_DUPLICATE;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeBookWithId;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeStudyMember;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeStudyMemberWithId;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeStudyWithId;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeUserWithId;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import com.devcourse.checkmoi.domain.book.model.Book;
import com.devcourse.checkmoi.domain.study.converter.StudyConverter;
import com.devcourse.checkmoi.domain.study.dto.StudyRequest;
import com.devcourse.checkmoi.domain.study.exception.DuplicateStudyJoinRequestException;
import com.devcourse.checkmoi.domain.study.exception.NotRecruitingStudyException;
import com.devcourse.checkmoi.domain.study.exception.NotStudyOwnerException;
import com.devcourse.checkmoi.domain.study.exception.StudyJoinMaximumReachedException;
import com.devcourse.checkmoi.domain.study.exception.StudyJoinRequestNotFoundException;
import com.devcourse.checkmoi.domain.study.exception.StudyMemberFullException;
import com.devcourse.checkmoi.domain.study.exception.StudyNotFoundException;
import com.devcourse.checkmoi.domain.study.model.Study;
import com.devcourse.checkmoi.domain.study.model.StudyMember;
import com.devcourse.checkmoi.domain.study.model.StudyMemberStatus;
import com.devcourse.checkmoi.domain.study.model.StudyStatus;
import com.devcourse.checkmoi.domain.study.repository.StudyMemberRepository;
import com.devcourse.checkmoi.domain.study.repository.StudyRepository;
import com.devcourse.checkmoi.domain.study.service.validator.StudyValidator;
import com.devcourse.checkmoi.domain.user.model.User;
import com.devcourse.checkmoi.domain.user.repository.UserRepository;
import com.devcourse.checkmoi.global.exception.error.ErrorMessage;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyCommandServiceImplTest {

    @InjectMocks
    StudyCommandServiceImpl studyCommandService;

    @Mock
    StudyConverter studyConverter;

    @Mock
    StudyRepository studyRepository;

    @Mock
    StudyMemberRepository studyMemberRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    StudyValidator studyValidator;

    @Nested
    @DisplayName("스터디 등록 #5")
    class CreateTest {

        @Test
        @DisplayName("S 스터디를 등록할 수 있다")
        void createStudy() {
            StudyRequest.Create request = StudyRequest.Create.builder()
                .bookId(1L)
                .name("스터디 이름")
                .thumbnail("스터디 썸네일 URL")
                .description("스터디입니다")
                .maxParticipant(5)
                .gatherStartDate(LocalDate.now())
                .gatherEndDate(LocalDate.now())
                .build();
            Long userId = 1L;
            User user = makeUserWithId(1L);
            Study study = Study.builder()
                .book(
                    Book.builder().
                        id(request.bookId())
                        .build()
                )
                .id(1L)
                .name(request.name())
                .thumbnailUrl(request.thumbnail())
                .description(request.description())
                .maxParticipant(request.maxParticipant())
                .gatherStartDate(request.gatherStartDate())
                .gatherEndDate(request.gatherEndDate())
                .build();
            StudyMember studyMember = StudyMember.builder()
                .id(1L)
                .status(OWNED)
                .user(makeUserWithId(1L))
                .study(makeStudyWithId(makeBookWithId(1L), RECRUITING, 1L))
                .build();
            Long want = 1L;

            when(studyConverter.createToEntity(any(StudyRequest.Create.class)))
                .thenReturn(study);
            when(studyRepository.save(any(Study.class)))
                .thenReturn(study);
            when(studyMemberRepository.save(any(StudyMember.class)))
                .thenReturn(studyMember);
            Long got = studyCommandService.createStudy(request, userId);

            assertThat(got).isEqualTo(want);
        }
    }

    @Nested
    @DisplayName("스터디 수정 #30")
    class EditTest {

        @Test
        @DisplayName("S 스터디 정보 수정을 할 수 있다")
        void editStudyInfo() {
            StudyRequest.Edit request = StudyRequest.Edit.builder()
                .name("스터디 이름")
                .thumbnail("https://example.com")
                .description("스터디 설명")
                .status(IN_PROGRESS.getMappingCode())
                .build();
            Long userId = 1L;
            Long studyId = 1L;
            Study study = Study.builder()
                .id(1L)
                .status(RECRUITING)
                .build();
            when(studyRepository.findStudyOwner(anyLong()))
                .thenReturn(userId);
            when(studyRepository.findById(studyId))
                .thenReturn(Optional.ofNullable(study));

            Long got = studyCommandService.editStudyInfo(studyId, userId, request);

            assertThat(got).isEqualTo(studyId);
        }

        @Test
        @DisplayName("F 스터디 정보 수정 중 로그인 유저가 스터디 장이 아니라면 예외가 발생합니다")
        void validateStudyOwner() {
            StudyRequest.Edit request = StudyRequest.Edit.builder()
                .name("스터디 이름")
                .thumbnail("https://example.com")
                .description("스터디 설명")
                .build();
            Long userId = 1L;
            Long studyOwnerId = 2L;
            Long studyId = 1L;
            Study study = Study.builder()
                .id(1L)
                .build();

            when(studyRepository.findStudyOwner(anyLong()))
                .thenThrow(new NotStudyOwnerException(
                    "스터디 정보 수정 권한이 없습니다. 유저 아이디 : " + userId + " 스터디장 Id : " + studyOwnerId,
                    ACCESS_DENIED));

            assertThatExceptionOfType(NotStudyOwnerException.class)
                .isThrownBy(() -> studyCommandService.editStudyInfo(studyId, userId, request))
                .withMessage(
                    "스터디 정보 수정 권한이 없습니다. 유저 아이디 : " + userId + " 스터디장 Id : " + studyOwnerId);
        }

        @Test
        @DisplayName("F 스터디 정보 수정 중 해당 스터디 ID를 가진 스터디가 존재하지 않을 경우 예외가 발생합니다.")
        void studyNotFound() {
            StudyRequest.Edit request = StudyRequest.Edit.builder()
                .name("스터디 이름")
                .thumbnail("https://example.com")
                .description("스터디 설명")
                .build();
            Long userId = 1L;
            Long studyId = 1L;
            Study study = Study.builder()
                .id(1L)
                .build();
            when(studyRepository.findStudyOwner(anyLong()))
                .thenReturn(userId);
            when(studyRepository.findById(studyId))
                .thenThrow(new StudyNotFoundException());

            assertThatExceptionOfType(StudyNotFoundException.class)
                .isThrownBy(() -> studyCommandService.editStudyInfo(studyId, userId, request));
        }
    }

    @Nested
    @DisplayName("스터디 가입 승낙 및 거절 #37")
    class AuditTest {

        private final Long studyId = 1L;

        private final Long memberId = 1L;

        private final Long userId = 1L;

        private final StudyRequest.Audit request = StudyRequest.Audit.builder()
            .status("ACCEPTED")
            .build();

        private final Book book = makeBookWithId(1L);

        private final User user = makeUserWithId(userId);

        Study study = makeStudyWithId(book, StudyStatus.RECRUITING, studyId);

        private final StudyMember studyMember = makeStudyMemberWithId(study, user, PENDING, 1L);

        @Test
        @DisplayName("S 스터디 가입 승낙 및 거절할 수 있다.")
        void auditStudyParticipationTest() {
            Long studyOwnerId = 1L;
            int joinStudy = 1;
            given(studyRepository.findById(anyLong())).willReturn(Optional.of(study));
            given(studyRepository.findStudyOwner(anyLong())).willReturn(studyOwnerId);
            given(studyMemberRepository.findById(anyLong()))
                .willReturn(Optional.of(studyMember));
            given(userRepository.userJoinedStudies(anyLong()))
                .willReturn(joinStudy);
            studyCommandService.auditStudyParticipation(studyId, memberId, userId, request);

            assertThat(studyMember.getStatus()).isEqualTo(StudyMemberStatus.ACCEPTED);
        }

        @Test
        @DisplayName("F 존재하지 않는 스터디 ID일 경우 예외가 발생한다.")
        void validateExistStudy() {
            given(studyRepository.findById(anyLong()))
                .willThrow(new StudyNotFoundException());

            assertThatExceptionOfType(StudyNotFoundException.class)
                .isThrownBy(
                    () -> studyCommandService.auditStudyParticipation(studyId, memberId, userId,
                        request));
        }

        @Test
        @DisplayName("F 현재 모집중인 스터디만 승인, 거절할 수 있습니다.")
        void recruitingStudy() {
            Study inProgressStudy = makeStudyWithId(book, IN_PROGRESS, studyId);

            given(studyRepository.findById(anyLong()))
                .willReturn(Optional.of(inProgressStudy));
            doThrow(NotRecruitingStudyException.class)
                .when(studyValidator)
                .validateRecruitingStudy(any(Study.class));
            assertThatExceptionOfType(NotRecruitingStudyException.class)
                .isThrownBy(
                    () -> studyCommandService.auditStudyParticipation(studyId, memberId, userId,
                        request));
        }

        @Test
        @DisplayName("F 현재 스터디원이 최대치에 도달했을때 더 이상 승인을 할 수 없습니다")
        void fullMemberStudy() {
            Study inProgressStudy = makeStudyWithId(book, IN_PROGRESS, studyId);

            given(studyRepository.findById(anyLong()))
                .willReturn(Optional.of(inProgressStudy));
            doThrow(StudyMemberFullException.class)
                .when(studyValidator)
                .validateFullMemberStudy(any(Study.class));
            assertThatExceptionOfType(StudyMemberFullException.class)
                .isThrownBy(
                    () -> studyCommandService.auditStudyParticipation(studyId, memberId, userId,
                        request));
        }

        @Test
        @DisplayName("F 스터디장이 아닌 유저가 변경 시도시 예외가 발생한다.")
        void validateStudyOwner() {
            Long studyId = 1L;
            Long memberId = 1L;
            Long userId = 1L;
            Long studyOwnerId = 2L;

            StudyRequest.Audit request = StudyRequest.Audit.builder()
                .status("ACCEPTED")
                .build();

            String errorMessage =
                "스터디 승인 권한이 없습니다. 유저 Id : " + userId + " 스터디 장 Id : " + studyOwnerId;

            doThrow(new NotStudyOwnerException(errorMessage, ErrorMessage.ACCESS_DENIED))
                .when(studyValidator)
                .validateStudyOwner(anyLong(), anyLong(), anyString());

            given(studyRepository.findById(anyLong())).willReturn(Optional.of(study));
            given(studyRepository.findStudyOwner(anyLong())).willReturn(studyOwnerId);

            assertThatExceptionOfType(NotStudyOwnerException.class)
                .isThrownBy(
                    () -> studyCommandService
                        .auditStudyParticipation(studyId, memberId, userId, request))
                .withMessage("스터디 승인 권한이 없습니다. 유저 Id : " + userId + " 스터디 장 Id : " + studyOwnerId);
        }

        @Test
        @DisplayName("F 해당 신청이 존재하지 않을 경우 예외가 발생한다.")
        void studyJoinRequestNotFound() {
            Long studyId = 1L;
            Long memberId = 1L;
            Long userId = 1L;
            Long studyOwnerId = 1L;

            StudyRequest.Audit request = StudyRequest.Audit.builder()
                .status("ACCEPTED")
                .build();

            given(studyRepository.findById(anyLong())).willReturn(Optional.of(study));
            given(studyRepository.findStudyOwner(anyLong())).willReturn(studyOwnerId);
            given(studyMemberRepository.findById(anyLong()))
                .willReturn(Optional.empty());

            assertThatExceptionOfType(StudyJoinRequestNotFoundException.class)
                .isThrownBy(() ->
                    studyCommandService
                        .auditStudyParticipation(studyId, memberId, userId, request));
        }

        @Test
        @DisplayName("F 가입 하려는 유저가 이미 최대 스터디 수에 도달한 경우 예외발생")
        void maximumJoinStudy() {
            Long studyOwnerId = 1L;
            int joinStudy = 10;
            given(studyRepository.findById(anyLong())).willReturn(Optional.of(study));
            given(studyRepository.findStudyOwner(anyLong())).willReturn(studyOwnerId);
            given(studyMemberRepository.findById(anyLong()))
                .willReturn(Optional.of(studyMember));
            given(userRepository.userJoinedStudies(anyLong()))
                .willReturn(joinStudy);
            doThrow(StudyJoinMaximumReachedException.class)
                .when(studyValidator)
                .validateMaximumJoinStudy(joinStudy);

            assertThatExceptionOfType(StudyJoinMaximumReachedException.class)
                .isThrownBy(
                    () -> studyCommandService.auditStudyParticipation(studyId, memberId, userId,
                        request));
        }
    }

    @Nested
    @DisplayName("스터디 가입 신청 #52")
    class RequestStudyJoinTest {

        Book book = makeBookWithId(1L);

        User user = makeUserWithId(1L);

        Study study = makeStudyWithId(book, RECRUITING, 1L);

        StudyMember studyMember = makeStudyMember(study, user, StudyMemberStatus.PENDING);

        @Test
        @DisplayName("S 스터디 가입 신청을 할 수 있습니다.")
        void requestStudyJoin() {
            given(studyRepository.findById(anyLong()))
                .willReturn(Optional.of(study));
            given(studyMemberRepository.findByUserAndStudy(anyLong(), anyLong()))
                .willReturn(Optional.empty());
            given(studyMemberRepository.save(any(StudyMember.class)))
                .willReturn(studyMember);

            Long got = studyCommandService.requestStudyJoin(study.getId(), user.getId());
            Long want = studyMember.getId();

            assertThat(got).isEqualTo(want);
        }

        @Test
        @DisplayName("S 만약 거절당했다면 스터디 가입 재신청을 할 수 있습니다.")
        void reRequestStudyJoin() {
            StudyMember deniedMember =
                makeStudyMemberWithId(study, user, StudyMemberStatus.DENIED, 1L);

            given(studyRepository.findById(anyLong()))
                .willReturn(Optional.of(study));
            given(studyMemberRepository.findByUserAndStudy(anyLong(), anyLong()))
                .willReturn(Optional.of(deniedMember));
            given(studyMemberRepository.save(any(StudyMember.class)))
                .willReturn(deniedMember);

            Long got = studyCommandService.requestStudyJoin(study.getId(), user.getId());
            Long want = deniedMember.getId();

            assertThat(got).isEqualTo(want);
        }


        @Test
        @DisplayName("F 해당 스터디가 존재하지 않는다면 예외 발생")
        void studyNotFound() {
            Long notExistStudyId = 0L;

            given(studyRepository.findById(anyLong()))
                .willReturn(Optional.empty());

            assertThatExceptionOfType(StudyNotFoundException.class)
                .isThrownBy(
                    () -> studyCommandService.requestStudyJoin(notExistStudyId, user.getId()));
        }

        @Test
        @DisplayName("F 현재 모집중인 스터디가 아니라면 예외 발생")
        void recruitingStudy() {
            Study inProgressStudy = makeStudyWithId(book, IN_PROGRESS, study.getId());

            given(studyRepository.findById(anyLong()))
                .willReturn(Optional.of(inProgressStudy));
            doThrow(NotRecruitingStudyException.class)
                .when(studyValidator)
                .validateRecruitingStudy(any(Study.class));
            assertThatExceptionOfType(NotRecruitingStudyException.class)
                .isThrownBy(
                    () -> studyCommandService.requestStudyJoin(study.getId(), user.getId()));
        }

        @Test
        @DisplayName("F 현재 스터디원이 최대치에 도달했을때 더 이상 신청을 할 수 없습니다")
        void fullMemberStudy() {
            given(studyRepository.findById(anyLong()))
                .willReturn(Optional.of(study));
            doThrow(StudyMemberFullException.class)
                .when(studyValidator)
                .validateFullMemberStudy(any(Study.class));
            assertThatExceptionOfType(StudyMemberFullException.class)
                .isThrownBy(
                    () -> studyCommandService.requestStudyJoin(study.getId(), user.getId()));
        }

        @Test
        @DisplayName("F 유저가 이미 가입 요청을 했다면 예외 발생")
        void duplicateStudyJoin() {
            Study study = makeStudyWithId(makeBookWithId(1L), RECRUITING, 1L);
            User user = makeUserWithId(1L);

            StudyMember studyMember =
                makeStudyMemberWithId(study, user, OWNED, 1L);

            given(studyRepository.findById(anyLong()))
                .willReturn(Optional.of(study));
            given(studyMemberRepository.findByUserAndStudy(anyLong(), anyLong()))
                .willReturn(Optional.of(studyMember));

            doThrow(new DuplicateStudyJoinRequestException(STUDY_JOIN_REQUEST_DUPLICATE))
                .when(studyValidator)
                .validateDuplicateStudyMemberRequest(any(StudyMember.class));

            assertThatExceptionOfType(DuplicateStudyJoinRequestException.class)
                .isThrownBy(
                    () -> studyCommandService.requestStudyJoin(study.getId(), user.getId()));
        }

    }
}
