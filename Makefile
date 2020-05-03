DOCKER_COMMAND=docker run -it --rm --name priority-sorter -v $(PWD):/usr/src/priority-sorter -v "${HOME}/.m2":/root/.m2 --env-file ./maven.env -w /usr/src/priority-sorter maven:3-jdk-8

build: ## build priority sorter plugn
	$(DOCKER_COMMAND) mvn install

plugin: ## create jenkins plugin archetype skeleton
	$(DOCKER_COMMAND) mvn archetype:generate -Dfilter="io.jenkins.archetypes:"

test-coverage: clean ## package priority sorter plugin
	$(DOCKER_COMMAND) mvn -P enable-jacoco -Dmaven.spotbugs.skip=true test verify

test: clean ## package priority sorter plugin
	$(DOCKER_COMMAND) mvn -Dmaven.spotbugs.skip=true test

test-only: clean ## package priority sorter plugin
	$(DOCKER_COMMAND) mvn -Djava.util.logging.config.file=./src/test/resources/logging.properties -Dtest=$(TEST_NAME) -Dmaven.spotbugs.skip=true test

package: clean ## package priority sorter plugin
	$(DOCKER_COMMAND) mvn -P quick-build -Xlint:all -Dmaven.spotbugs.skip=true -Dmaven.test.skip=true versions:use-latest-versions package
	cat target/classes/META-INF/annotations/hudson.Extension.txt

clean:
	# delete all target foldes except jenkins-for-test reduce test time by 80 seconds
	@bash -c $$'cd target; shopt -s extglob\nrm -rf !("jenkins-for-test"); cd ..'

spotbugs: ## package priority sorter plugin
	$(DOCKER_COMMAND) mvn -Dmaven.test.skip=true install spotbugs:check
	#spotbugs:gui
