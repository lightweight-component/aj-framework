package com.ajaxjs.business.banned_words;

import lombok.Data;

import java.util.ArrayList;

@Data
public class PunctuationOrHtmlFilteredResult {
    private String originalString;

    private StringBuilder filteredString;

    private ArrayList<Integer> charOffsets;
}
