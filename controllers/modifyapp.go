// modifyapp
package controllers

import (
	. "appinhouse/constants"
	"appinhouse/models"

	"github.com/astaxie/beego/core/logs"
)

type ModifyAppController struct {
	BaseController
}

func (c *ModifyAppController) ModifyApp() {
	dto := NewSuccessResponseDto()
	app := c.Ctx.Input.Param(":app")
	desc := c.GetString("description")
	alias := c.GetString("alias")

	if app == "" || len(app) > App_Name_Len || desc == "" {
		logs.Info("ModifyApp param name  error !name:", app, "desc:", desc)
		c.setError4Dto(ErrorParam, dto)
		return
	}
	has, err := models.AppDao.Exist(app)
	if err != nil {
		logs.Info("ModifyApp Exist app  error !name:", app, "error:", err.Error())
		c.setError4Dto(ErrorParam, dto)
		return
	}
	if has {
		appinfo := new(models.AppInfo)
		appinfo.App = app
		appinfo.Description = desc
		appinfo.Alias = alias
		err = models.AppDao.Save(appinfo)
		if err != nil {
			logs.Info("AddApp save app  error !name:", app, "error:", err.Error())
			c.setError4Dto(err, dto)
			return
		}
	} else {
		c.setError4Dto(ErrorAppNotExistError, dto)
		return
	}

	c.Data["json"] = dto
	c.ServeJSON()
}
