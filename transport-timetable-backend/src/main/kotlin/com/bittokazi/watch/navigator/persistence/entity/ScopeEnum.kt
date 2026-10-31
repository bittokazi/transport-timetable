package com.bittokazi.watch.navigator.persistence.entity

enum class ScopeEnum {
    ROLE_SUPER_ADMIN,
    ROLE_USER,

    // User
    USER_WHO_AM_I,
    USER_READ_ALL,
    USER_READ,
    USER_ADD,
    USER_UPDATE,

    // Role
    ROLE_READ_ALL,

    // Feedback
    FEEDBACK_CREATE,
    FEEDBACK_READ_ALL,
    FEEDBACK_RESOLVE,

    // Campaign
    CAMPAIGN_CREATE,

    // Transport
    USER_TRANSPORT_SEARCH,
    USER_TRANSPORT_PLAN,
    USER_TRANSPORT_MAP,
}
