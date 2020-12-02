// plist
package models

import (
	. "appinhouse/constants"
	"bytes"

	"github.com/DHowett/go-plist"

	"gopkg.in/redis.v3"
)

const (
	software_kind      = "software-package"
	full_image_kind    = "full-size-image"
	display_image_kind = "display-image"
	metadata_kind      = "software"
)

type ItemsXml struct {
	Array []interface{} `plist:"items"`
}
type AssetsXml struct {
	Array    []interface{} `plist:"assets"`
	Metadata *MetadataXml  `plist:"metadata"`
}
type SoftwareXml struct {
	Kind string `plist:"kind"`
	Url  string `plist:"url"`
}
type ImageXml struct {
	Kind       string `plist:"kind"`
	NeedsShine bool   `plist:"needs-shine"`
	Url        string `plist:"url"`
}

type MetadataXml struct {
	BundleIdentifier string `plist:"bundle-identifier"`
	BundleVersion    string `plist:"bundle-version"`
	Kind             string `plist:"kind"`
	Title            string `plist:"title"`
}

func NewPlist(id, version, title, softvareUrl, fullUrl, displayUrl string) ([]byte, error) {
	meta := &MetadataXml{
		BundleIdentifier: id,
		BundleVersion:    version,
		Kind:             metadata_kind,
		Title:            title,
	}
	if softvareUrl == "" {
		return nil, ErrorIPANotExistError
	}
	software := SoftwareXml{
		Kind: software_kind,
		Url:  softvareUrl,
	}
	assetlist := make([]interface{}, 0, 3)
	assetlist = append(assetlist, software)
	if fullUrl != "" {
		full := ImageXml{
			Kind:       full_image_kind,
			NeedsShine: true,
			Url:        fullUrl,
		}
		assetlist = append(assetlist, full)
	}
	if displayUrl != "" {
		display := ImageXml{
			Kind:       display_image_kind,
			NeedsShine: true,
			Url:        displayUrl,
		}
		assetlist = append(assetlist, display)
	}

	assets := AssetsXml{
		Array:    assetlist,
		Metadata: meta,
	}
	itemlist := make([]interface{}, 0, 1)
	itemlist = append(itemlist, assets)
	data := &ItemsXml{
		Array: itemlist,
	}

	plist, err := plist.MarshalIndent(data, plist.XMLFormat, "\t")
	if err != nil {
		return nil, ErrorCreatePlistError
	}
	return plist, nil
}

//-------------------------------------------------------------

func newPlistInfoDao() *PlistInfoDao {
	dao := &PlistInfoDao{
		client: redisClient,
	}
	return dao
}

type PlistInfoDao struct {
	client *redis.Client
	key    string
}

func (this *PlistInfoDao) Save(env Environment, app string, version string, extendSoftwareUrlName string, plist []byte) error {

	_, err := this.client.HSet(this.getKey(env, app), this.GetField(version, extendSoftwareUrlName), string(plist)).Result()
	if err != nil {
		return ErrorDB
	}
	return nil
}
func (this *PlistInfoDao) Get(env Environment, app string, field string) (string, error) {
	ret, err := this.client.HGet(this.getKey(env, app), field).Result()
	if err != nil && err != redis.Nil {
		return "", ErrorDB
	}

	return ret, nil

}
func (this *PlistInfoDao) Remove(env Environment, app string, versions []string) error {
	if versions == nil {
		return nil
	}
	_, err := this.client.HDel(this.getKey(env, app), versions...).Result()
	if err != nil {
		return ErrorDB
	}
	return nil
}

func (this *PlistInfoDao) getKey(env Environment, app string) string {
	var buffer bytes.Buffer
	buffer.WriteString(key_prefix)
	buffer.WriteString(Colon)
	buffer.WriteString(app)
	buffer.WriteString(Colon)
	buffer.WriteString(key_plist)
	buffer.WriteString(Colon)
	buffer.WriteString(Ios_Str)
	buffer.WriteString(Colon)
	if env == Dev {
		buffer.WriteString(Dev_Str)
	} else {
		buffer.WriteString(Release_Str)
	}

	return buffer.String()
}

func (this *PlistInfoDao) GetField(version string, extendSoftwareUrlName string) string {
	if extendSoftwareUrlName == "" {
		return version
	}
	var buffer bytes.Buffer
	buffer.WriteString(version)
	buffer.WriteString(Underline)
	buffer.WriteString(extendSoftwareUrlName)
	return buffer.String()
}
