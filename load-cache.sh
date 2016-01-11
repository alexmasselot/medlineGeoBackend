#!/usr/bin/env bash
for i in `seq 1950 2015`; do
    for url in api/countryPairs/citationCount/ api/countries/citationCount/ api/geoLoc/citationCountByHexagon/1/; do
        u="http://localhost:9000/$url$i"
        echo "$u"
        curl -s "$u" >/dev/null
    done
done