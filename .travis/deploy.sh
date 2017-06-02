#!/bin/bash
#openssl aes-256-cbc -K $encrypted_9d373418652a_key -iv $encrypted_9d373418652a_iv -in gradle.properties.enc -out gradle.properties -d
openssl aes-256-cbc -K $encrypted_1b8d0aeee504_key -iv $encrypted_1b8d0aeee504_iv -in .travis/24AC15F2.gpg.enc -out .travis/24AC15F2.gpg -d
./gradlew -b dist.gradle uploadArchives -PossrhUsername=$OSSRH_USERNAME -PossrhPassword=$OSSRH_PASSWORD -Psigning.password=SIGNING_PASSWORD
