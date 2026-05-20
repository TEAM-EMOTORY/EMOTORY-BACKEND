package com.emotory.backend.global.exception.PlaySession;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class PlaySessionNotFoundException extends CustomException {
    public PlaySessionNotFoundException() { super(ErrorCode.PLAY_SESSION_NOT_FOUND); }
}