# mbeanz

Mbean search and execution util.

## Installation

Install `fzf` first: `brew install --HEAD fzf`

then: `cd share && sudo install.sh`

You need to update the config to be able to search your mbeans:
`sudo vim /etc/mbeanz/config`

To have the api pick up the changes you need to restart it:
```
launchctl unload /System/Library/LaunchDaemons/mbeanz.plist
launchctl load /System/Library/LaunchDaemons/mbeanz.plist
```

After giving it some seconds to start back up you should be able to start the fuzzy finder with `mbeanz`

## Usage in development

### Run the server

Configure:
```
export MBEANZ_OBJECT_PATTERN="MyBeanz:*"
export MBEANZ_JMX_REMOTE_HOST="localhost"
export MBEANZ_JMX_REMOTE_PORT=11080
```

Start the server
`lein run`

## License

Copyright © 2015 Oskar Jung

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
