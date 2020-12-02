package controllers

import (
	. "appinhouse/constants"
	"appinhouse/models"

	"github.com/astaxie/beego/core/logs"
)

type GetBuildController struct {
	BaseController
}

func (c *GetBuildController) GetBuild() {
	dto := NewSuccessItemResponseDto()
	app := c.Ctx.Input.Param(":app")
	version := c.GetString(":version")
	if app == "" || len(app) > App_Name_Len {
		logs.Info("Get param name  error !name:", app)
		c.setError4Dto(ErrorParam, dto)
		return
	}
	has, err := models.AppDao.Exist(app)
	if err != nil {
		logs.Info("Get Exist app  error !name:", app, "error:", err.Error())
		c.setError4Dto(ErrorParam, dto)
		return
	}
	platformParam := c.Ctx.Input.Param(":platform")
	platform, err := GetPlaform(platformParam)
	if err != nil {
		logs.Info("Get param platform  error !platform:", platformParam)
		c.setError4Dto(err, dto)
		return
	}
	envParam := c.Ctx.Input.Param(":environment")
	env, err := GetEnvironment(envParam)
	if err != nil {
		logs.Info("Get param environment  error !environment:", envParam)
		c.setError4Dto(err, dto)
		return
	}
	if version == "" {
		logs.Info("Get get version  error !")
		c.setError4Dto(ErrorParam, dto)
		return
	}
	toffset := 0
	toffsetStr := c.GetString("time_offset")
	if toffsetStr == "" {
		toffset = Not_Offset
	} else {
		toffset, err = c.GetInt("time_offset")
		if err != nil {
			logs.Info("Get param toffset  error !time_offset:", toffset, "error:", err.Error())
			c.setError4Dto(ErrorParam, dto)
			return
		}
	}
	if has {
		info, err := models.DescDao.Get(platform, env, app, version)
		if err != nil {
			logs.Info("Get get app  error !name:", app, "error:", err.Error())
			c.setError4Dto(ErrorParam, dto)
			return
		}

		dto.Item = c.converInfoTOItem(info, platform, env, app, toffset)

	} else {
		c.setError4Dto(ErrorAppNotExistError, dto)
		return
	}

	c.Data["json"] = dto
	c.ServeJSON()
}
