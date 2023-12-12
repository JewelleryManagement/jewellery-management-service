#!/bin/bash

DB_NAME=${JMS_DATABASE_NAME}
DB_USER=${JMS_DATABASE_USER}
DB_PASSWORD=${JMS_DATABASE_PASSWORD}

case "$(uname -s)" in
    Linux*|Darwin*)
        BACKUP_DIR="/tmp/jms/db_backup"
        ;;
    MINGW*|CYGWIN*|MSYS*)
        BACKUP_DIR="/c/tmp/jms/db_backup"
        ;;
    *)
        echo "Unsupported OS"
        exit 1
        ;;
esac

mkdir -p "$BACKUP_DIR"

DATE=$(date +"%Y%m%d_%H%M%S")
FILE_NAME="db_backup_${DATE}.sql"

export PGPASSWORD="$DB_PASSWORD"

if pg_dump -U "$DB_USER" -h localhost "$DB_NAME" > "$BACKUP_DIR/$FILE_NAME"; then
    echo "Database backup saved to $BACKUP_DIR/$FILE_NAME"
else
    echo "Backup failed"
    rm -f "$BACKUP_DIR/$FILE_NAME"
fi