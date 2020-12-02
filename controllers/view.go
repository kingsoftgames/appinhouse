package controllers

import (
	"github.com/astaxie/beego/core/logs"
	"strings"
)

var mobiles = [...]string{"ipad", "iphone", "ipod", "ipod"}

type ViewController struct {
	BaseController
}

func (c *ViewController) Index() {
	c.TplName = "index.html"
}

func (c *ViewController) Help() {
	c.TplName = "help.html"
}

func (c *ViewController) CreateApp() {
	c.TplName = "create.html"
}

func (c *ViewController) Delete() {
	app := c.GetString("app")
	c.Data["app"] = app
	c.TplName = "delete.html"
}

func (c *ViewController) Update() {
	app := c.GetString("app")
	c.Data["app"] = app
	c.TplName = "update.html"
}

func (c *ViewController) Info() {
	app := c.GetString("app")
	platform := c.GetString("platform")
	environment := c.GetString("environment")
	version := c.GetString("version")
	c.Data["app"] = app
	c.Data["platform"] = platform
	c.Data["environment"] = environment
	c.Data["version"] = version
	c.TplName = "info.html"
}

func (c *ViewController) App() {
	id := c.GetString("id")
	logs.Info(id)
	c.Data["app"] = id
	if c.isMobile() {
		c.TplName = "index_mobile.html"
	} else {
		c.TplName = "index_pc.html"
	}
}

func (c *ViewController) isMobile() bool {
	userAgent := c.Ctx.Request.Header.Get("User-Agent")
	userAgent = strings.ToLower(userAgent)
	isMobile := false
	for _, v := range mobiles {
		if strings.Contains(userAgent, v) {
			isMobile = true
			break
		}
	}
	return isMobile
}
