

install:
	./gradlew install

publishSnapshot: clean
	./gradlew artifactoryPublish

publishRelease:
	./gradlew release

test:
	SHORT_TEST=true ./gradlew check

clean:
	./gradlew clean

testextended:
	./gradlew check

