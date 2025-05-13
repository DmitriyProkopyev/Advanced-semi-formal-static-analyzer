# Advanced-semi-formal-static-analyzer
Professional enterprise environments require rigorous code reviews, which are dreadfully long and expensive. Advanced semi-formal static analyzer can take over 90% of the job. Just define your team's code quality requirements in natural language and watch you codebase being rigorously criticized!

## Dependencies installation for SAT
To setup and test SAT firstly install deps (needs `sudo` rights)

```bash
sudo apt install gradle # If not installed
cd sat-analyzer
mvn install clean

# Also install SpotBugs AND build
cd src/main && mkdir resources
cd resources
curl -O -L "https://github.com/spotbugs/spotbugs/releases/download/4.9.3/spotbugs-4.9.3.tgz"
tar -xf spotbugs-*.tgz && mv spotbugs-* spotbugs
rm spotbugs-*.tgz
cd spotbugs && gradle
# Allow access globally 
sudo ln -s $(pwd)/bin/spotbugs /usr/bin/spotbugs
```

## Usage
Main file lays in `src/main/java/org/SNA/tool` directory
To run it execute ``