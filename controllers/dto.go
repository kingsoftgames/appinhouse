// dto
package controllers

import (
	. "appinhouse/constants"
)

type ResponseDto interface {
	SetCode(code ErrCode)
	SetMsg(msg string)
}

//--------------------------------------------------------------------
type ItemsResponseDto struct {
	Code  ErrCode    `json:"code"`
	Msg   string     `json:"msg"`
	Items []*ItemDto `json:"items"`
}

func (this *ItemsResponseDto) SetCode(code ErrCode) {
	this.Code = code
}

func (this *ItemsResponseDto) SetMsg(msg string) {
	this.Msg = msg
}

func NewSuccessItemsResponseDto() *ItemsResponseDto {
	i := &ItemsResponseDto{
		Code:  ErrOk,
		Items: []*ItemDto{},
	}
	return i
}

//-------------------------------------------------------------------
type ItemResponseDto struct {
	Code ErrCode  `json:"code"`
	Msg  string   `json:"msg"`
	Item *ItemDto `json:"item"`
}

func (this *ItemResponseDto) SetCode(code ErrCode) {
	this.Code = code
}

func (this *ItemResponseDto) SetMsg(msg string) {
	this.Msg = msg
}

func NewSuccessItemResponseDto() *ItemResponseDto {
	i := &ItemResponseDto{
		Code: ErrOk,
		Item: &ItemDto{},
	}
	return i
}

//--------------------------------------------------------------------
type ItemsDto []*ItemDto

type ItemDto struct {
	Platform    string            `json:"platform"`
	Env         string            `json:"environment"`
	Version     string            `json:"version"`
	Time        string            `json:"time"`
	Description string            `json:"description"`
	Url         string            `json:"url"`
	Down        string            `json:"down"`
	Channel     string            `json:"channel"`
	ExtendUrls  map[string]string `json:"extendUrls"`
}
type ItemsResponsePageDto struct {
	Code      ErrCode    `json:"code"`
	Msg       string     `json:"msg"`
	Items     []*ItemDto `json:"items"`
	Page      int        `json:"page"`
	TotalPage int        `json:"total_page"`
}

func (this *ItemsResponsePageDto) SetCode(code ErrCode) {
	this.Code = code
}

func (this *ItemsResponsePageDto) SetMsg(msg string) {
	this.Msg = msg
}
func NewSuccessItemsResponsePageDto() *ItemsResponsePageDto {
	i := &ItemsResponsePageDto{
		Code:  ErrOk,
		Items: []*ItemDto{},
	}
	return i
}

//--------------------------------------------------------------------
type NormalResponseDto struct {
	Code ErrCode `json:"code"`
	Msg  string  `json:"msg"`
}

func (this *NormalResponseDto) SetCode(code ErrCode) {
	this.Code = code
}

func (this *NormalResponseDto) SetMsg(msg string) {
	this.Msg = msg
}

func NewSuccessResponseDto() *NormalResponseDto {
	i := &NormalResponseDto{
		Code: ErrOk,
	}
	return i
}

//--------------------------------------------------------------------
type AppDto struct {
	App   string `json:"app"`
	Desc  string `json:"description"`
	Alias string `json:"alias"`
}
type AppsResponseDto struct {
	Code      ErrCode   `json:"code"`
	Msg       string    `json:"msg"`
	Items     []*AppDto `json:"items"`
	Page      int       `json:"page"`
	TotalPage int       `json:"total_page"`
}

func (this *AppsResponseDto) SetCode(code ErrCode) {
	this.Code = code
}

func (this *AppsResponseDto) SetMsg(msg string) {
	this.Msg = msg
}
func NewSuccessAppsResponseDto() *AppsResponseDto {
	i := &AppsResponseDto{
		Code:  ErrOk,
		Items: []*AppDto{},
	}
	return i
}

//--------------------------------------------------------------------

type AppResponseDto struct {
	Code ErrCode `json:"code"`
	Msg  string  `json:"msg"`
	Item *AppDto `json:"item"`
}

func (this *AppResponseDto) SetCode(code ErrCode) {
	this.Code = code
}

func (this *AppResponseDto) SetMsg(msg string) {
	this.Msg = msg
}
func NewSuccessAppResponseDto() *AppResponseDto {
	i := &AppResponseDto{
		Code: ErrOk,
		Item: &AppDto{},
	}
	return i
}
