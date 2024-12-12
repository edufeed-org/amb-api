# Edufeed To AMB Transformer

Run with: `clj -M -m app.core` or `docker-compose up` (before build the uberjar with `clj -T:build uber`)

Runs on port `8890`.

Sets up an endpoint `/resources` which accepts `pk` for an actory public key as a query parameter.

Results are validated against the metadata profile [Allgemeines Metadatenprofil für Bildungsressourcen](https://w3id.org/kim/amb/latest/) before being returned.



