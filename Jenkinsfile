pipeline {

  agent {
    docker {
      /* using a custom build image with a defined home directory for UID 1000 among other things */
      image 'nexus.intranda.com:4443/maven:3.9.3-eclipse-temurin-17'
      registryUrl 'https://nexus.intranda.com:4443'
      registryCredentialsId 'jenkins-docker'
      args '-v $HOME/.m2:/var/maven/.m2:z -v $HOME/.config:/var/maven/.config -v $HOME/.sonar:/var/maven/.sonar -u 1000 -ti -e _JAVA_OPTIONS=-Duser.home=/var/maven -e MAVEN_CONFIG=/var/maven/.m2'
    }
  }

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '15', daysToKeepStr: '90', numToKeepStr: '')
    disableConcurrentBuilds()
  }

  stages {
    stage('prepare') {
      steps {
        sh 'git clean -fdx'
      }
    }
    stage('build') {
      steps {
        sh 'mvn -f Goobi/pom.xml clean verify'
      }
    }
    stage('sonarcloud') {
      when {
        anyOf {
          branch 'master'
          branch 'sonar_*'
        }
      }
      steps {
        withCredentials([string(credentialsId: 'jenkins-sonarcloud', variable: 'TOKEN')]) {
          sh 'mvn -f Goobi/pom.xml verify sonar:sonar -Dsonar.token=$TOKEN'
        }
      }
    }
    stage('deployment to maven repository') {
      when {
        anyOf {
        branch 'master'
        branch 'develop'
        }
      }
      steps {
        sh 'mvn -f Goobi/pom.xml -DskipTests=true -Dcheckstyle.skip=true -Dmdep.analyze.skip=true deploy'
      }
    }
    stage('trigger pull-requester') {
      when {
        branch 'master'
      }
      steps {
        build wait: false, job: '../goobi-plugins-pull-requester/master'
      }
    }
  }
  post {
    always {
      junit "**/target/surefire-reports/*.xml"
      step([
        $class           : 'JacocoPublisher',
        execPattern      : 'Goobi/module-ci/target/jacoco.exec',
        classPattern     : 'Goobi/module-ci/target/classes/',
        sourcePattern    : 'Goobi/src/main/java',
        exclusionPattern : '**/*Test.class'
      ])
      recordIssues (
        enabledForFailure: true, aggregatingResults: false,
        tools: [checkStyle(pattern: '**/target/checkstyle-result.xml', reportEncoding: 'UTF-8')]
      )
      dependencyCheckPublisher pattern: '**/target/dependency-check-report.xml'
    }
    success {
      archiveArtifacts artifacts: 'Goobi/module-war/target/*.war, Goobi/install/db/goobi.sql', fingerprint: true
    }
    changed {
      emailext(
        subject: '${DEFAULT_SUBJECT}',
        body: '${DEFAULT_CONTENT}',
        recipientProviders: [requestor(),culprits()],
        attachLog: true
      )
    }
  }
}
/* vim: set ts=2 sw=2 tw=120 et :*/
