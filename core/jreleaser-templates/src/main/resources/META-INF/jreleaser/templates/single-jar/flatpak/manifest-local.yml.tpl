# {{jreleaserCreationStamp}}

app-id: {{flatpakAppId}}
runtime: {{flatpakRuntimeName}}
runtime-version: {{flatpakRuntimeVersion}}
sdk: {{flatpakSdk}}
appstream-compose: false
command: {{distributionExecutable}}
finish-args:
  - --filesystem=host
  {{#flatpakFinishArgs}}
  - {{.}}
  {{/flatpakFinishArgs}}
modules:
  - name: {{distributionExecutable}}
    buildsystem: simple
    build-commands:
      - install -Dm755 {{distributionExecutable}} /app/bin/{{distributionExecutable}}
      - install -Dm644 {{distributionArtifactFile}} /app/lib/{{distributionArtifactFile}}
    post-install:
      - install -Dm644 --target-directory=${FLATPAK_DEST}/share/metainfo ${FLATPAK_ID}.metainfo.xml
      - appstream-compose --basename=${FLATPAK_ID} --prefix=${FLATPAK_DEST} --origin=flatpak ${FLATPAK_ID}
    sources:
      - type: file
        path: {{flatpakAppId}}.metainfo.xml
      - type: file
        path: assembly/{{distributionExecutable}}
      - type: file
        path: assembly/{{distributionArtifactFile}}
        sha256: {{distributionChecksumSha256}}