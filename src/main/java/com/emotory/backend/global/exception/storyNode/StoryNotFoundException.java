package com.emotory.backend.global.exception.storyNode;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class StoryNotFoundException extends CustomException {
    public StoryNotFoundException() { super(ErrorCode.STORY_NOT_FOUND); }
}