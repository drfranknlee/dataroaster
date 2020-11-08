#!/bin/sh

cd {{ tempDirectory }};

openssl req -new -newkey rsa:4096 -nodes -keyout {{ keyFileName }} -out {{ csrFileName }} -subj "/CN={{ user }}/O={{ group }}";