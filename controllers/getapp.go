// getapp
package controllers

import (
	. "appinhouse/constants"
	"appinhouse/models"
	"github.com/astaxie/beego/core/logs"
)

type GetAppController struct {
	BaseController
}

func (c *GetAppController) GetApp() {
	dto := NewSuccessAppResponseDto()
	app := c.Ctx.Input.Param(":app")

	if app == "" || len(app) > App_Name_Len {
		logs.Info("App param name  error !name:", app)
		c.setError4Dto(ErrorParam, dto)
		return
	}
	has, err := models.AppDao.Exist(app)
	if err != nil {
		logs.Info("App Exist app  error !name:", app, "error:", err.Error())
		c.setError4Dto(ErrorParam, dto)
		return
	}
	if has {
		app, err := models.AppDao.Get(app)
		if err != nil {
			logs.Info("App get app  error !name:", app, "error:", err.Error())
			c.setError4Dto(ErrorParam, dto)
			return
		}
		dto.Item = c.converAppTOItem(app)
	} else {
		c.setError4Dto(ErrorAppNotExistError, dto)
		return
	}
	c.Data["json"] = dto
	c.ServeJSON()
}
