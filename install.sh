#!/bin/bash

REPOS="muon-java muon-node muon-clojure muon-cli muonjs-gateway muon.js documentation photon muon-intro-talk molecule muon-pipeline photon-config"

for i in $REPOS ; do
  echo "Installing into $i"
  git clone git@github.com:muoncore/$i /tmp/licenseupdate/$i
  cp -R . /tmp/licenseupdate/$i
  cp -R .github /tmp/licenseupdate/$1
  rm /tmp/licenseupdate/$1/install.sh
  cd /tmp/licenseupdate/$i
  git add .
  git commit -m "Update license artifacts"
  git push origin
done
