package com.example.modulelensmobile.domain.model

data class Dashboard(
    val user: User,
    val overallProgress: Int,
    val upcomingTasks: List<AcademicTask>,
    val recentActivity: List<String>
)
