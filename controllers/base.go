// base
package controllers

import (
	. "appinhouse/constants"
	"appinhouse/models"
	"appinhouse/util"
	"strings"
	"time"

	"github.com/astaxie/beego/server/web"
)

type BaseController struct {
	web.Controller
}

func (c *BaseController) setError4Dto(err error, dto ResponseDto) {
	code := GetErrCode(err)
	dto.SetCode(code)
	dto.SetMsg(GetMsg(code))
	c.Data["json"] = dto
	c.ServeJSON()
}

func (c *BaseController) appExist(app string) (string, error) {
	if app == "" {
		return "", ErrorParam
	}
	has, err := models.AppDao.Exist(app)
	if err != nil {
		return app, err
	}
	if !has {
		return app, ErrorAppNotExistError
	}
	return app, nil
}

func (c *BaseController) converAppTOItem(info *models.AppInfo) *AppDto {

	item := new(AppDto)
	item.App = info.App
	item.Desc = info.Description
	item.Alias = info.Alias
	return item
}

func (c *BaseController) converInfoTOItem(info *models.DescInfo, platform Platform, environment Environment, app string, toffset int) *ItemDto {
	if info == nil {
		return nil
	}
	item := new(ItemDto)
	item.ExtendUrls = make(map[string]string)

	switch platform {
	case Android:
		item.Platform = Android_Str
		item.Down = info.SoftwareUrl
		if info.ExtendSoftwareUrls != nil && len(info.ExtendSoftwareUrls) > 0 {
			for k, v := range info.ExtendSoftwareUrls {
				ename := c.getNameForExtendSoftwareUrl(k)
				item.ExtendUrls[ename] = v
			}
		}
		break
	case Ios:
		item.Platform = Ios_Str
		if info.Channel == Ios_Channel {
			if info.SoftwareUrl != "" {
				item.Down = Https + Domain + info.SoftwareUrl
			}
			if info.ExtendSoftwareUrls != nil && len(info.ExtendSoftwareUrls) > 0 {
				for k, v := range info.ExtendSoftwareUrls {
					ename := c.getNameForExtendSoftwareUrl(k)
					item.ExtendUrls[ename] = Https + Domain + v
				}
			}
		} else {
			item.Down = info.SoftwareUrl
			if info.ExtendSoftwareUrls != nil && len(info.ExtendSoftwareUrls) > 0 {
				for k, v := range info.ExtendSoftwareUrls {
					ename := c.getNameForExtendSoftwareUrl(k)
					item.ExtendUrls[ename] = v
				}
			}
		}
		break
	}
	switch environment {
	case Release:
		item.Env = Release_Str
		break
	case Dev:
		item.Env = Dev_Str
		break
	}
	item.Channel = info.Channel
	item.Description = info.Description

	item.Time = c.converToClientTime(info.Time, toffset)

	item.Url = info.Url
	item.Version = info.Version

	return item
}

func (c *BaseController) getNameForExtendSoftwareUrl(key string) string {
	keys := strings.Split(key, Colon)
	if len(keys) == 2 {
		return keys[1]
	}
	return key
}

func (c *BaseController) converToClientTime(ctime string, toffset int) string {
	l := len(ctime)
	if toffset == Not_Offset {
		if len(ISO_Datetime) == l {
			t, _ := time.Parse(ISO_Datetime, ctime)
			rs := []rune(t.Local().String())
			return string(rs[0 : len(ISO_Datetime)-5])
		} else if len(UTC_Datetime) == l {
			t, _ := time.Parse(time.RFC3339, ctime)
			rs := []rune(t.Local().String())
			return string(rs[0 : len(UTC_Datetime)-1])
		} else {
			return ctime
		}

	} else {
		if len(ISO_Datetime) == l {
			return util.FixUTCTimeToOffsetSpecifiedZoneTime(toffset*60, ctime, ISO_Datetime, View_Datetime)
		} else if len(UTC_Datetime) == l {
			return util.FixUTCTimeToOffsetSpecifiedZoneTime(toffset*60, ctime, UTC_Datetime, View_Datetime)
		} else {
			return ctime
		}
	}

}
