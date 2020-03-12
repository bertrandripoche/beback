package com.depuisletemps.beback.model

data class Loaner(var name: String, var lending: Int?, var borrowing: Int?, var ended_lending: Int?, var ended_borrowing: Int?, var delivery: Int?, var ended_delivery: Int?, var my_points: Int?, var their_points: Int?){
    constructor() : this("",null,null,null,null,null, null,null, null)
}