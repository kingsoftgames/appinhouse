package controllers

import (
	. "appinhouse/constants"
	"appinhouse/models"
	"appinhouse/util"

	"github.com/astaxie/beego/core/logs"
)

type GetBuildsController struct {
	BaseController
}

func (c *GetBuildsController) GetBuilds() {
	dto := NewSuccessItemsResponsePageDto()
	page, err := c.GetInt("page")
	if err != nil || page <= 0 || page > Max_Page {
		logs.Info("GetBuilds param page  error !page:", page)
		c.setError4Dto(ErrorParam, dto)
		return
	}
	appParam := c.Ctx.Input.Param(":app")
	app, err := c.appExist(appParam)
	if err != nil {
		logs.Info("GetBuilds param app  error !app:", appParam)
		c.setError4Dto(err, dto)
		return
	}
	platformParam := c.Ctx.Input.Param(":platform")
	platform, err := GetPlaform(platformParam)
	if err != nil {
		logs.Info("GetBuilds param platform  error !platform:", platformParam)
		c.setError4Dto(err, dto)
		return
	}
	envParam := c.Ctx.Input.Param(":environment")
	env, err := GetEnvironment(envParam)
	if err != nil {
		logs.Info("GetBuilds param environment  error !environment:", envParam)
		c.setError4Dto(err, dto)
		return
	}
	toffset := 0
	toffsetStr := c.GetString("time_offset")
	if toffsetStr == "" {
		toffset = Not_Offset
	} else {
		toffset, err = c.GetInt("time_offset")
		if err != nil {
			logs.Info("GetBuilds param toffset  error !time_offset:", toffset, "error:", err.Error())
			c.setError4Dto(ErrorParam, dto)
			return
		}
	}
	start := util.GetStartForPage(page, Page_Size)
	end := util.GetEndForPage(page, Page_Size)

	ret, total, err := c.getList(platform, env, start, end, app, toffset)

	if err != nil {
		dto.Code = GetErrCode(err)
		dto.Msg = GetMsg(dto.Code)
	} else {
		dto.Items = ret
		dto.Page = page
		dto.TotalPage = util.GetTotalPage(total, Page_Size)
	}
	c.Data["json"] = dto
	c.ServeJSON()
}

func (c *GetBuildsController) GetBuilds4Mobile() {
	dto := NewSuccessItemsResponsePageDto()
	page, err := c.GetInt("page")
	if err != nil || page <= 0 || page > Max_Page {
		logs.Info("GetBuilds4Mobile param page  error !page:", page)
		c.setError4Dto(ErrorParam, dto)
		return
	}
	appParam := c.Ctx.Input.Param(":app")
	app, err := c.appExist(appParam)
	if err != nil {
		logs.Info("GetBuilds4Mobile param app  error !app:", appParam)
		c.setError4Dto(err, dto)
		return
	}
	envParam := c.Ctx.Input.Param(":environment")
	env, err := GetEnvironment(envParam)
	if err != nil {
		logs.Info("GetBuilds4Mobile param environment  error !environment:", envParam)
		c.setError4Dto(err, dto)
		return
	}
	toffset := 0
	toffsetStr := c.GetString("time_offset")
	if toffsetStr == "" {
		toffset = Not_Offset
	} else {
		toffset, err = c.GetInt("time_offset")
		if err != nil {
			logs.Info("GetBuilds4Mobile param toffset  error !time_offset:", toffset, "error:", err.Error())
			c.setError4Dto(ErrorParam, dto)
			return
		}
	}
	start := util.GetStartForPage(page, Page_Size)
	end := util.GetEndForPage(page, Page_Size)
	userAgent := c.Ctx.Request.UserAgent()
	osType := util.CheckAgent(userAgent)
	var ret []*ItemDto
	total := 0
	switch osType {
	case util.OS_ANDROID:
		ret, total, err = c.getList(Android, env, start, end, app, toffset)
		break
	case util.OS_IOS:
		ret, total, err = c.getList(Ios, env, start, end, app, toffset)
		break
	default:
		ret, total, err = c.getList(Android, env, start, end, app, toffset)
		break
	}

	if err != nil {
		dto.Code = GetErrCode(err)
		dto.Msg = GetMsg(dto.Code)
	} else {
		dto.Items = ret
		dto.Page = page
		dto.TotalPage = util.GetTotalPage(total, Page_Size)
	}
	c.Data["json"] = dto
	c.ServeJSON()
}

func (c *GetBuildsController) getList(platform Platform, environment Environment, start, end int, app string, toffset int) ([]*ItemDto, int, error) {

	count, err := models.DescListDao.Count(platform, environment, app)
	if err != nil {
		logs.Error("getList count  error :" + err.Error())
		return nil, 0, err
	}
	if count == 0 {
		return make([]*ItemDto, 0, 0), 0, nil
	}
	if start > count {
		logs.Info("getList call page start greate than size")
		return nil, 0, ErrorPageOut
	}
	if end > count {
		end = count - 1
	}

	versions, err := models.DescListDao.GetList(platform, environment, app, start, end)

	if err != nil {
		logs.Error("getList from redis error :" + err.Error())
		return nil, 0, err
	}
	size := len(versions)
	if versions == nil || size == 0 {
		return make([]*ItemDto, 0, size), 0, nil
	}
	items := make([]*ItemDto, 0, size)
	infos, err := models.DescDao.MGet(platform, environment, app, versions)
	for _, info := range infos {
		items = append(items, c.converInfoTOItem(info, platform, environment, app, toffset))
	}
	return items, count, nil
}
