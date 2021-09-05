{{/* vim: set filetype=mustache: */}}
{{/*
Kafka and zookeeper customisation
*/}}
{{- define "kafka.name" -}}
{{- default "jhipster-kafka" -}}
{{- end -}}

{{- define "kafka.fullname" -}}
{{- default "jhipster-kafka" -}}
{{- end -}}

{{- define "zookeeper.name" -}}
{{- default "jhipster-zookeeper" -}}
{{- end -}}

{{- define "zookeeper.fullname" -}}
{{- default "jhipster-zookeeper" -}}
{{- end -}}

{{- define "kafka.zookeeper.fullname" -}}
{{- default "jhipster-zookeeper" -}}
{{- end -}}

