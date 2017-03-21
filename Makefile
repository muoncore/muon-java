

install:
	./gradlew install

publish: clean
ifndef VERSION
	$(error VERSION is undefined for Muon Java Release)
endif
	echo currentVersion=$(VERSION)>gradle.properties
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

