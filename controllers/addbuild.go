package controllers

import (
	. "appinhouse/constants"
	"appinhouse/models"

	"time"

	"github.com/astaxie/beego/core/logs"
)

type AddBuildController struct {
	BaseController
}

func (c *AddBuildController) AddBuild() {
	dto := NewSuccessResponseDto()

	version := c.GetString("version")
	ctime := c.GetString("time")
	description := c.GetString("description")
	url := c.GetString("url")
	channel := c.GetString("channel")
	softwareUrl := c.GetString("software_url")
	//当生成ios描述文件时，生成plist需要
	id := c.GetString("id")
	title := c.GetString("title")
	fullUrl := c.GetString("full_url")
	displayUrl := c.GetString("display_url")
	extendSoftwareUrlName := c.GetString("software_url_extend_name")
	extendSoftwareUrlKey := c.GetString("software_url_extend_key")

	if ctime == "" || version == "" || channel == "" || softwareUrl == "" {
		logs.Info("Desc Params fail version:", version, "time:", ctime, "description:", description,
			"software_url", softwareUrl, "channel:", channel)
		c.setError4Dto(ErrorParam, dto)
		return
	}
	if (extendSoftwareUrlKey != "" && extendSoftwareUrlName == "") || (extendSoftwareUrlKey == "" && extendSoftwareUrlName != "") {
		logs.Info("Desc Params fail extendSoftwareUrlKey:", extendSoftwareUrlKey, "extendSoftwareUrlName:", extendSoftwareUrlName)
		c.setError4Dto(ErrorParam, dto)
		return
	}
	var err error
	appParam := c.Ctx.Input.Param(":app")
	app, err := c.appExist(appParam)
	if err != nil {
		logs.Info("Desc param app  error !app:", appParam)
		c.setError4Dto(err, dto)
		return
	}
	platformParam := c.Ctx.Input.Param(":platform")
	platform, err := GetPlaform(platformParam)
	if err != nil {
		logs.Info("Desc param platform  error !platform:", platformParam)
		c.setError4Dto(err, dto)
		return
	}

	envParam := c.Ctx.Input.Param(":environment")
	env, err := GetEnvironment(envParam)
	if err != nil {
		logs.Info("Desc param environment  error !environment:", envParam)
		c.setError4Dto(err, dto)
		return
	}

	//	if !existHttpPath(softwareUrl) {
	//		beego.Info("Desc error archive file not exsit,url:", softwareUrl)
	//		c.setError4Dto(ErrorIPANotExistError, dto)
	//		return
	//	}
	score, err := time.Parse(ISO_Datetime, ctime)
	if err != nil {
		logs.Info("Desc error ,time:", ctime)
		c.setError4Dto(ErrorTimeFormat, dto)
		return
	}
	if platform == Ios && channel == Ios_Channel {
		plistUrl, err := c.savePlist(env, app, channel, version, extendSoftwareUrlKey, id, title, softwareUrl, fullUrl, displayUrl)
		if err != nil {
			c.setError4Dto(err, dto)
			return
		}
		softwareUrl = plistUrl
	}
	info, err := models.DescDao.Get(platform, env, app, version)
	if err != nil {
		logs.Info("get Desc error !platform:", platform, "env:", env, "app:", app, "version:", version, "error:", err.Error())
		c.setError4Dto(ErrorDB, dto)
		return
	}
	newInfo := false
	if info == nil {
		newInfo = true
		info = new(models.DescInfo)
		info.Channel = channel
		info.Description = description
		info.Version = version
		info.Time = ctime
		info.Url = url
	}

	if extendSoftwareUrlName != "" {
		if info.ExtendSoftwareUrls == nil {
			info.ExtendSoftwareUrls = make(map[string]string)
		}
		ekey := extendSoftwareUrlKey + Colon + extendSoftwareUrlName
		info.ExtendSoftwareUrls[ekey] = softwareUrl
	} else {
		info.SoftwareUrl = softwareUrl
	}

	err = models.DescDao.Save(platform, env, app, info)
	if err != nil {
		c.setError4Dto(err, dto)
		return
	}
	if newInfo {
		err = models.DescListDao.Save(platform, env, app, info.Version, score)
		if err != nil {
			c.setError4Dto(err, dto)
			return
		}
	}

	c.Data["json"] = dto
	c.ServeJSON()
}

func (c *AddBuildController) savePlist(env Environment, app, channel, version, extendSoftwareUrlKey, id, title, softwareUrl, fullUrl, displayUrl string) (string, error) {

	if id == "" || title == "" {
		logs.Info("PList Params fail .id:", id, "title:", title)
		return "", ErrorParam
	}
	hasFile := true
	//	if fullUrl != "" {
	//		if !existHttpPath(fullUrl) {
	//			hasFile = false
	//		}
	//	}
	//	if displayUrl != "" {
	//		if !existHttpPath(displayUrl) {
	//			hasFile = false
	//		}
	//	}
	if !hasFile {
		logs.Info("PList error fullUrl or displayUrl not exsit.fullUrl:", fullUrl, "displayUrl", displayUrl)
		return "", ErrorParam
	}
	plist, err := models.NewPlist(id, version, title, softwareUrl, fullUrl, displayUrl)

	if err != nil {
		logs.Info("PList NewPlist error :", err.Error())
		return "", err
	}

	err = models.PlistDao.Save(env, app, version, extendSoftwareUrlKey, plist)
	if err != nil {
		logs.Info("PList CreatPlist fail :", err.Error())
		return "", err
	}
	return c.getPlistUrl(env, app, version, extendSoftwareUrlKey), nil
}

func (c *AddBuildController) getPlistUrl(env Environment, app, version, extendSoftwareUrlKey string) string {
	envstr := Dev_Str
	if env == Release {
		envstr = Release_Str
	}
	plist := models.PlistDao.GetField(version, extendSoftwareUrlKey) + Plist
	return c.URLFor("GetPlistController.GetPList", ":app", app, ":environment", envstr, ":version", plist)
}
