package com.emotory.backend.global.exception.storyResult;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class StoryResultNotFoundException extends CustomException {

    public StoryResultNotFoundException() {
        super(ErrorCode.STORY_RESULT_NOT_FOUND);
    }
}
