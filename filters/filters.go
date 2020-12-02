// filters
package filters

import (
	. "appinhouse/constants"
	"github.com/astaxie/beego/core/logs"

	"github.com/astaxie/beego/server/web/context"
)

var SecretKeyFilter = func(ctx *context.Context) {
	secretKey := ctx.Input.Header("X-SecretKey")
	if secretKey == "" || len(secretKey) == 0 || secretKey != Secret_Key {
		msg := "Missing or invalid X-SecretKey HTTP header"
		logs.Info(msg)
		ctx.Output.SetStatus(400)
		ctx.Output.Body([]byte(msg))
	}
}

var SecurityGroupFilter = func(ctx *context.Context) {
	ip := ctx.Input.IP()
	if !AllowIP(ip) {
		msg := "not allow this ip access."
		logs.Info(msg, "ip:", ip)
		ctx.Output.SetStatus(400)
		ctx.Output.Body([]byte(msg))
	}
}
