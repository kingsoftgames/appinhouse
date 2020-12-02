// timeutil
package util

import (
	"time"
)

func ParseAutoInLocation(sTime, format string, loc *time.Location) (t time.Time, err error) {

	t, err = time.ParseInLocation(format, sTime, loc)
	if err == nil {
		return
	}
	return
}

func FixUTCTimeToOffsetSpecifiedZoneTime(timeOffset int, localTime, parseFormat, viewFormat string) string {
	clientTimeZone := time.FixedZone("", timeOffset)
	serverTime := MustParseAutoInDefault(localTime, parseFormat)
	return serverTime.In(clientTimeZone).Format(viewFormat)
}

func MustParseAutoInDefault(sTime, format string) (t time.Time) {
	t, err := ParseAutoInLocation(sTime, format, time.UTC)
	if err != nil {
		panic(err)
	}
	return t
}
