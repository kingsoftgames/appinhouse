// constants
package constants

import (
	"errors"
)

type ErrCode int32

type Platform int32

type Environment int32

type ArchiveFileType int32

type PathType int32

var (
	ErrorUnknown          = errors.New("appinhouse: unknown error")
	ErrorPageOut          = errors.New("appinhouse: page size out of total")
	ErrorParam            = errors.New("appinhouse: param error")
	ErrorCreatePlistError = errors.New("appinhouse: create plist fail")
	ErrorIPANotExistError = errors.New("appinhouse: not have IPA ")
	ErrorDeleteFileError  = errors.New("appinhouse: delete File fail")
	ErrorTimeFormat       = errors.New("appinhouse: time format is error ")
	ErrorDB               = errors.New("appinhouse: db is error")
	ErrorAppNotExistError = errors.New("appinhouse: app not exist")
	ErrorAppExistError    = errors.New("appinhouse: app  exist")

	errCodeToError = map[string]ErrCode{
		ErrorUnknown.Error():          ErrUnknown,
		ErrorParam.Error():            ErrParams,
		ErrorPageOut.Error():          ErrPageOut,
		ErrorCreatePlistError.Error(): ErrCreatePlistError,
		ErrorIPANotExistError.Error(): ErrIPANotExistError,
		ErrorDeleteFileError.Error():  ErrDeleteFileError,
		ErrorTimeFormat.Error():       ErrTimeFormat,
		ErrorDB.Error():               ErrDB,
		ErrorAppNotExistError.Error(): ErrAppNotExistError,
		ErrorAppExistError.Error():    ErrAppExistError,
	}
	errCodeToMsg = map[ErrCode]string{
		ErrOk:               "成功",
		ErrSystemError:      "系统错误",
		ErrUnknown:          "未知错误",
		ErrParams:           "参数错误，请参考API文档",
		ErrPageOut:          "page超出最大值",
		ErrCreatePlistError: "创建文plist错误",
		ErrIPANotExistError: "归档文件不存在",
		ErrDeleteFileError:  "删除文件错误",
		ErrTimeFormat:       "时间格式不正确",
		ErrDB:               "数据库错误",
		ErrAppNotExistError: "应用不存在",
		ErrAppExistError:    "应用已存在",
	}
	Log_Dir     = ""
	Ios_Channel = ""
	Domain      = ""
	platforms   = map[string]Platform{
		Android_Str: Android,
		Ios_Str:     Ios,
	}
	environments = map[string]Environment{
		Release_Str: Release,
		Dev_Str:     Dev,
	}
	Full_Size_Image = ""
	Display_Image   = ""
	Min_Residue     = 0
	Page_Size       = 0
	Max_Page        = 0
	App_Name_Len    = 0
	Redis_Addr      = ""
	Redis_DB        = 0
	Redis_Password  = ""
	Redis_PoolSize  = 10

	Secret_Key = ""

	Security_Group = make(map[string]bool)
)

func AllowIP(ip string) bool {
	_, ok := Security_Group[ip]
	return ok
}

func GetEnvironment(environment string) (Environment, error) {
	if environment == "" {
		return 0, ErrorParam
	}
	if ret, ok := environments[environment]; ok {
		return ret, nil
	}
	return 0, ErrorParam
}
func GetPlaform(platform string) (Platform, error) {
	if platform == "" {
		return 0, ErrorParam
	}
	if ret, ok := platforms[platform]; ok {
		return ret, nil
	}
	return 0, ErrorParam
}
func GetErrCode(e error) ErrCode {
	if e == nil {
		return ErrOk
	}
	if err, ok := errCodeToError[e.Error()]; ok {
		return err
	}
	return ErrUnknown
}
func GetMsg(code ErrCode) string {
	if msg, ok := errCodeToMsg[code]; ok {
		return msg
	}
	return errCodeToMsg[ErrUnknown]
}

const (
	ErrOk               = 0
	ErrSystemError      = -1
	ErrUnknown          = ErrCode(-2)
	ErrParams           = ErrCode(1001)
	ErrPageOut          = ErrCode(1002)
	ErrCreatePlistError = ErrCode(1003)
	ErrIPANotExistError = ErrCode(1004)
	ErrDeleteFileError  = ErrCode(1005)
	ErrTimeFormat       = ErrCode(1006)
	ErrDB               = ErrCode(1007)
	ErrAppNotExistError = ErrCode(1008)
	ErrAppExistError    = ErrCode(1009)
)

const (
	Ios     = Platform(1)
	Android = Platform(2)
	Dev     = Environment(1)
	Release = Environment(2)
)
const (
	Ios_Str     = "ios"
	Android_Str = "android"
	Release_Str = "release"
	Dev_Str     = "dev"
	Url_Start   = "/api"
	Plist       = ".plist"
)
const (
	Slash         = "/"
	Colon         = ":"
	Underline     = "_"
	Vertical      = "|"
	Log_File      = "appinhouse.log"
	ISO_Datetime  = "2006-01-02T15:04:05-0700"
	UTC_Datetime  = "2006-01-02T15:04:05Z"
	View_Datetime = "2006-01-02 15:04:05"
	Https         = "https://"
	Not_Offset    = 999999999
)
const (
	APK           = ArchiveFileType(0)
	IPA           = ArchiveFileType(1)
	FullSizeImage = ArchiveFileType(2)
	DisplayImage  = ArchiveFileType(3)
)
const (
	List = PathType(0)
	Data = PathType(1)
)
const (
	Up   = "up"
	Down = "down"
	Top  = "top"
	End  = "end"
)

func ValidateMoveOperation(operation string) error {
	switch operation {
	case Up:
		break
	case Down:
		break
	case Top:
		break
	case End:
		break
	default:
		return ErrorParam
	}
	return nil
}
