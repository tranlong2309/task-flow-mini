package com.taskflow.application.port.in;

import com.taskflow.domain.model.User;
import java.util.List;

public interface GetUserInfoUseCase {
    User getCurrentUser(Long userId);
    List<User> getAllUsers();
}
