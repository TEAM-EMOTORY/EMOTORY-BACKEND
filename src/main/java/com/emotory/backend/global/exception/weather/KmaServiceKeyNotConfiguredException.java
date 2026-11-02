package com.emotory.backend.global.exception.weather;

import com.emotory.backend.global.exception.CustomException;
import com.emotory.backend.global.exception.ErrorCode;

public class KmaServiceKeyNotConfiguredException extends CustomException {

    public KmaServiceKeyNotConfiguredException() {
        super(ErrorCode.KMA_SERVICE_KEY_NOT_CONFIGURED);
    }
}
