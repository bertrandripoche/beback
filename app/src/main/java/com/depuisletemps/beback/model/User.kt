package com.depuisletemps.beback.model

data class User (
    val id:String = "",
    var mail:String = "",
    var firstname:String? = null,
    var lastname:String? = null,
    var pseudo:String? = null,
    var pic: String = ""
)