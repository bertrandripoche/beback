package com.depuisletemps.beback.model

import java.util.*

class Loan (val id:String, var requestor_id:String, var recipient_id:String, var type:String, var product:String, var product_category:String, var creation_date : Date, var due_date:Date, var returned_date:Date)
