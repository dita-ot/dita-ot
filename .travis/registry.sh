#!/usr/bin/env sh

ROOT=$TRAVIS_BUILD_DIR
openssl aes-256-cbc -K $encrypted_9d373418652a_key -iv $encrypted_9d373418652a_iv -in $ROOT/.travis/ditaotbot_rsa.enc -out $ROOT/.travis/ditaotbot_rsa -d
eval "$(ssh-agent -s)"
chmod 600 $ROOT/.travis/ditaotbot_rsa
ssh-add $ROOT/.travis/ditaotbot_rsa

REGISTRY=$TRAVIS_BUILD_DIR/registry
git clone -b master https://github.com/dita-ot/registry.git $REGISTRY

node $ROOT/.travis/registry.js $ROOT/build/distributions $REGISTRY || exit 1

BRANCH=feature/dita-ot-${TRAVIS_TAG}
cd $REGISTRY
git config user.name "DITA-OT Bot"
git config user.email "ditaotbot@gmail.com"
git add --all
git commit -s -a -m "Update plugins for DITA-OT ${TRAVIS_TAG}"
git remote set-url origin git@github.com:dita-ot/registry.git
git push -v origin master:$BRANCH
