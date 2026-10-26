package com.example.model

data class GameEventOption(
    val label: String,
    val description: String,
    val actionType: EventActionType,
    val amount: Double = 0.0
)

enum class EventActionType {
    ACCEPT_INVESTMENT,
    REJECT_INVESTMENT,
    PAY_FIX,
    RISK_DOWNTIME,
    MATCH_SALARY,
    LET_EMPLOYEE_GO,
    SIGN_ENTERPRISE_DEAL,
    BUY_FIREWALL,
    DISMISS
}

data class GameEvent(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val options: List<GameEventOption>,
    val relatedEntityId: String? = null
)
