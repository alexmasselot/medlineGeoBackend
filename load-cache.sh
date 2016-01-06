#!/usr/bin/env bash
for i in `seq 1950 2015`; do
    for url in api/country-pair/count/ api/country/count/ api/citation-located/countByHexagon/1?year=; do
        u="http://localhost:9000/$url$i"
        echo "$u"
        curl -s "$u" >/dev/null
    done
done