package com.example.timewarp.domain.model;

/**
 * Roles supported by the application.
 */
public enum UserType {
    /** User who books campsites and enters lotteries. */
    Entrant,
    /** User who owns and manages campsites. */
    CampsiteOwner,
    /** User with administrative access. */
    Admin,
}