package com.devcourse.checkmoi.domain.book.stub;

import com.devcourse.checkmoi.domain.book.dto.BookRequest.CreateBook;
import com.devcourse.checkmoi.domain.book.dto.BookResponse.BookInfo;
import com.devcourse.checkmoi.domain.book.model.Book;
import com.devcourse.checkmoi.domain.book.model.PublishedDate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record PersistedDummyData(String author, String title, String thumbnail, Long bookId,
                                 String description, String isbn, String publisher,
                                 String publishedAt, LocalDateTime createdAt) {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public PersistedDummyData(String author, String title, String thumbnail, Long bookId,
        String description,
        String isbn, String publisher, String publishedAt) {

        this(author, title, thumbnail, bookId, description, isbn, publisher,
            publishedAt,
            LocalDateTime.now());
    }

    public BookInfo simple() {
        return BookInfo.builder()
            .author(this.author)
            .createdAt(this.createdAt)
            .title(this.title)
            .image(this.thumbnail)
            .description(this.description)
            .id(this.bookId)
            .isbn(this.isbn)
            .publisher(this.publisher)
            .pubdate(LocalDate.parse(this.publishedAt, formatter))
            .build();
    }

    public CreateBook create() {
        return CreateBook.builder()
            .author(this.author)
            .description(this.description)
            .isbn(this.isbn)
            .pubDate(this.publishedAt)
            .publisher(this.publisher)
            .image(this.thumbnail)
            .title(this.title)
            .build();
    }

    public Book book() {
        return Book.builder()
            .author(this.author)
            .title(this.title)
            .thumbnail(this.thumbnail)
            .id(this.bookId)
            .description(this.description)
            .isbn(this.isbn)
            .publisher(this.publisher)
            .publishedAt(new PublishedDate(this.publishedAt))
            .build();
    }


}
