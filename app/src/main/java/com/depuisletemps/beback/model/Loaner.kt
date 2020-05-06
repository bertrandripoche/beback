package com.depuisletemps.beback.model

data class Loaner(
    var name: String = "",
    var lending: Int? = null,
    var borrowing: Int? = null,
    var ended_lending: Int? = null,
    var ended_borrowing: Int? = null,
    var delivery: Int? = null,
    var ended_delivery: Int? = null,
    var my_points: Int? = null,
    var their_points: Int? = null)