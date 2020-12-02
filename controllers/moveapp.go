package controllers

import (
	. "appinhouse/constants"
	"appinhouse/models"

	"github.com/astaxie/beego/core/logs"
)

type MoveAppController struct {
	BaseController
}

func (c *MoveAppController) MoveApp() {
	dto := NewSuccessResponseDto()
	app := c.Ctx.Input.Param(":app")
	operation := c.Ctx.Input.Param(":operation")
	if ValidateMoveOperation(operation) != nil {
		logs.Info("UpApp param name  error !name:", app)
		c.setError4Dto(ErrorParam, dto)
		return
	}

	meRank, err := models.AppListDao.GetRank(app)
	if err != nil {
		return
	}
	targetRank, err := c.getRanks(operation, meRank)
	if err != nil {
		return
	}

	me, err := models.AppListDao.GetAppByRank(meRank)
	target, err := models.AppListDao.GetAppByRank(targetRank)
	if me == nil || target == nil {
		return
	}
	meScore := me.Score
	switch operation {
	case Up, Down:
		me.Score = target.Score
		target.Score = meScore
		models.AppListDao.SaveWithScore(target.Key, target.Score)
		break
	case Top:
		me.Score = target.Score + 1
		break
	case End:
		me.Score = target.Score - 1
		break
	default:
	}
	models.AppListDao.SaveWithScore(me.Key, me.Score)
	c.Data["json"] = dto
	c.ServeJSON()
}

func (c *MoveAppController) getRanks(operation string, rank int) (int, error) {
	index := 0
	var err error
	switch operation {
	case Up:
		if rank != 0 {
			index = rank - 1
		} else {
			err = ErrorParam
		}
		break
	case Down:
		index = rank + 1
		break
	case Top:
		break
	case End:
		index = -1
		break
	default:
		err = ErrorParam
	}
	return index, err
}
