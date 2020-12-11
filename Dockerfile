FROM docker.shiyou.kingsoft.com/mirror/golang:1.15.0 as build-env

ENV GO111MODULE=on
ENV BUILDPATH=appinhouse
ENV GOPROXY=https://goproxy.io
ENV GOPATH=/go
RUN mkdir -p /go/src/${BUILDPATH}
COPY ./ /go/src/${BUILDPATH}
RUN cd /go/src/${BUILDPATH} && CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go install -v

FROM alpine:latest

COPY --from=build-env /go/bin/appinhouse /go/bin/appinhouse
COPY ./conf /go/bin/conf

RUN addgroup -S app -g 1000 && adduser -S app -G app -u 1000 \
    && chown -R app:app /go \
    && chmod -R u+x /go

USER app
WORKDIR /go/bin/
CMD ["/go/bin/appinhouse"]