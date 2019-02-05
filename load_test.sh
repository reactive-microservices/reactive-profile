#!/usr/bin/env bash

wrk -t20 -c500 -d60s http://localhost:9090/profile/1

