package uk.co.redsoft.sandbox.adapters.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.redsoft.sandbox.domain.model.Book;
import uk.co.redsoft.sandbox.domain.model.BookDetail;
import uk.co.redsoft.sandbox.domain.model.CreateBookCommand;
import uk.co.redsoft.sandbox.domain.ports.in.BookWriteUseCase;
import uk.co.redsoft.sandbox.domain.ports.in.BookDetailUseCase;
import uk.co.redsoft.sandbox.domain.ports.in.BookQueryUseCase;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookQueryUseCase bookQueryUseCase;

    @MockitoBean
    private BookWriteUseCase bookWriteUseCase;

    @MockitoBean
    private BookDetailUseCase bookDetailUseCase;

    @Test
    void getAllBooksReturns200WithAllBooks() throws Exception {
        when(bookQueryUseCase.findAll()).thenReturn(List.of(
                new Book(1L, "The Pragmatic Programmer", "David Thomas & Andrew Hunt", "978-0135957059", "Software Engineering"),
                new Book(2L, "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering")
        ));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("The Pragmatic Programmer"))
                .andExpect(jsonPath("$[1].title").value("Clean Code"));
    }

    @Test
    void getBookReturns200WithBookAndPriceWhenFound() throws Exception {
        when(bookDetailUseCase.getBookDetail(1L)).thenReturn(Optional.of(
                new BookDetail(1L, "The Pragmatic Programmer", "David Thomas & Andrew Hunt",
                        "978-0135957059", "Software Engineering", new java.math.BigDecimal("29.99"))
        ));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The Pragmatic Programmer"))
                .andExpect(jsonPath("$.author").value("David Thomas & Andrew Hunt"))
                .andExpect(jsonPath("$.gbrPrice").value(29.99));
    }

    @Test
    void getBookReturns200WithNullPriceWhenNoGbrPriceExists() throws Exception {
        when(bookDetailUseCase.getBookDetail(1L)).thenReturn(Optional.of(
                new BookDetail(1L, "The Pragmatic Programmer", "David Thomas & Andrew Hunt",
                        "978-0135957059", "Software Engineering", null)
        ));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gbrPrice").doesNotExist());
    }

    @Test
    void getBookReturns404WhenNotFound() throws Exception {
        when(bookDetailUseCase.getBookDetail(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBookReturns400WhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","author":"Author","isbn":"978-0132350884","genre":"Tech"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void createBookReturns400WhenIsbnIsInvalid() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code","author":"Author","isbn":"not-an-isbn","genre":"Tech"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isbn").exists());
    }

    @Test
    void importBooksReturns202() throws Exception {
        var csv = "title,author,isbn,genre\nClean Code,Robert C. Martin,978-0132350884,Software Engineering\n";
        var file = new MockMultipartFile("file", "books-10.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/books/import").file(file))
                .andExpect(status().isAccepted());
    }

    @Test
    void importBooksSkipsInvalidRows() throws Exception {
        var csv = "title,author,isbn,genre\n" +
                  "Clean Code,Robert C. Martin,978-0132350884,Software Engineering\n" +
                  "Bad Book,Author,not-an-isbn,Tech\n";
        var file = new MockMultipartFile("file", "books.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/books/import").file(file))
                .andExpect(status().isAccepted());

        verify(bookWriteUseCase, times(1)).importBook(any(CreateBookCommand.class));
    }

    @Test
    void createBookReturns201WithLocationAndBody() throws Exception {
        when(bookWriteUseCase.create(any(CreateBookCommand.class))).thenReturn(
                new Book(1L, "Clean Code", "Robert C. Martin", "978-0132350884", "Software Engineering")
        );

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code","author":"Robert C. Martin","isbn":"978-0132350884","genre":"Software Engineering"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/books/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }
}
