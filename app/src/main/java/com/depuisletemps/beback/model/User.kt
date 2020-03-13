package com.depuisletemps.beback.model

data class User (
    val id:String,
    var mail:String,
    var firstname:String?,
    var lastname:String?,
    var pseudo:String?,
    var pic: String?
) {
    constructor() : this("","",null,null,null,null)
}