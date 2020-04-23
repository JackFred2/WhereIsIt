Remove-Item ./.gradle -Confirm:$False -Recurse
Remove-Item $env:USERPROFILE\.gradle\caches\fabric-loom -Confirm:$False -Recurse
./gradlew clean cleanIdea genSources genIdeaWorkspace