package com.example.model

data class ActiveLoan(
    val id: String,
    val lender: String,
    val principalBorrowed: Double,
    val dailyPayment: Double,
    val daysRemaining: Int,
    val interestRate: Float = 0.08f
) {
    val remainingBalance: Double get() = dailyPayment * daysRemaining
}

data class FinancialRecord(
    val day: Int,
    val revenue: Double,
    val expenses: Double,
    val activeUsers: Int,
    val cashAtEndOfDay: Double
) {
    val profit: Double get() = revenue - expenses
}

data class DailyLedger(
    val productRevenue: Double = 0.0,
    val contractRevenue: Double = 0.0,
    val payrollExpenses: Double = 0.0,
    val rentExpenses: Double = 0.0,
    val serverExpenses: Double = 0.0,
    val marketingExpenses: Double = 0.0,
    val loanRepaymentExpenses: Double = 0.0
) {
    val totalRevenue: Double get() = productRevenue + contractRevenue
    val totalExpenses: Double get() = payrollExpenses + rentExpenses + serverExpenses + marketingExpenses + loanRepaymentExpenses
    val netProfit: Double get() = totalRevenue - totalExpenses
}
