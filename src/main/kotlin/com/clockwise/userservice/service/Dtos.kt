package com.clockwise.userservice.service

import com.clockwise.userservice.domain.UserStatus

data class UpdateUserStatusRequest(
    val status: UserStatus
) 