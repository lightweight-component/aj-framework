package com.ajaxjs.security.dfa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
//@AutoConfigureMockMvc
class SensitiveWordHttpTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private SensitiveWordFilter filter;

    @BeforeEach
    void resetWords() {
        filter.reloadWords(List.of("app", "apple"));
    }

    @Test
    void booleanAndListAreWrappedWithoutChangingTheirTypes() throws Exception {
        mvc.perform(get("/api/sensitive-word/check").param("text", "apple"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").value(true));
        mvc.perform(get("/api/sensitive-word/check").param("text", "safe"))
                .andExpect(jsonPath("$.status").value(1)).andExpect(jsonPath("$.data").value(false));
        mvc.perform(get("/api/sensitive-word/find-all").param("text", "apple"))
                .andExpect(jsonPath("$.data.length()").value(2)).andExpect(jsonPath("$.data[1].word").value("apple"));
        mvc.perform(get("/api/sensitive-word/find-all").param("text", "safe"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void filterDtoAndStatusUseSameEnvelope() throws Exception {
        mvc.perform(get("/api/sensitive-word/filter").param("text", "apple!").param("replacement", "[x]"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.filteredText").value("[x]".repeat(5) + "!"))
                .andExpect(jsonPath("$.data.hasSensitive").value(true))
                .andExpect(jsonPath("$.data.sensitiveWordCount").value(2));
        mvc.perform(get("/api/sensitive-word/status"))
                .andExpect(jsonPath("$.status").value(1)).andExpect(jsonPath("$.data.status").value("running"));
    }

    @Test
    void formPostsWorkAndInvalidReloadDoesNotEraseDictionary() throws Exception {
        mvc.perform(post("/api/sensitive-word/add").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("word", "new"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));
        mvc.perform(post("/api/sensitive-word/add-batch").param("words", " one, two "))
                .andExpect(jsonPath("$.data").value(true));
        mvc.perform(post("/api/sensitive-word/reload").param("words", " ,  , "))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.message").value("敏感词列表不能为空"));
        mvc.perform(get("/api/sensitive-word/check").param("text", "apple"))
                .andExpect(jsonPath("$.data").value(true));
        mvc.perform(post("/api/sensitive-word/reload").param("words", " x "))
                .andExpect(jsonPath("$.data").value(true));
        mvc.perform(get("/api/sensitive-word/check").param("text", "x"))
                .andExpect(jsonPath("$.data").value(true));
        mvc.perform(get("/api/sensitive-word/check"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(0));
    }
}
