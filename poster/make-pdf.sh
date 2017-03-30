#!/usr/bin/env bash

if ! which wkhtmltopdf; then
    echo "wkhtmltopdf must be installed to convert"
fi

wkhtmltopdf poster/index.html poster/poster.pdf
