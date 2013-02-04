package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class Welcome extends Controller {
	
	public static Result index() {
		return ok(views.html.welcome.index.render());
	}

}
