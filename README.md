## appinhouse

公司内网的appinhouse Web服务，mvc结构，静态资源内嵌。

## 快速启动

脚本见`script`目录。

如果希望`单独部署`其中一个，或几个，请参考[文档](#文档)。

##### 环境

`go1.15.3，redis`

##### 静态资源内嵌


使用 Go 开发应用的时候，有时会遇到需要读取静态资源的情况。比如开发 Web 应用，程序需要加载模板文件生成输出的 HTML。在程序部署的时候，除了发布应用可执行文件外，还需要发布依赖的静态资源文件。这给发布过程添加了一些麻烦。既然发布单独一个可执行文件是非常简单的操作，就有人会想办法把静态资源文件打包进 Go 的程序文件中。

本项目使用 [go-bindata](https://github.com/jteeuwen/go-bindata)


```bash
go-bindata -o=asset/asset.go -pkg=asset static/... views/...

#测试方法 不生成二进制文件，直接读取本地静态文件，方便调试
go-bindata -debug -o=asset/asset.go -pkg=asset static/... views/...

# 结合beego
	// 设置页面
	web.SetTemplateFSFunc(func() http.FileSystem {
		return AppinhouseFileSystem{assetFS}
	})
	// 设置静态资源
	web.Handler("/*", http.FileServer(assetFS))
```

## 文档

* [api文档](doc/api.md)
  


