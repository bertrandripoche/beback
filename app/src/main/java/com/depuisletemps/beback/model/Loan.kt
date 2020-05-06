package com.depuisletemps.beback.model

import com.google.firebase.Timestamp

data class Loan (
    var id:String = "",
    var requestor_id:String = "",
    var recipient_id:String = "",
    var type:String = "",
    var product:String = "",
    var product_category:String = "",
    var creation_date : Timestamp? = null,
    var due_date:Timestamp? = null,
    var notif: String? = null,
    var returned_date:Timestamp? = null
)