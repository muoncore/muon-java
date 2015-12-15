#!/bin/bash

mkdir tempdocumentation
cd  tempdocumentation
rm -rf muon-documentation
git clone git@github.com:microserviceux/muon-documentation.git
cd  muon-documentation
mkdir -p java
rsync -avr --delete ../../doc/ java/
rm java/*.iml
git add java/
git commit -m "Update Java Documentation"
git push origin
rm -rf tempdocumentation
