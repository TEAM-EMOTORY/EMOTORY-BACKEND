package com.emotory.backend.global.exception.authoring;

import com.emotory.backend.domain.authoring.dto.AuthoringDtos.Issue;
import java.util.List;

public record AuthoringErrorResponse(int status, String message, List<Issue> errors) {}
