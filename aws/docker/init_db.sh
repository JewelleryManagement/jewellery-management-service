#!/bin/bash

if [ -z "$(ls -A /var/lib/postgresql/data)" ]; then
    # Data directory is empty, initialize the database
    /usr/lib/postgresql/13/bin/initdb -D /var/lib/postgresql/data

    # Start PostgreSQL server
    /usr/lib/postgresql/13/bin/pg_ctl -D /var/lib/postgresql/data -l logfile start

    # Copy existing database files to the data directory
    cp -r /path/to/your/existing/database/* /var/lib/postgresql/data/

    # Stop PostgreSQL server
    /usr/lib/postgresql/13/bin/pg_ctl -D /var/lib/postgresql/data -l logfile stop
fi

# Start PostgreSQL server
exec /usr/lib/postgresql/13/bin/postgres -D /var/lib/postgresql/data -c config_file=/etc/postgresql/12/main/postgresql.conf
