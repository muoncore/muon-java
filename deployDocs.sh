#!/bin/bash

mkdir tempdocumentation
cd  tempdocumentation
git clone git@github.com:microserviceux/muon-documentation.git
cd  muon-documentation
mkdir -p java
rsync -avr --delete ../../doc/ java/
git add java/
git commit -m "Update Java Documentation"
git push origin

