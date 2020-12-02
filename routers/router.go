package routers

import (
	"appinhouse/controllers"
	"github.com/astaxie/beego/server/web"
)

func init() {

	web.Router("/", &controllers.ViewController{}, "get:Index")
	web.Router("/view/index", &controllers.ViewController{}, "get:Index")
	web.Router("/view/createapp", &controllers.ViewController{}, "get:CreateApp")
	web.Router("/view/app", &controllers.ViewController{}, "get:App")
	web.Router("/view/info", &controllers.ViewController{}, "get:Info")
	web.Router("/view/update", &controllers.ViewController{}, "get:Update")
	web.Router("/view/delete", &controllers.ViewController{}, "get:Delete")
	web.Router("/view/help", &controllers.ViewController{}, "get:Help")

	web.Router("/api/:app/mobile/last", &controllers.GetLastBuildsController{}, "get:GetLastBuilds")
	web.Router("/api/:app/mobile/list/:environment", &controllers.GetBuildsController{}, "get:GetBuilds4Mobile")
	web.Router("/api/:app/last", &controllers.GetLastBuildsController{}, "get:GetLastBuilds")
	web.Router("/api/:app/list/:platform/:environment", &controllers.GetBuildsController{}, "get:GetBuilds")
	web.Router("/api/:app/plist/:environment/:version", &controllers.GetPlistController{}, "get:GetPList")
	web.Router("/api/:app/delete/:platform/:environment", &controllers.RemoveBuildsontroller{}, "delete:Clean")
	web.Router("/api/:app/desc/:platform/:environment", &controllers.AddBuildController{}, "post:AddBuild")
	web.Router("/api/:app/create", &controllers.CreateAppController{}, "post:CreateApp")
	web.Router("/api/:app/update", &controllers.ModifyAppController{}, "post:ModifyApp")
	web.Router("/api/:app/delete", &controllers.DeleteAppController{}, "delete:DeleteApp")
	web.Router("/api/:app/get", &controllers.GetAppController{}, "get:GetApp")
	web.Router("/api/apps", &controllers.GetAppsController{}, "get:GetApps")
	web.Router("/api/modify/app", &controllers.ModifyDataController{}, "get:ModifyAppData")
	web.Router("/api/move/:app/:operation", &controllers.MoveAppController{}, "post:MoveApp")
	web.Router("/api/:app/:platform/:environment/:version", &controllers.GetBuildController{}, "get:GetBuild")
}
