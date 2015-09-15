# mbeanz

Mbean search and execution util.

## Installation

Install `fzf` first: `brew install --HEAD fzf`

`lein uberjar` the project

`cd mbeanz && pip install -r requirements.txt`

## Usage

### Run the server

Configure:
```
export MBEANZ_OBJECT_PATTERN="MyBeanz:*"
export MBEANZ_JMX_REMOTE_HOST="localhost"
export MBEANZ_JMX_REMOTE_PORT=11080
```

Start the server
`java -jar target/uberjar/mbeanz-0.1.0-SNAPSHOT-standalone.jar`

### Use the client

Either put `mbeanz/mbeanz` in your path or use it from the project root.

## License

Copyright © 2015 Oskar Jung

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
