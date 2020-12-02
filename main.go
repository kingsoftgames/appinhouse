package main

import (
	"appinhouse/asset"
	. "appinhouse/constants"
	"github.com/astaxie/beego/core/logs"
	assetfs "github.com/elazarl/go-bindata-assetfs"
	"net/http"

	_ "appinhouse/routers"

	_ "appinhouse/asset"
	"appinhouse/filters"
	"appinhouse/models"
	"github.com/astaxie/beego/server/web"
	"os"
)

func main() {
	setLog()
	setParam()
	setTemplateFSAndStatic()
	models.InitValue()
	web.Run()
}

type AppinhouseFileSystem struct {
	assetfs *assetfs.AssetFS
}

func (d AppinhouseFileSystem) Open(name string) (http.File, error) {
	return d.assetfs.Open(name)
}

func setTemplateFSAndStatic() {
	assetFS := &assetfs.AssetFS{Asset: asset.Asset, AssetDir: asset.AssetDir, AssetInfo: asset.AssetInfo}
	// 设置页面
	web.SetTemplateFSFunc(func() http.FileSystem {
		return AppinhouseFileSystem{assetFS}
	})
	// 设置静态资源
	web.Handler("/*", http.FileServer(assetFS))
}

func setLog() {
	Log_Dir, _ = web.AppConfig.String("users::log_dir")
	initLogDirectory()
	logs.SetLevel(logs.LevelInformational)
	logs.SetLogFuncCall(true)
	logs.SetLogger("file", `{"filename":"`+Log_Dir+Log_File+`"}`)
	setFilter()

}

func initLogDirectory() (string, error) {
	src := Log_Dir
	if isExist(src) {
		return src, nil
	}

	if err := os.MkdirAll(src, 0757); err != nil {
		if os.IsPermission(err) {
			logs.Info("permission denied path:" + src)
			panic("permission denied")
		}
		return "", err
	}

	return src, nil
}

func isExist(path string) bool {
	_, err := os.Stat(path)
	return err == nil || os.IsExist(err)
}

func setFilter() {
	web.InsertFilter("/api/:app/delete/:platform/:environment", web.BeforeRouter, filters.SecretKeyFilter)
	web.InsertFilter("/api/:app/desc/:platform/:environment", web.BeforeRouter, filters.SecretKeyFilter)
	web.InsertFilter("/api/:app/create", web.BeforeRouter, filters.SecretKeyFilter)
	web.InsertFilter("/api/:app/update", web.BeforeRouter, filters.SecretKeyFilter)
	web.InsertFilter("/api/:app/delete", web.BeforeRouter, filters.SecretKeyFilter)
}

func setParam() {
	Ios_Channel, _ = web.AppConfig.String("users::ios_channel")
	logs.Info("app.conf-> Ios_Channel:", Ios_Channel)
	if Ios_Channel == "" {
		panic("app.conf not have users::ios_channel ")
	}

	App_Name_Len, _ = web.AppConfig.Int("users::app_name_len")
	logs.Info("app.conf-> app_name_len:", App_Name_Len)
	if App_Name_Len == 0 {
		panic("app.conf not have users::app_name_len ")
	}

	Min_Residue, _ = web.AppConfig.Int("users::min_residue")
	logs.Info("app.conf-> Min_Residue:", Min_Residue)
	if Min_Residue == 0 {
		panic("app.conf not have users::min_residue or not int")
	}

	Page_Size, _ = web.AppConfig.Int("users::page_size")
	logs.Info("app.conf-> Page_Size:", Page_Size)
	if Page_Size == 0 {
		panic("app.conf not have users::page_size or not int")
	}

	Max_Page, _ = web.AppConfig.Int("users::max_page")
	logs.Info("app.conf-> Max_Page:", Max_Page)
	if Max_Page == 0 {
		panic("app.conf not have users::max_page or not int")
	}

	Redis_Addr, _ = web.AppConfig.String("redis::addr")
	logs.Info("app.conf-> addr:", Redis_Addr)
	if Redis_Addr == "" {
		panic("app.conf not have users::addr ")
	}

	Redis_Password, _ = web.AppConfig.String("redis::password")
	logs.Info("app.conf-> password:", Redis_Password)

	Redis_DB = web.AppConfig.DefaultInt("redis::db", Redis_DB)
	logs.Info("app.conf-> db:", Redis_DB)

	Redis_PoolSize = web.AppConfig.DefaultInt("redis::pool_siz", Redis_PoolSize)
	logs.Info("app.conf-> pool_siz:", Redis_PoolSize)

	Secret_Key, _ = web.AppConfig.String("authentication::secret_key")
	logs.Info("app.conf-> secret_key:", Secret_Key)
	if Secret_Key == "" {
		panic("app.conf not authentication::secret_key")
	}

	group, _ := web.AppConfig.Strings("securityGroup::inbound")
	logs.Info("app.conf-> inbound:", group)
	if group == nil || len(group) == 0 {
		panic("app.conf not securityGroup::inbound")
	}
	for _, value := range group {
		Security_Group[value] = true
	}
	logs.Info("app.conf-> inbound:", Security_Group)
}
