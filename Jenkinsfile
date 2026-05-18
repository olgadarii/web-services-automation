pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn compile'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -Dsurefire.suiteXmlFiles=testng.xml'
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
        success {
            echo 'All tests passed!'
        }
        failure {
            echo 'Tests failed — check the TestNG report in target/surefire-reports.'
        }
    }
}
