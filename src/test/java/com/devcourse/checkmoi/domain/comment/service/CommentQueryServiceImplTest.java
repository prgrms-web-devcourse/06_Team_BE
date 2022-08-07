package com.devcourse.checkmoi.domain.comment.service;

import static com.devcourse.checkmoi.domain.post.model.PostCategory.BOOK_REVIEW;
import static com.devcourse.checkmoi.domain.study.model.StudyMemberStatus.ACCEPTED;
import static com.devcourse.checkmoi.domain.study.model.StudyMemberStatus.OWNED;
import static com.devcourse.checkmoi.domain.study.model.StudyStatus.IN_PROGRESS;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeBook;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeComment;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makePost;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeStudy;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeStudyMember;
import static com.devcourse.checkmoi.util.EntityGeneratorUtil.makeUser;
import static org.assertj.core.api.Assertions.assertThat;
import com.devcourse.checkmoi.domain.book.repository.BookRepository;
import com.devcourse.checkmoi.domain.comment.converter.CommentConverter;
import com.devcourse.checkmoi.domain.comment.dto.CommentRequest.Search;
import com.devcourse.checkmoi.domain.comment.dto.CommentResponse.CommentInfo;
import com.devcourse.checkmoi.domain.comment.repository.CommentRepository;
import com.devcourse.checkmoi.domain.post.model.Post;
import com.devcourse.checkmoi.domain.post.repository.PostRepository;
import com.devcourse.checkmoi.domain.study.repository.StudyMemberRepository;
import com.devcourse.checkmoi.domain.study.repository.StudyRepository;
import com.devcourse.checkmoi.domain.user.model.User;
import com.devcourse.checkmoi.domain.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CommentQueryServiceImplTest {

    @Autowired
    private CommentQueryServiceImpl commentQueryService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentConverter commentConverter;


    @Nested
    @DisplayName("작성된 글에 대한 댓글 목록 조회 #130")
    class FindAllComments {

        User user1;

        User user2;

        User user3;

        Post givenPost;

        @BeforeEach
        void given() {
            var book = bookRepository.save(makeBook());
            var study = studyRepository.save(makeStudy(book, IN_PROGRESS));

            user1 = userRepository.save(makeUser());
            user2 = userRepository.save(makeUser());
            user3 = userRepository.save(makeUser());

            studyMemberRepository.save(makeStudyMember(study, user1, OWNED));
            studyMemberRepository.save(makeStudyMember(study, user2, ACCEPTED));
            studyMemberRepository.save(makeStudyMember(study, user3, ACCEPTED));

            givenPost = postRepository.save(makePost(BOOK_REVIEW, study, user1));
        }

        // TODO: 권한체크
        @Test
        @DisplayName("S 해당 포스트에 작성한 글을 조회할 수 있다")
        void findAllComments() {
            List<CommentInfo> commentInfos = Stream.of(
                commentRepository.save(makeComment(givenPost, user2)),
                commentRepository.save(makeComment(givenPost, user3)),
                commentRepository.save(makeComment(givenPost, user2))
            ).map(commentConverter::commentToInfo).toList();

            List<CommentInfo> comments =
                commentQueryService.findAllComments(user2.getId(), Search.builder().build());

            assertThat(comments)
                .usingRecursiveFieldByFieldElementComparator()
                .hasSameElementsAs(commentInfos);
        }
    }

}
