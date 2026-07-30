package app.caloriecore.ui.model

enum class PhoneStepStatus {
    Starting,
    Active,
    PermissionNeeded,
    NotAvailable,
    Error
}

data class PhoneStepState(
    val steps: Int = 0,
    val status: PhoneStepStatus = PhoneStepStatus.Starting
)
