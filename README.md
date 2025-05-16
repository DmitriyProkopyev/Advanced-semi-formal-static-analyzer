# Advanced-semi-formal-static-analyzer
Professional enterprise environments require rigorous code reviews, which are dreadfully long and expensive. Advanced semi-formal static analyzer can take over 90% of the job. Just define your team's code quality requirements in natural language and watch you codebase being rigorously criticized!

## Intallation
To setup and test SAT firstly install deps (needs `sudo` rights)

```bash
sudo apt install gradle python # If not installed
cd sat-analyzer
mvn install clean

# Also install SpotBugs AND build
cd src/main && mkdir resources
cd resources
curl -O -L "https://github.com/spotbugs/spotbugs/releases/download/4.9.3/spotbugs-4.9.3.tgz"
tar -xf spotbugs-*.tgz && mv spotbugs-* spotbugs
rm spotbugs-*.tgz
cd spotbugs && gradleDependencies installation for SAT
# Allow access globally 
sudo ln -s $(pwd)/bin/spotbugs /usr/bin/spotbugs
```

### SpotBugs installation


### Semgrep installation
If you use Linux:
```bash
python -m venv myenv
source ./myenv/bin/activate
pip3 install semgrep
sudo ln -s $(pwd)/myenv/bin/semgrep /usr/bin/semgrep
```

If you use Windows, just type
```bash
pip3 install semgrep
```
It should be installed globally

## Usage
Main file lays in `src/main/java/org/SNA/tool` directory
To run it execute ``