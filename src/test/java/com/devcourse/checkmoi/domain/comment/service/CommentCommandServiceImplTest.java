package com.devcourse.checkmoi.domain.comment.service;

import static com.devcourse.checkmoi.domain.post.model.PostCategory.GENERAL;
import static com.devcourse.checkmoi.domain.study.model.StudyStatus.IN_PROGRESS;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeBook;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeComment;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makePost;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeStudy;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeStudyMember;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeUser;
import static org.assertj.core.api.Assertions.assertThat;
import com.devcourse.checkmoi.domain.book.model.Book;
import com.devcourse.checkmoi.domain.book.repository.BookRepository;
import com.devcourse.checkmoi.domain.comment.model.Comment;
import com.devcourse.checkmoi.domain.comment.repository.CommentRepository;
import com.devcourse.checkmoi.domain.post.model.Post;
import com.devcourse.checkmoi.domain.post.repository.PostRepository;
import com.devcourse.checkmoi.domain.study.model.Study;
import com.devcourse.checkmoi.domain.study.model.StudyMemberStatus;
import com.devcourse.checkmoi.domain.study.repository.StudyMemberRepository;
import com.devcourse.checkmoi.domain.study.repository.StudyRepository;
import com.devcourse.checkmoi.domain.user.model.User;
import com.devcourse.checkmoi.domain.user.repository.UserRepository;
import com.devcourse.checkmoi.template.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommentCommandServiceImplTest extends IntegrationTest {

    @Autowired
    protected CommentRepository commentRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected StudyRepository studyRepository;

    @Autowired
    protected BookRepository bookRepository;

    @Autowired
    protected StudyMemberRepository studyMemberRepository;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    private CommentCommandServiceImpl commentCommandService;

    private User givenUser;

    private Post givenPost;

    @AfterEach
    void cleanUp() {
        commentRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        studyMemberRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @BeforeEach
    void setBasicGiven() {
        Book book = bookRepository.save(makeBook());
        // user
        User user = userRepository.save(makeUser());
        givenUser = userRepository.save(makeUser());

        // study
        Study givenStudy = studyRepository.save(makeStudy(book, IN_PROGRESS));

        // studyMember
        studyMemberRepository.save(makeStudyMember(givenStudy, user, StudyMemberStatus.OWNED));
        studyMemberRepository.save(
            makeStudyMember(givenStudy, givenUser, StudyMemberStatus.ACCEPTED));

        // given post
        givenPost = postRepository.save(makePost(GENERAL, givenStudy, givenUser));
    }

    @Nested
    @DisplayName("댓글 삭제 #136")
    class DeleteComment {

        Comment comment;

        @BeforeEach
        void createComment() {
            comment = commentRepository.save(makeComment(givenPost, givenUser));
            assertThat(commentRepository.existsById(comment.getId())).isTrue();
        }

        @Test
        @DisplayName("S 댓글 작성자는 댓글을 삭제할 수 있다.")
        void deleteComment() {
            commentCommandService.deleteById(givenUser.getId(), comment.getId());
            assertThat(commentRepository.existsById(comment.getId())).isFalse();
        }
    }
}
