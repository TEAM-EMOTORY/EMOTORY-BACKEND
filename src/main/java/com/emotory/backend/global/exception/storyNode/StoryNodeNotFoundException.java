package com.emotory.backend.global.exception.storyNode;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class StoryNodeNotFoundException extends CustomException {
    public StoryNodeNotFoundException() { super(ErrorCode.STORY_NODE_NOT_FOUND); }
}