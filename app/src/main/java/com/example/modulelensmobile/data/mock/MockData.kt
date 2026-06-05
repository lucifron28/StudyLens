package com.example.modulelensmobile.data.mock

import com.example.modulelensmobile.domain.model.AcademicTask
import com.example.modulelensmobile.domain.model.BoardScan
import com.example.modulelensmobile.domain.model.Dashboard
import com.example.modulelensmobile.domain.model.LearningModule
import com.example.modulelensmobile.domain.model.Subject
import com.example.modulelensmobile.domain.model.User

object MockData {
    val currentUser = User(
        id = 0,
        username = "demo_student",
        email = "student@example.com",
        firstName = "Student",
        lastName = "Name"
    )

    val subjects = listOf(
        Subject("s1", "CS301", "Native Android Development", "Learn to build Android apps using Jetpack Compose and Kotlin", "3 Modules, 2 Tasks", 45),
        Subject("s2", "DB204", "Database Systems", "Relational database design and SQL", "5 Modules", 12),
        Subject("s3", "SE101", "Software Engineering", "Software development lifecycle and methodologies", "2 Modules, 1 Task", 80)
    )

    val tasks = listOf(
        AcademicTask("t1", "Finish Jetpack Compose Lab", "CS301", "Tomorrow, 11:59 PM", false),
        AcademicTask("t2", "ER Diagram Assignment", "DB204", "Friday, 5:00 PM", false)
    )

    val boardScans = listOf(
        BoardScan("b1", "Compose State Management", "CS301", "Oct 12", "Needs Review", "remember and rememberSaveable differences..."),
        BoardScan("b2", "Normalization 1NF to 3NF", "DB204", "Oct 10", "Reviewed", "1NF: Atomic values. 2NF: No partial dependencies...")
    )

    val modules = listOf(
        LearningModule("m1", "s1", "1. Introduction to Jetpack Compose", "Declarative UI paradigm...", 100),
        LearningModule("m2", "s1", "2. State Management", "Understanding state hoisting...", 20),
        LearningModule("m3", "s1", "3. Navigation", "Setting up NavHost...", 0)
    )

    val recentActivity = listOf(
        "Read Module: 2. State Management",
        "Scanned Board: Compose State Management",
        "Completed Quiz: Jetpack Compose Basics"
    )

    val dashboard = Dashboard(
        user = currentUser,
        overallProgress = 45,
        upcomingTasks = tasks,
        recentActivity = recentActivity
    )
}
