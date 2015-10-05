FROM java:openjdk-8u45-jdk
MAINTAINER Simplicity Itself

RUN apt-get install -y unzip

COPY muon-tck/build/distributions/muon-tck*.zip /
COPY setupTck.sh /
RUN chmod 755 /setupTck.sh

RUN /setupTck.sh

CMD ["/muon-tck/bin/muon-tck"]
