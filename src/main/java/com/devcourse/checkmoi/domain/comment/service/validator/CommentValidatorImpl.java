package com.devcourse.checkmoi.domain.comment.service.validator;

import com.devcourse.checkmoi.domain.comment.exception.CommentNoPermissionException;
import com.devcourse.checkmoi.domain.comment.model.Comment;
import com.devcourse.checkmoi.domain.study.exception.FinishedStudyException;
import com.devcourse.checkmoi.domain.study.model.Study;
import com.devcourse.checkmoi.domain.study.model.StudyMember;
import com.devcourse.checkmoi.domain.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class CommentValidatorImpl implements CommentValidator {

    public void editComment(Comment comment, Long userId) {
        User writer = comment.getUser();
        Study study = comment.getPost().getStudy();

        if (!writer.getId().equals(userId)) {
            throw new CommentNoPermissionException();
        }
        if (study.isFinished()) {
            throw new FinishedStudyException();
        }
    }

    @Override
    public void deleteComment(Comment comment, Long ownerId, Long userId) {
        User writer = comment.getUser();
        Study study = comment.getPost().getStudy();

        if (!(writer.getId().equals(userId) || ownerId.equals(userId))) {
            throw new CommentNoPermissionException();
        }
        if (study.isFinished()) {
            throw new FinishedStudyException();
        }
    }
}
