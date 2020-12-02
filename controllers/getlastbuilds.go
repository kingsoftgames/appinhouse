package controllers

import (
	. "appinhouse/constants"
	"appinhouse/models"
	"appinhouse/util"

	"github.com/astaxie/beego/core/logs"
)

type GetLastBuildsController struct {
	BaseController
}

func (c *GetLastBuildsController) GetLastBuilds() {
	dto := NewSuccessItemsResponseDto()
	userAgent := c.Ctx.Request.UserAgent()
	appParam := c.Ctx.Input.Param(":app")

	app, err := c.appExist(appParam)
	if err != nil {
		logs.Info("Last param app  error !app:", appParam)
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
			logs.Info("Last param toffset  error !time_offset:", toffset, "error:", err.Error())
			c.setError4Dto(ErrorParam, dto)
			return
		}
	}
	osType := util.CheckAgent(userAgent)
	ret := make([]*ItemDto, 0, 4)
	switch osType {
	case util.OS_ANDROID:
		androiddev, err := c.getLastOne(Android, Dev, app, toffset)
		if err != nil {
			dto.Code = GetErrCode(err)
			break
		}
		androidre, err := c.getLastOne(Android, Release, app, toffset)
		if err != nil {
			dto.Code = GetErrCode(err)
			break
		}
		if androiddev != nil {
			ret = append(ret, androiddev)
		}
		if androidre != nil {
			ret = append(ret, androidre)
		}
		break
	case util.OS_IOS:
		iosdev, err := c.getLastOne(Ios, Dev, app, toffset)
		if err != nil {
			dto.Code = GetErrCode(err)
			break
		}
		iosre, err := c.getLastOne(Ios, Release, app, toffset)
		if err != nil {
			dto.Code = GetErrCode(err)
			break
		}
		if iosdev != nil {
			ret = append(ret, iosdev)
		}
		if iosre != nil {
			ret = append(ret, iosre)
		}
		break
	default:
		iosdev, err := c.getLastOne(Ios, Dev, app, toffset)
		if err != nil {
			dto.Code = GetErrCode(err)
			break
		}
		iosre, err := c.getLastOne(Ios, Release, app, toffset)
		if err != nil {
			dto.Code = GetErrCode(err)
			break
		}
		androiddev, err := c.getLastOne(Android, Dev, app, toffset)
		if err != nil {
			dto.Code = GetErrCode(err)
			break
		}
		androidre, err := c.getLastOne(Android, Release, app, toffset)
		if err != nil {
			dto.Code = GetErrCode(err)
			break
		}

		if androiddev != nil {
			ret = append(ret, androiddev)
		}
		if androidre != nil {
			ret = append(ret, androidre)
		}

		if iosdev != nil {
			ret = append(ret, iosdev)
		}
		if iosre != nil {
			ret = append(ret, iosre)
		}
		break
	}
	dto.Msg = GetMsg(dto.Code)
	dto.Items = ret
	c.Data["json"] = dto
	c.ServeJSON()
}

func (c *GetLastBuildsController) getLastOne(platform Platform, environment Environment, app string, toffset int) (*ItemDto, error) {
	version, err := models.DescListDao.GetLast(platform, environment, app)
	if err != nil {
		logs.Error("getLastOne  GetLast error:" + err.Error())
		return nil, err
	}
	if version == "" {
		return nil, nil
	}
	info, err := models.DescDao.Get(platform, environment, app, version)
	if err != nil {
		logs.Error("getLastOne  Get error:" + err.Error())
		return nil, err
	}
	return c.converInfoTOItem(info, platform, environment, app, toffset), nil
}
