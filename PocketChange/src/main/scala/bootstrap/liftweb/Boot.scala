package bootstrap.liftweb

import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import Helpers._
import com.pocketchangeapp.model._
import com.pocketchangeapp.api._
import com.pocketchangeapp.util.{Charting,Image}
import com.pocketchangeapp.db.Database

/* Connect Lucene/Compass for search */
class Boot {
  def boot {
    LiftRules.early.append {
      _.setCharacterEncoding("UTF-8")
    }

    Database.ensureIndexes()
    S.addAround(Database.liftRequestWrapper)
    LiftRules.addToPackages("com.pocketchangeapp")     

    LiftRules.setSiteMap(SiteMap(MenuInfo.menu :_*))

    LiftRules.dispatch.prepend(RestAPI.dispatch)

    // Set up some rewrites
    LiftRules.rewrite.append {
      case RewriteRequest(ParsePath(List("account", acctName), _, _, _), _, _) =>
	RewriteResponse("viewAcct" :: Nil, Map("name" -> urlDecode(acctName)))
      case RewriteRequest(ParsePath(List("account", acctName, tag), _, _, _), _, _) =>
	RewriteResponse("viewAcct" :: Nil, Map("name" -> urlDecode(acctName), "tag" -> urlDecode(tag)))
    }

    // Custom dispatch for graph and receipt image generation
    LiftRules.dispatch.append {
      case Req(List("graph", acctName, "history"), _, _) =>
	() => Charting.history(acctName)
      case Req(List("graph", acctName, "tagpie"), _, _) =>
	() => Charting.tagpie(acctName)
      case Req(List("graph", acctName, "tagbar"), _, _) =>
	() => Charting.tagbar(acctName)
      case Req(List("image", expenseId), _, _) =>
	() => Full(Image.viewImage(expenseId))
    }

    import scala.xml.Text
    val m = Title(if (User.loggedIn_?) { Text("a") } else { Text("b") })

    Log.info("Bootstrap up")
  }
}

object MenuInfo {
  import Loc._
  val IfLoggedIn = If(() => User.currentUser.isDefined, "You must be logged in")
  def menu: List[Menu] =  Menu(Loc("home", List("index"), "Home")) :: 
    Menu(Loc("manageAccts", List("manage"), "Manage Accounts", IfLoggedIn)) :: 
    Menu(Loc("addAcct", List("editAcct"), "Add Account", Hidden, IfLoggedIn)) ::
    Menu(Loc("viewAcct", List("viewAcct") -> true, "View Account", Hidden, IfLoggedIn)) ::
    Menu(Loc("help", List("help", "index"), "Help")) ::
    User.sitemap
}