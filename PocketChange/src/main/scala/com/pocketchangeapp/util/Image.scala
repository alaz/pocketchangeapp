package com.pocketchangeapp.util

import _root_.net.liftweb.http._
import _root_.net.liftweb.util._
import _root_.net.liftweb.mapper._
import S._
import Helpers._

import com.pocketchangeapp.model._

object Image {
  def viewImage(id : String) : LiftResponse = 
    try {
      User.currentUser match {
	case Full(user) => {
	  Expense.byId(id) match {
	    case Full(e) => {
	      if (e.owner == User.currentUser) {
		InMemoryResponse(e.receipt, List("Content-Type" -> e.receiptMime), Nil, 200)
	      } else {
		ForbiddenResponse()
	      }
	    }
	    case _ => PlainTextResponse("No such expense item", Nil, 404)
	  }
	}
	case _ => RedirectResponse("/user_mgt/login")
      }
    } catch {
      case nfe : NumberFormatException => PlainTextResponse("Invalid expense ID", Nil, 400)
    }
}
