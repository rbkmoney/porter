#!groovy
build('porter', 'java-maven') {
    checkoutRepo()
    loadBuildUtils()

    def javaServicePipeline
    runStage('load JavaService pipeline') {
        javaServicePipeline = load("build_utils/jenkins_lib/pipeJavaServiceInsideDocker.groovy")
    }

    def serviceName = env.REPO_NAME
    def mvnArgs = '-DjvmArgs="-Xmx256m"'

    pipeJavaServiceInsideDocker(serviceName, mvnArgs)
}
