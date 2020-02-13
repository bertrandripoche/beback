package com.depuisletemps.beback.model

import java.util.*

data class Loan (
    var requestor_id:String,
    var recipient_id:String,
    var type:String,
    var product:String,
    var product_category:String,
    var creation_date : String,
    var due_date:String?,
    var returned_date:String?
)
