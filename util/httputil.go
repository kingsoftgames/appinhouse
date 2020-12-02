// httputil
package util

import (
	"strings"
)

type OsType int

const (
	OS_ANDROID OsType = iota
	OS_IOS
	OS_PC
)

const (
	Android = "android"
	Iphone  = "iphone"
	Pc      = "pc"
	Ipad    = "ipad"
	Ipod    = "ipod"
)

var (
	agents = []string{Android, Iphone, Pc, Ipad, Ipod}
)

func CheckAgent(userAgent string) OsType {
	if userAgent == "" {
		return OS_PC
	}
	userAgent = GetBetweenStr(userAgent, "(", ")")
	userAgent = strings.ToLower(userAgent)
	for _, value := range agents {
		if strings.Index(userAgent, value) != -1 {
			switch value {
			case Android:
				return OS_ANDROID
			case Ipad:
				return OS_IOS
			case Iphone:
				return OS_IOS
			case Ipod:
				return OS_IOS
			default:
				return OS_PC

			}
		}
	}
	return OS_PC
}
func GetBetweenStr(str, start, end string) string {
	n := strings.Index(str, start)
	if n == -1 {
		n = 0
	}
	str = string([]byte(str)[n:])
	m := strings.Index(str, end)
	if m == -1 {
		m = len(str)
	}
	str = string([]byte(str)[:m])
	return str
}
func GetStartForPage(page, pageSize int) int {
	if page == 1 {
		return 0
	}
	return ((page - 1) * pageSize)
}
func GetEndForPage(page, pageSize int) int {
	return (page * pageSize) - 1
}
func GetTotalPage(total, pageSize int) int {
	if total <= pageSize {
		return 1
	}

	ret := (total / pageSize)
	if (total % pageSize) > 0 {
		ret = ret + 1
	}
	return ret
}
