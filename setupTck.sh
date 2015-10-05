#!/bin/bash

unzip muon-tck*
rm *.zip

PROGRAM_FULL=$(ls -d muon* )
echo "Muon is ${PROGRAM_FULL}"

PROGRAM_NAME=$(echo $PROGRAM_FULL | sed 's/-[0-9].*//')

ln -s $PROGRAM_FULL $PROGRAM_NAME
