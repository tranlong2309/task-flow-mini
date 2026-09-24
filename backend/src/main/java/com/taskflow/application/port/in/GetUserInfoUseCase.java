package com.taskflow.application.port.in;

import com.taskflow.domain.model.User;

public interface GetUserInfoUseCase {
    User getCurrentUser(Long userId);
}
