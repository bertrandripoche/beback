package com.depuisletemps.beback.model

enum class LoanType(val type: String) {
    LENDING("lending"),
    BORROWING("borrowing"),
    DELIVERY("delivery"),
    ENDED_LENDING("ended_lending"),
    ENDED_BORROWING("ended_borrowing"),
    ENDED_DELIVERY("ended_delivery")
}