#!/bin/bash
cd /home/kavia/workspace/code-generation/post-playback-content-rating-screen-with-picture-in-picture-219315/post_playback_rating_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

