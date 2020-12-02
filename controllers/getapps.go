// getapp
package controllers

import (
	. "appinhouse/constants"
	"appinhouse/models"
	"appinhouse/util"

	"github.com/astaxie/beego/core/logs"
)

type GetAppsController struct {
	BaseController
}

func (c *GetAppsController) GetApps() {

	dto := NewSuccessAppsResponseDto()
	page, err := c.GetInt("page")
	if err != nil || page <= 0 || page > Max_Page {
		logs.Info("GetApps param page  error !page:", page)
		c.setError4Dto(ErrorParam, dto)
		return
	}
	count, err := models.AppListDao.Count()
	if err != nil {
		logs.Error("GetApps count  error :" + err.Error())
		c.setError4Dto(err, dto)
		return
	}

	if count == 0 {
		c.Data["json"] = dto
		c.ServeJSON()
		return
	}
	start := util.GetStartForPage(page, Page_Size)
	end := util.GetEndForPage(page, Page_Size)
	if start > count {
		c.setError4Dto(ErrorPageOut, dto)
		return
	}
	if end > count {
		end = count - 1
	}
	apps, err := models.AppListDao.GetList(start, end)
	if err != nil {
		c.setError4Dto(err, dto)
		return
	}
	if apps != nil {
		ret, err := models.AppDao.MGet(apps)
		if err != nil {
			c.setError4Dto(err, dto)
			return
		}
		items := make([]*AppDto, 0, len(ret))
		for _, info := range ret {
			items = append(items, c.converAppTOItem(info))
		}
		dto.Items = items
		dto.Page = page
		dto.TotalPage = util.GetTotalPage(count, Page_Size)
	}

	c.Data["json"] = dto
	c.ServeJSON()
}
