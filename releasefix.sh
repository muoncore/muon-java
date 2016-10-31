#!/bin/sh

npm version patch
npm publish

./deployDocs.sh release
