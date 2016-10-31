FROM node:4.4.3

RUN apt-get install libstdc++6

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY . /usr/src/app

CMD ["node_modules/mocha/bin/mocha", "-R", "mocha-multi", "--reporter-options", "mocha-teamcity-reporter=-,xunit=test-results/xunit.xml", "--recursive"]
