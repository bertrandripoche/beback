package com.depuisletemps.beback.model

import java.util.*

class Loan (
    val id:String,
    var requestor_id:String,
    var recipient_id:String,
    var type:String,
    var product:String,
    var product_category:String,
    var creation_date : String,
    var due_date:String?,
    var returned_date:String?
)
