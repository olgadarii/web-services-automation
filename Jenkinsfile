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
                withCredentials([
                    string(credentialsId: 'github-token', variable: 'GITHUB_TOKEN'),
                    usernamePassword(credentialsId: 'github-credentials', usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_PASS')
                ]) {
                    sh 'mvn test -Dsurefire.suiteXmlFiles=testng.xml -Dgithub.token=$GITHUB_TOKEN -Dgithub.username=$GITHUB_USER -Dgithub.password=$GITHUB_PASS'
                }
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
