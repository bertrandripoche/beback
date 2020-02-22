package com.depuisletemps.beback.model

data class Loaner(var name: String, var lending: Int = 0, var borrowing: Int = 0){
    constructor(): this("",0,0)
}