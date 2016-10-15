

install:
	./gradlew install

publishSnapshot:
	./gradlew artifactoryPublish

publishRelease:
	./gradlew release

test:
	./gradlew test