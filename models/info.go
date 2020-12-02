// info.go
package models

import (
	. "appinhouse/constants"

	"bytes"

	"time"

	"gopkg.in/redis.v3"

	"encoding/json"

	"github.com/astaxie/beego/core/logs"
)

var (
	redisClient *redis.Client
	DescListDao *DescInfoListDao
	PlistDao    *PlistInfoDao
	DescDao     *DescInfoDao
	AppDao      *AppInfoDao
	AppListDao  *AppInfoListDao
)

const (
	version     = "version"
	infotime    = "time"
	description = "description"
	url         = "url"
	channel     = "channel"
	channel_def = "none"
)
const (
	key_prefix   = "aih"
	desc_list    = "dlist"
	desc         = "desc"
	key_plist    = "plist"
	key_app      = "app"
	key_app_list = "alist"
)

func InitValue() {
	redisClient = newClient(Redis_Addr, Redis_Password, Redis_PoolSize, int64(Redis_DB))
	DescListDao = newDescInfoListDao()
	PlistDao = newPlistInfoDao()
	DescDao = newDescInfoDao()
	AppDao = newAppInfoDao()
	AppListDao = newAppListDao()
}

type DescInfo struct {
	Version            string            `json:"Version"`
	Time               string            `json:"Time"`
	Description        string            `json:"Description"`
	Url                string            `json:"Url"`
	Channel            string            `json:"Channel"`
	SoftwareUrl        string            `json:"SoftUrl"`
	ExtendSoftwareUrls map[string]string `json:"ExtendSoftUrls"`
}

type DescInfoDao struct {
	client *redis.Client
}

func newDescInfoDao() *DescInfoDao {
	dao := &DescInfoDao{
		client: redisClient,
	}
	return dao
}
func (this *DescInfoDao) Save(platform Platform, env Environment, app string, info *DescInfo) error {
	key := this.getKey(platform, env, app)
	body, _ := json.Marshal(info)
	err := this.client.HSet(key, info.Version, string(body)).Err()
	if err != nil {
		return ErrorDB
	}
	return nil
}
func (this *DescInfoDao) Get(platform Platform, env Environment, app, version string) (*DescInfo, error) {
	key := this.getKey(platform, env, app)
	ret, err := this.client.HGet(key, version).Result()
	if err != nil && err != redis.Nil {
		return nil, ErrorDB
	}

	if ret == "" {
		return nil, nil
	}
	var info *DescInfo
	json.Unmarshal([]byte(ret), &info)
	return info, nil
}
func (this *DescInfoDao) MGet(platform Platform, env Environment, app string, versions []string) ([]*DescInfo, error) {
	key := this.getKey(platform, env, app)

	ret, err := this.client.HMGet(key, versions...).Result()
	if err != nil {
		return nil, ErrorDB
	}
	size := len(ret)
	infos := make([]*DescInfo, 0, size)
	for _, v := range ret {
		var a *DescInfo
		b := v.(string)
		if v == nil {
			logs.Info("ret has nil .key:", key, " versions:", versions)
			continue
		}
		json.Unmarshal([]byte(b), &a)
		infos = append(infos, a)
	}
	return infos, nil
}
func (this *DescInfoDao) Remove(platform Platform, env Environment, app string, versions []string) error {
	key := this.getKey(platform, env, app)

	err := this.client.HDel(key, versions...).Err()
	if err != nil {
		return ErrorDB
	}

	return nil
}
func (this *DescInfoDao) getKey(platform Platform, env Environment, app string) string {
	var buffer bytes.Buffer
	buffer.WriteString(key_prefix)
	buffer.WriteString(Colon)
	buffer.WriteString(app)
	buffer.WriteString(Colon)
	buffer.WriteString(desc)
	buffer.WriteString(Colon)
	if platform == Ios {
		buffer.WriteString(Ios_Str)
	} else {
		buffer.WriteString(Android_Str)
	}
	buffer.WriteString(Colon)
	if env == Dev {
		buffer.WriteString(Dev_Str)
	} else {
		buffer.WriteString(Release_Str)
	}
	return buffer.String()
}

//-------------------------------------------------------------

type DescInfoListDao struct {
	client *redis.Client
}

func newDescInfoListDao() *DescInfoListDao {
	dao := &DescInfoListDao{
		client: redisClient,
	}
	return dao
}

func (this *DescInfoListDao) Save(platform Platform, env Environment, app, version string, score time.Time) error {

	z := redis.Z{
		Score:  float64(score.Unix()),
		Member: version,
	}
	err := this.client.ZAdd(this.getKey(platform, env, app), z).Err()
	if err != nil {
		return ErrorDB
	}
	return nil
}
func (this *DescInfoListDao) GetList(platform Platform, env Environment, app string, start, end int) ([]string, error) {

	ret, err := this.client.ZRevRange(this.getKey(platform, env, app), int64(start), int64(end)).Result()
	if err != nil {
		return nil, ErrorDB
	}
	size := len(ret)
	versions := make([]string, 0, size)
	if len(ret) == 0 {
		return nil, nil
	}
	for _, v := range ret {

		versions = append(versions, v)
	}

	return versions, nil
}
func (this *DescInfoListDao) Count(platform Platform, env Environment, app string) (int, error) {

	ret, err := this.client.ZCard(this.getKey(platform, env, app)).Result()
	if err != nil {
		return 0, ErrorDB
	}

	return int(ret), nil
}
func (this *DescInfoListDao) GetLast(platform Platform, env Environment, app string) (string, error) {

	ret, err := this.client.ZRevRange(this.getKey(platform, env, app), 0, 0).Result()
	if err != nil {
		return "", ErrorDB
	}
	if len(ret) == 0 {
		return "", nil
	}

	return ret[0], nil
}
func (this *DescInfoListDao) Remove(platform Platform, env Environment, app string, residue int) ([]string, error) {
	ret, err := this.client.ZRange(this.getKey(platform, env, app), int64(0), -int64(residue+1)).Result()
	if err != nil {
		return nil, ErrorDB
	}
	err = this.client.ZRemRangeByRank(this.getKey(platform, env, app), int64(0), -int64(residue+1)).Err()
	if err != nil {
		return nil, ErrorDB
	}

	if len(ret) == 0 {
		return nil, nil
	}

	return ret, nil
}
func (this *DescInfoListDao) getKey(platform Platform, env Environment, app string) string {
	var buffer bytes.Buffer
	buffer.WriteString(key_prefix)
	buffer.WriteString(Colon)
	buffer.WriteString(app)
	buffer.WriteString(Colon)
	buffer.WriteString(desc_list)
	buffer.WriteString(Colon)
	if platform == Ios {
		buffer.WriteString(Ios_Str)
	} else {
		buffer.WriteString(Android_Str)
	}
	buffer.WriteString(Colon)
	if env == Dev {
		buffer.WriteString(Dev_Str)
	} else {
		buffer.WriteString(Release_Str)
	}
	return buffer.String()
}

//-------------------------------------------------------------

func newClient(addr, password string, poolSize int, db int64) *redis.Client {
	client := redis.NewClient(&redis.Options{
		Addr:     addr,
		PoolSize: poolSize,
		Password: password,
		DB:       db,
	})
	go autoCheck(client, addr)
	return client
}
func autoCheck(client *redis.Client, addr string) {
	ticker := time.NewTicker(10 * time.Second)
	defer ticker.Stop()
	for _ = range ticker.C {
		err := client.Ping().Err()
		if err != nil {
			logs.Error(err)
			logs.Info("redis not connect. addr:", addr)
		}
	}
}
