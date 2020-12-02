package controllers

import (
	. "appinhouse/constants"
	"appinhouse/models"

	"github.com/astaxie/beego/core/logs"
)

type RemoveBuildsontroller struct {
	BaseController
}

func (c *RemoveBuildsontroller) Clean() {
	dto := NewSuccessResponseDto()
	residue, err := c.GetInt("residue", -1)
	if err != nil || residue < Min_Residue {
		logs.Info("Delete param residue  error !residue:", residue)
		c.setError4Dto(ErrorParam, dto)
		return
	}
	appParam := c.Ctx.Input.Param(":app")
	app, err := c.appExist(appParam)
	if err != nil {
		logs.Info("Delete param app  error !app:", appParam)
		c.setError4Dto(err, dto)
		return
	}
	platformParam := c.Ctx.Input.Param(":platform")
	platform, err := GetPlaform(platformParam)
	if err != nil {
		logs.Info("Delete param platform  error !platform:", platformParam)
		c.setError4Dto(err, dto)
		return
	}
	envParam := c.Ctx.Input.Param(":environment")
	env, err := GetEnvironment(envParam)
	if err != nil {
		logs.Info("Delete param environment  error !environment:", envParam)
		c.setError4Dto(err, dto)
		return
	}
	count, err := models.DescListDao.Count(platform, env, app)
	if err != nil {
		logs.Error("Delete count  error :" + err.Error())
		c.setError4Dto(err, dto)
		return
	}

	if residue < count {
		versions, err := models.DescListDao.Remove(platform, env, app, residue)
		if err != nil {
			c.setError4Dto(err, dto)
			return
		}

		if versions != nil && len(versions) > 0 {

			if platform == Ios {
				extendUrls := make([]string, 10)
				infos, _ := models.DescDao.MGet(platform, env, app, versions)
				for _, info := range infos {
					if info.ExtendSoftwareUrls != nil && len(info.ExtendSoftwareUrls) > 0 {

						for k, _ := range info.ExtendSoftwareUrls {
							ename := c.getNameForExtendSoftwareUrl(k)
							extendUrls = append(extendUrls, models.PlistDao.GetField(info.Version, ename))
						}
					}
				}
				err := models.PlistDao.Remove(env, app, extendUrls)
				err = models.PlistDao.Remove(env, app, versions)
				if err != nil {
					c.setError4Dto(err, dto)
					return
				}
			}
			models.DescDao.Remove(platform, env, app, versions)
		}
	}
	c.Data["json"] = dto
	c.ServeJSON()
}
