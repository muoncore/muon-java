

install:
	./gradlew clean publishToMavenLocal

publish: clean
ifndef VERSION
	$(error VERSION is undefined for Muon Java Release)
endif
	echo version=$(VERSION)>gradle.properties
	echo group=io.muoncore>>gradle.properties
	echo exclude=doc,muon-examples>>gradle.properties
	./gradlew artifactoryPublish
	git add gradle.properties
	git commit -m "Update version to $(VERSION )while publishing"
	git push origin

test:
	SHORT_TEST=true ./gradlew check

clean:
	./gradlew clean

testextended:
	./gradlew check

snapshot: clean
	echo repoKey=muon-snapshot>>gradle.properties
	./gradlew artifactoryPublish
