

install:
	./gradlew install

publishSnapshot:
	./gradlew artifactoryPublish

publishRelease:
	./gradlew release

test:
	SHORT_TEST=true ./gradlew test

clean:
	./gradlew clean

testextended:
	./gradlew test

