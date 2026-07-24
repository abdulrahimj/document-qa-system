package com.groupten.documentqa.qa.dto;

import java.util.List;

public record QuestionResponse(String answer, List<SourceReference> sources) {
}
