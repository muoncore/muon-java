

install:
	./gradlew install

publishSnapshot:
	./gradlew artifactoryPublish

publishRelease:
	./gradlew release

test:
	SHORT_TEST=true ./gradlew check

clean:
	./gradlew clean

testextended:
	./gradlew test

