// app
package models

import (
	. "appinhouse/constants"
	"bytes"
	"encoding/json"
	"time"

	"github.com/astaxie/beego/core/logs"

	"gopkg.in/redis.v3"
)

type AppInfo struct {
	App         string `json:"App"`
	Description string `json:"Desc"`
	Alias       string `json:"Alias"`
}

type AppInfoDao struct {
	client *redis.Client
}

type AppListInfo struct {
	Key   string
	Score float64
}

func newAppInfoDao() *AppInfoDao {
	dao := &AppInfoDao{
		client: redisClient,
	}
	return dao
}
func (this *AppInfoDao) Save(app *AppInfo) error {
	key := this.getKey()
	body, _ := json.Marshal(app)
	err := this.client.HSet(key, app.App, string(body)).Err()
	if err != nil {
		return ErrorDB
	}
	return nil
}
func (this *AppInfoDao) Exist(app string) (bool, error) {
	ret, err := this.client.HExists(this.getKey(), app).Result()
	if err != nil && err != redis.Nil {
		return false, ErrorDB
	}
	return ret, nil
}

func (this *AppInfoDao) MGet(apps []string) ([]*AppInfo, error) {
	key := this.getKey()
	ret, err := this.client.HMGet(key, apps...).Result()
	if err != nil {
		return nil, ErrorDB
	}
	size := len(ret)
	infos := make([]*AppInfo, 0, size)
	for _, v := range ret {
		var a *AppInfo
		b := v.(string)
		if v == nil {
			logs.Info("ret has nil .key:", key, " versions:", apps)
			continue
		}
		json.Unmarshal([]byte(b), &a)
		infos = append(infos, a)
	}
	return infos, nil
}
func (this *AppInfoDao) Get(app string) (*AppInfo, error) {
	key := this.getKey()
	ret, err := this.client.HGet(key, app).Result()
	if err != nil && err != redis.Nil {
		return nil, ErrorDB
	}

	if ret == "" {
		return nil, nil
	}
	var info *AppInfo
	json.Unmarshal([]byte(ret), &info)
	return info, nil
}
func (this *AppInfoDao) Remove(app string) error {

	err := this.client.HDel(this.getKey(), app).Err()
	if err != nil {
		return ErrorDB
	}

	return nil
}
func (this *AppInfoDao) getKey() string {
	var buffer bytes.Buffer
	buffer.WriteString(key_prefix)
	buffer.WriteString(Colon)
	buffer.WriteString(key_app)
	return buffer.String()
}

//-------------------------------------------------------------

type AppInfoListDao struct {
	client *redis.Client
}

func newAppListDao() *AppInfoListDao {
	dao := &AppInfoListDao{
		client: redisClient,
	}
	return dao
}

func (this *AppInfoListDao) SaveWithScore(app string, score float64) error {

	z := redis.Z{
		Score:  score,
		Member: app,
	}
	err := this.client.ZAdd(this.getKey(), z).Err()
	if err != nil {
		return ErrorDB
	}
	return nil
}

func (this *AppInfoListDao) Save(app string) error {
	score := float64(time.Now().Unix())
	return this.SaveWithScore(app, score)
}

func (this *AppInfoListDao) GetList(start, end int) ([]string, error) {

	ret, err := this.client.ZRevRange(this.getKey(), int64(start), int64(end)).Result()
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

func (this *AppInfoListDao) GetAppByRank(rank int) (*AppListInfo, error) {

	ret, err := this.GetListWithScore(rank, rank)
	if err != nil {
		return nil, err
	}
	if len(ret) == 0 {
		return nil, nil
	}
	return ret[0], nil
}

func (this *AppInfoListDao) GetListWithScore(start, end int) ([]*AppListInfo, error) {

	ret, err := this.client.ZRevRangeWithScores(this.getKey(), int64(start), int64(end)).Result()
	if err != nil {
		return nil, ErrorDB
	}
	size := len(ret)
	linfos := make([]*AppListInfo, 0, size)
	if len(ret) == 0 {
		return nil, nil
	}
	for _, v := range ret {
		linfo := &AppListInfo{}
		linfo.Key = v.Member.(string)
		linfo.Score = v.Score
		linfos = append(linfos, linfo)
	}

	return linfos, nil
}

func (this *AppInfoListDao) Exist(app string) (bool, error) {

	_, err := this.client.ZRank(this.getKey(), app).Result()
	if err != nil {
		if err == redis.Nil {
			return false, nil
		} else {
			return false, ErrorDB
		}
	}
	return true, nil
}
func (this *AppInfoListDao) Count() (int, error) {

	ret, err := this.client.ZCard(this.getKey()).Result()
	if err != nil {
		return 0, ErrorDB
	}

	return int(ret), nil
}

func (this *AppInfoListDao) Remove(app string) error {

	err := this.client.ZRem(this.getKey(), app).Err()
	if err != nil {
		return ErrorDB
	}
	return nil
}

func (this *AppInfoListDao) GetRank(app string) (int, error) {

	ret, err := this.client.ZRevRank(this.getKey(), app).Result()
	if err != nil {
		return 0, ErrorDB
	}

	return int(ret), nil
}

func (this *AppInfoListDao) getKey() string {
	var buffer bytes.Buffer
	buffer.WriteString(key_prefix)
	buffer.WriteString(Colon)
	buffer.WriteString(key_app_list)
	return buffer.String()
}
