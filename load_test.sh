#!/usr/bin/env bash

wrk -t20 -c500 -d60s http://localhost:7070
