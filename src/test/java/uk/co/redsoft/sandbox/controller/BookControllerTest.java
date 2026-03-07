package uk.co.redsoft.sandbox.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.co.redsoft.sandbox.model.Book;
import uk.co.redsoft.sandbox.service.BookService;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
    private BookService bookService;

@Test
    void getAllBooksReturns200WithAllBooks() throws Exception {
        when(bookService.findAll()).thenReturn(List.of(
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
    void getBookReturns200WithBookWhenFound() throws Exception {
        when(bookService.findById(1L)).thenReturn(Optional.of(
                new Book(1L, "The Pragmatic Programmer", "David Thomas & Andrew Hunt", "978-0135957059", "Software Engineering")
        ));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The Pragmatic Programmer"))
                .andExpect(jsonPath("$.author").value("David Thomas & Andrew Hunt"));
    }

    @Test
    void getBookReturns404WhenNotFound() throws Exception {
        when(bookService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void importBooksReturns202() throws Exception {
        var csv = "title,author,isbn,genre\nClean Code,Robert C. Martin,978-0132350884,Software Engineering\n";
        var file = new MockMultipartFile("file", "books-10.csv", "text/csv", csv.getBytes());

        mockMvc.perform(multipart("/books/import").file(file))
                .andExpect(status().isAccepted());
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
    void importBooksReturns400WhenFileCannotBeRead() throws Exception {
        var file = new MockMultipartFile("file", "bad.csv", "text/csv", new byte[0]);
        doThrow(new IOException("stream closed")).when(bookService).importCsv(any());

        mockMvc.perform(multipart("/books/import").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Could not read uploaded file"));
    }

    @Test
    void createBookReturns201WithLocationAndBody() throws Exception {
        when(bookService.create(any(Book.class))).thenReturn(
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
