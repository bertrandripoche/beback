package com.depuisletemps.beback.model

import com.google.firebase.Timestamp

data class Loan (
    val id:String,
    var requestor_id:String,
    var recipient_id:String,
    var type:String,
    var product:String,
    var product_category:String,
    var creation_date : Timestamp?,
    var due_date:Timestamp?,
    var notif: String?,
    var returned_date:Timestamp?
){
    constructor(): this("","","", "", "", "", null, null, null, null)
}
