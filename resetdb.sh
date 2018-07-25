#!/bin/sh
rm codecafe.db
sqlite3 codecafe.db < src/main/resources/sql/init-db.sql 
